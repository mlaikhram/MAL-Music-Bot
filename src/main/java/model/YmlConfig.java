package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class YmlConfig {

    @JsonProperty
    private String token;

    @JsonProperty
    private List<String> jikan;

    @JsonProperty
    private String mal;

    @JsonProperty
    private String yt;


    public String getToken() {
        return token;
    }

    public List<String> getJikan() {
        return jikan;
    }

    public String getMal() {
        return mal;
    }

    public String getYt() {
        return yt;
    }
}
