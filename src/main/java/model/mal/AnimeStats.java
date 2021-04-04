package model.mal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeStats {

    private long completed;
    private long watching;

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getWatching() {
        return watching;
    }

    public void setWatching(long watching) {
        this.watching = watching;
    }
}
