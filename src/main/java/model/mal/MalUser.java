package model.mal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import util.JikanUtils;

import java.util.*;

public class MalUser implements Comparable<MalUser> {

    private static final Logger logger = LoggerFactory.getLogger(MalUser.class);

    private static final long MAX_SHOW_RESPONSE_COUNT = 300;

    private static final Map<Long, AnimeObject> animeLibrary = new HashMap<>();

    private String username;
    private Set<AnimeObject> animeList;
    private Map<String, Long> showCounts;

    public MalUser(String user) {
        this.username = user;
        animeList = new HashSet<>();
        showCounts = new HashMap<>();
    }

    public MalUser(JikanUserResponse response) {
        this.username = response.getUsername();
        animeList = new HashSet<>();
        showCounts = Map.of("completed", response.getAnimeStats().getCompleted(), "watching", response.getAnimeStats().getWatching());
    }

    public String getUsername() {
        return username;
    }

    public void populate(String jikanUrl) {
        logger.info("populating " + username + "'s list");
        for (String status : Arrays.asList("completed", "watching")) {
            List<AnimeObject> responseAnime;
            for (int page = 1; page <= (showCounts.get(status) / MAX_SHOW_RESPONSE_COUNT) + 1; ++page) {
                logger.info("trying page " + page);
                ResponseEntity<JikanListResponse> response = JikanUtils.getList(jikanUrl, username, status, page, true);
                responseAnime = response.getBody().getAnime();
                for (AnimeObject animeObject : responseAnime) {
                    if (!animeLibrary.containsKey(animeObject.getMalId())) {
                        animeLibrary.put(animeObject.getMalId(), animeObject);
                    }
                    animeList.add(animeLibrary.get(animeObject.getMalId()));
                }
            }
        }
    }

    public Set<AnimeObject> getAnimeList() {
        return animeList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MalUser)) {
            return false;
        }
        MalUser c = (MalUser) o;
        return username.equals(c.username);
    }

    @Override
    public int compareTo(@NotNull MalUser o) {
        return username.toLowerCase().compareTo(o.username.toLowerCase());
    }

    @Override
    public int hashCode() {
        return username.toLowerCase().hashCode();
    }
}
