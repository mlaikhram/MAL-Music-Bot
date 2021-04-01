package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JikanConfig {

    @JsonProperty
    private String url;

    public String getUrl() {
        return url;
    }
}
