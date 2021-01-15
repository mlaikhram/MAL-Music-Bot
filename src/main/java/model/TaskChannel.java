package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskChannel {

    @JsonProperty
    private long id;

    @JsonProperty
    private long emote;


    public long getId() {
        return id;
    }

    public long getEmote() {
        return emote;
    }
}
