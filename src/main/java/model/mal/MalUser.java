package model.mal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class MalUser implements Comparable<MalUser> {

    private static final Logger logger = LoggerFactory.getLogger(MalUser.class);

    private static final Map<Long, AnimeObject> animeLibrary = new HashMap<>();

    private String username;
    private Set<AnimeObject> animeList;

    public MalUser(String username) {
        this.username = username;
        animeList = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public void populate(List<String> jikanUrls) {
        RestTemplate template = new RestTemplate();
        logger.info("populating " + username + "'s list");
        for (String url : jikanUrls) {
            ResponseEntity<JikanResponse> response = template.getForEntity(url, JikanResponse.class, Collections.singletonMap("user", username));
            for (AnimeObject animeObject : response.getBody().getAnime()) {
                if (!animeLibrary.containsKey(animeObject.getMalId())) {
                    animeLibrary.put(animeObject.getMalId(), animeObject);
                }
                animeList.add(animeLibrary.get(animeObject.getMalId()));
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
