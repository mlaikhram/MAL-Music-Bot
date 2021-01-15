package util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AnimeType {
    TV,
    OVA,
    @JsonProperty("Movie") MOVIE,
    @JsonProperty("Special") SPECIAL,
    ONA,
    @JsonProperty("Music") MUSIC,
    @JsonProperty("Unknown") UNKNOWN
}
