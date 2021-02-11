package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YmlConfig {

    @JsonProperty
    private String token;

    @JsonProperty
    private List<String> jikan;

    @JsonProperty
    private String mal;

    @JsonProperty
    private String yt;

    @JsonProperty
    private String dbPath;

    @JsonProperty
    private List<String> fixers;

    @JsonProperty
    private ArrayList<String> voiceLines;

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

    public String getDbPath() {
        return dbPath;
    }

    public List<String> getFixers() {
        return fixers;
    }

    public String getRandomVoiceLine() {
        if (voiceLines.isEmpty()) {
            return "";
        }
        return voiceLines.get(new Random().nextInt(voiceLines.size())) + " ";
    }
}
