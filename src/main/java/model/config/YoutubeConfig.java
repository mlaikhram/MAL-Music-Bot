package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YoutubeConfig {

    @JsonProperty
    private String token;

    @JsonProperty
    private String url;

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }
}
