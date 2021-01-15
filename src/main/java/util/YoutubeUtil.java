package util;

import model.JikanResponse;
import model.YoutubeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class YoutubeUtil {

    private static final String VIDEO_URL = "https://www.youtube.com/watch?v={id}";

    public static String pickSong(String url, Collection<String> songs) {
        String song = (String)songs.toArray()[new Random().nextInt(songs.size())];
        RestTemplate template = new RestTemplate();
        ResponseEntity<YoutubeResponse> response = template.getForEntity(url, YoutubeResponse.class, Collections.singletonMap("query", song));
        return VIDEO_URL.replace("{id}", response.getBody().getVideos().get(0).getId().getVideoId());
    }
}
