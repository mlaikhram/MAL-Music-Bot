package util;

import model.MusicSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Logger logger = LoggerFactory.getLogger(HtmlUtils.class);

    public static Set<String> getSongsFromMAL(String malUrl, long malId) throws IOException {
        Set<String> songs = new HashSet<>();
        Document doc = Jsoup.parse(Jsoup.connect(malUrl.replace("{id}", malId + "")).execute().body());
        logger.info("songs");
        Elements unparsedSongs = doc.getElementsByClass("theme-song");
        if (unparsedSongs.size() > 0) {
            Pattern regexWithEpisodes = Pattern.compile("(#[0-9]+: )?(\".+\" by .+)( \\(ep[s]? [0-9]+(\\-[0-9]+)?\\))");
            Pattern regexWithoutEpisodes = Pattern.compile("(#[0-9]+: )?(\".+\" by .+)");
            for (Element song : unparsedSongs) {
                logger.info(song.html());
                Matcher matcher = regexWithEpisodes.matcher(song.html());
                if (matcher.matches()) {
                    songs.add(matcher.group(2));
                }
                else {
                    matcher = regexWithoutEpisodes.matcher(song.html());
                    if (matcher.matches()) {
                        songs.add(matcher.group(2));
                    }
                }
            }
        }
        return songs;
    }
}
