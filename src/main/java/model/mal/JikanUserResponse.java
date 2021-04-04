package model.mal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JikanUserResponse {

    private String username;
    private String url;
    @JsonAlias("image_url")
    private String image;

    @JsonAlias("anime_stats")
    private AnimeStats animeStats;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public AnimeStats getAnimeStats() {
        return animeStats;
    }

    public void setAnimeStats(AnimeStats animeStats) {
        this.animeStats = animeStats;
    }
}
