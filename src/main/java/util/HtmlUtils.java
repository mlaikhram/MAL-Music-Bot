package util;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Logger logger = LoggerFactory.getLogger(HtmlUtils.class);

    public static Document getMALPage(String malUrl, long malId) throws Exception {
        try {
            return Jsoup.parse(Jsoup.connect(malUrl.replace("{id}", malId + "")).execute().body());
        }
        catch (HttpStatusException e) {
            throw new Exception(e.getMessage() + ": " + e.getStatusCode() + " for " + e.getUrl() + (e.getStatusCode() / 100 == 5 ? ". MAL might be having server issues" : ""), e);
        }
    }

    public static String getEnglishTitleFromMAL(Document doc, String defaultTitle) throws Exception {
        try {
            String possibleEnglishTitle = doc.getElementById("content")
                    .getElementsByTag("table").first()
                    .getElementsByTag("tbody").first()
                    .getElementsByTag("tr").first()
                    .getElementsByClass("borderClass").first()
                    .getElementsByTag("div").first()
                    .getElementsByClass("spaceit_pad").first()
                    .html();

            if (possibleEnglishTitle.contains("English")) {
                return Parser.unescapeEntities(possibleEnglishTitle.replace("<span class=\"dark_text\">English:</span>", ""), false).trim();
            } else {
                return defaultTitle;
            }
        }
        catch (Exception e) {
            logger.error(doc.html());
            throw new Exception("Failed to parse MAL's website for " + defaultTitle + ". MAL might be having server issues", e);
        }
    }

    public static Set<String> getSongsFromMAL(Document doc) throws Exception {
        try {
            Set<String> songs = new HashSet<>();
            Elements unparsedSongs = doc.getElementsByClass("theme-song");
            if (unparsedSongs.size() > 0) {
                Pattern regexWithEpisodes = Pattern.compile("(#[0-9]+: )?\"(.+?)( \\(.+\\))?\" by .+( \\(ep[s]? ([0-9]+(\\-[0-9]+)?(, )?)+\\))?"); // excludes japanese song name and artist name to (hopefully) refine search
                logger.info("songs:");
                for (Element song : unparsedSongs) {
                    logger.info(song.html());
                    Matcher matcher = regexWithEpisodes.matcher(song.html());
                    if (matcher.matches()) {
                        songs.add(Parser.unescapeEntities(matcher.group(2).replace("\"", ""), false));
                    }
                }
            }
            return songs;
        }
        catch (Exception e) {
            logger.error(doc.html());
            throw new Exception("Failed to parse MAL's website for songs. MAL might be having server issues", e);
        }
    }
}
