package model;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MalUser implements Comparable<MalUser> {

    private static final Logger logger = LoggerFactory.getLogger(MalUser.class);

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
        for (String url : jikanUrls) {
            ResponseEntity<JikanResponse> response = template.getForEntity(url, JikanResponse.class, Collections.singletonMap("user", username));
            animeList.addAll(response.getBody().getAnime());
//            for (int i = 0; i < 5; ++i) {
//                if (i >= response.getBody().getAnime().size()) {
//                    break;
//                }
//                logger.info(response.getBody().getAnime().get(i).getMalId() + ": " + response.getBody().getAnime().get(i).getTitle());
//            }
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
}
