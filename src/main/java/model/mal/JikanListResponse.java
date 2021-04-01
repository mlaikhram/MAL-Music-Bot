package model.mal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JikanListResponse {

    private String error;
    private List<AnimeObject> anime;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<AnimeObject> getAnime() {
        return anime;
    }

    public void setAnime(List<AnimeObject> anime) {
        this.anime = anime;
    }
}
