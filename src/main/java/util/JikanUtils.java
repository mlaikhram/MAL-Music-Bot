package util;

import model.mal.JikanAnimeResponse;
import model.mal.JikanListResponse;
import model.mal.JikanUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


public class JikanUtils {

    private static final Logger logger = LoggerFactory.getLogger(JikanUtils.class);

    private static final int DELAY_MILLISECONDS = 600;
    private static final int BULK_DELAY_MILLISECONDS = 4000;
    private static long LAST_RESPONSE = -1;

    private static final String LIST_ENDPOINT = "/user/{user}/animelist/{status}/{page}";
    private static final String USER_ENDPOINT = "/user/{user}";
    private static final String ANIME_ENDPOINT = "/anime/{id}";

    public static ResponseEntity<JikanListResponse> getList(String baseUrl, String user, String status, int page, boolean isBulk) {
        applyDelay(isBulk ? BULK_DELAY_MILLISECONDS : DELAY_MILLISECONDS);
        ResponseEntity<JikanListResponse> responseEntity = new RestTemplate().getForEntity(baseUrl + LIST_ENDPOINT, JikanListResponse.class, Map.of("user", user, "status", status, "page", page));
        LAST_RESPONSE = System.currentTimeMillis();
        return responseEntity;
    }

    public static ResponseEntity<JikanUserResponse> getUser(String baseUrl, String user, boolean isBulk) {
        applyDelay(isBulk ? BULK_DELAY_MILLISECONDS : DELAY_MILLISECONDS);
        ResponseEntity<JikanUserResponse> responseEntity = new RestTemplate().getForEntity(baseUrl + USER_ENDPOINT, JikanUserResponse.class, Map.of("user", user));
        LAST_RESPONSE = System.currentTimeMillis();
        return responseEntity;
    }

    public static ResponseEntity<JikanAnimeResponse> getAnime(String baseUrl, long id, boolean isBulk) {
        applyDelay(isBulk ? BULK_DELAY_MILLISECONDS : DELAY_MILLISECONDS);
        ResponseEntity<JikanAnimeResponse> responseEntity = new RestTemplate().getForEntity(baseUrl + ANIME_ENDPOINT, JikanAnimeResponse.class, Map.of("id", id));
        LAST_RESPONSE = System.currentTimeMillis();
        return responseEntity;
    }

    private static void applyDelay(long delay) {
        try {
            long millisecondsSinceLastCall = System.currentTimeMillis() - LAST_RESPONSE;
            if (millisecondsSinceLastCall < delay) {
                logger.info("delaying call by " + (delay - millisecondsSinceLastCall));
                Thread.sleep(delay - millisecondsSinceLastCall);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
