package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeVideo {

    private YoutubeId id;
    private YoutubeSnippet snippet;

    public YoutubeId getId() {
        return id;
    }

    public void setId(YoutubeId id) {
        this.id = id;
    }

    public YoutubeSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(YoutubeSnippet snippet) {
        this.snippet = snippet;
    }
}
