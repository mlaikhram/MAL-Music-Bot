package util;

import model.AnimeObject;
import model.YoutubeResponse;
import model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class YoutubeUtil {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeUtil.class);

    private static final String VIDEO_URL = "https://www.youtube.com/watch?v={id}";

    public static String pickSong(String url, AnimeObject anime) throws Exception {
        String song = (String)anime.getSongs().toArray()[new Random().nextInt(anime.getSongs().size())];
        String query = anime.getEnglishTitle() + " " + song + " song";
        logger.info("searching for: " + query);
        RestTemplate template = new RestTemplate();
        ResponseEntity<YoutubeResponse> response = template.getForEntity(url, YoutubeResponse.class, Collections.singletonMap("query", query));

        return VIDEO_URL.replace("{id}", filterResults(anime, response.getBody().getVideos()).getId().getVideoId()); // TODO: safety check if 0 results found
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
            throw new Exception("I couldn't find any songs for " + anime.getEnglishTitle());
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
            "remix",
            "piano",
            "guitar",
            "eng",
            "english",
            "instrumental",
            "pv",
            "preview"
    };

    private static final String[] softFilters = {
            "full"
    };
}
