package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DiscordConfig {

    @JsonProperty
    private String token;

    @JsonProperty
    private List<String> fixers;

    public String getToken() {
        return token;
    }

    public List<String> getFixers() {
        return fixers;
    }
}
