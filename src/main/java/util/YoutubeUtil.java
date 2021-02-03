package util;

import audio.MusicSession;
import audio.SessionManager;
import model.AnimeObject;
import model.MalSong;
import model.YoutubeResponse;
import model.YoutubeVideo;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class YoutubeUtil {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeUtil.class);

    private static final String VIDEO_URL = "https://www.youtube.com/watch?v={id}";
    private static final String EMPTY_SEARCH = "[no results]";

    public static MalSong pickSong(MusicSession session, long channelId, String url, AnimeObject anime) throws Exception {
        Set<String> filteredSongs = session.filterRecent(anime.getSongs());
        String song = (String) filteredSongs.toArray()[new Random().nextInt(filteredSongs.size())];
        String ytid = DBUtils.getSongId(song);
        if (ytid == null) {
            String query = anime.getEnglishTitle() + " " + song + " song";
            logger.info("couldn't find song in db, searching youtube for: " + query);
            RestTemplate template = new RestTemplate();
            ResponseEntity<YoutubeResponse> response = template.getForEntity(url, YoutubeResponse.class, Collections.singletonMap("query", query));
            YoutubeVideo video = filterResults(anime, response.getBody().getVideos());
            if (video == null) {
                DBUtils.addSong(song, EMPTY_SEARCH);
                throw new Exception("I couldn't find any songs for " + anime.getEnglishTitle());
            }
            ytid = video.getId().getVideoId();
            DBUtils.addSong(song, ytid);
        }
        else if (ytid.equals(EMPTY_SEARCH)) {
            throw new Exception("I couldn't find any songs for " + anime.getEnglishTitle());
        }
        return new MalSong(song, anime, VIDEO_URL.replace("{id}", ytid), channelId);
    }

    private static YoutubeVideo filterResults(AnimeObject anime, List<YoutubeVideo> videos) throws Exception {
        List<YoutubeVideo> hardFiltered = videos.stream().filter((video) -> !containsAny(anime, video.getSnippet().getTitle().toLowerCase(), hardFilters)).collect(Collectors.toList());
        if (!hardFiltered.isEmpty()) {
            logger.info("getting filtered vid (" + hardFiltered.size() + "/" + videos.size() + ")");
            return hardFiltered.stream().filter((video) -> !containsAny(anime, video.getSnippet().getTitle().toLowerCase(), softFilters)).findFirst().orElse(hardFiltered.get(0));
        }
        else if (!videos.isEmpty()) {
            logger.info("filters didn't work, enjoy a jank vid");
            return videos.get(0);
        }
        else {
            return null;
        }
    }

    private static boolean containsAny(AnimeObject animeException, String phrase, String[] keywords) {
        for (String keyword : keywords) {
            if (phrase.contains(keyword) && !animeException.getTitle().toLowerCase().contains(keyword) && !animeException.getEnglishTitle().toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] hardFilters = {
            "cover",
            "but",
            "remix",
            "mix",
            "nightcore",
            "synthesia",
            "montage",
            "amv",
            "pmv",
            "piano",
            "guitar",
            "orchestra",
            "a capella",
            "music box",
            "eng",
            "english",
            "instrumental",
            "pv",
            "preview",
            "trailer",
            "live"
    };

    private static final String[] softFilters = {
            "full",
            "extended",
            "osu",
            "bandori"
    };
}
