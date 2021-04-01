package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfig {

    @JsonProperty
    private String path;

    public String getPath() {
        return path;
    }
}
