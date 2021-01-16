package util;

import model.YoutubeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class YoutubeUtil {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeUtil.class);

    private static final String VIDEO_URL = "https://www.youtube.com/watch?v={id}";

    public static String pickSong(String url, Collection<String> songs) {
        String song = (String)songs.toArray()[new Random().nextInt(songs.size())];
        String query = song + " opening ending song TV Size";
        logger.info("searching for: " + query);
        RestTemplate template = new RestTemplate();
        ResponseEntity<YoutubeResponse> response = template.getForEntity(url, YoutubeResponse.class, Collections.singletonMap("query", query));
        return VIDEO_URL.replace("{id}", response.getBody().getVideos().get(0).getId().getVideoId()); // TODO: safety check if 0 results found
    }
}
