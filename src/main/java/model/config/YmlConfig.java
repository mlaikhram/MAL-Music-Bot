package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YmlConfig {

    @JsonProperty
    private DiscordConfig discord;

    @JsonProperty
    private JikanConfig jikan;

    @JsonProperty
    private YoutubeConfig youtube;

    @JsonProperty
    private DbConfig db;

    @JsonProperty
    private ArrayList<String> voiceLines;

    public DiscordConfig getDiscord() {
        return discord;
    }

    public JikanConfig getJikan() {
        return jikan;
    }

    public YoutubeConfig getYoutube() {
        return youtube;
    }

    public DbConfig getDb() {
        return db;
    }

    public String getRandomVoiceLine() {
        if (voiceLines.isEmpty()) {
            return "";
        }
        return voiceLines.get(new Random().nextInt(voiceLines.size())) + " ";
    }
}
