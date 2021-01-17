package util;

import model.AnimeObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

//    private static final Logger logger = LoggerFactory.getLogger(HtmlUtils.class);

    public static Document getMALPage(String malUrl, long malId) throws IOException {
        return Jsoup.parse(Jsoup.connect(malUrl.replace("{id}", malId + "")).execute().body());
    }

    public static String getEnglishTitleFromMAL(Document doc, String defaultTitle) {
        String possibleEnglishTitle = doc.getElementById("content")
                .getElementsByTag("table").first()
                .getElementsByTag("tbody").first()
                .getElementsByTag("tr").first()
                .getElementsByClass("borderClass").first()
                .getElementsByTag("div").first()
                .getElementsByClass("spaceit_pad").first()
                .html();

        if (possibleEnglishTitle.contains("English")) {
            return possibleEnglishTitle.replace("<span class=\"dark_text\">English:</span>", "").replace("&amp;", "&").trim();
        }
        else {
            return defaultTitle;
        }
    }

    public static Set<String> getSongsFromMAL(Document doc) {
        Set<String> songs = new HashSet<>();
//        Document doc = Jsoup.parse(Jsoup.connect(malUrl.replace("{id}", malId + "")).execute().body());
        Elements unparsedSongs = doc.getElementsByClass("theme-song");
        if (unparsedSongs.size() > 0) {
//            Pattern regexWithEpisodes = Pattern.compile("(#[0-9]+: )?(\".+\" by .+)( \\(ep[s]? ([0-9]+(\\-[0-9]+)?(, )?)+\\))");
//            Pattern regexWithoutEpisodes = Pattern.compile("(#[0-9]+: )?(\".+\" by .+)");
            Pattern regexWithEpisodes = Pattern.compile("(#[0-9]+: )?\"(.+)( \\(.+\\))\" by .+( \\(ep[s]? ([0-9]+(\\-[0-9]+)?(, )?)+\\))"); // excludes japanese song name and artist name to (hopefully) refine search
            Pattern regexWithoutEpisodes = Pattern.compile("(#[0-9]+: )?\"(.+)( \\(.+\\))\" by .+");
            for (Element song : unparsedSongs) {
//                logger.info(song.html());
                Matcher matcher = regexWithEpisodes.matcher(song.html());
                if (matcher.matches()) {
                    songs.add(matcher.group(2).replace("\"", ""));
                }
                else {
                    matcher = regexWithoutEpisodes.matcher(song.html());
                    if (matcher.matches()) {
                        songs.add(matcher.group(2).replace("\"", ""));
                    }
                }
            }
        }
        return songs;
    }
}
