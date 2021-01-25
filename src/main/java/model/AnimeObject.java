package model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import util.AnimeType;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeObject implements Comparable<AnimeObject> {

    @JsonAlias("mal_id")
    private long malId;
    private String title;
    private String englishTitle;
    private Set<String> songs;
    private AnimeType type;

    public long getMalId() {
        return malId;
    }

    public void setMalId(long malId) {
        this.malId = malId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public Set<String> getSongs() {
        return songs;
    }

    public void setSongs(Set<String> songs) {
        this.songs = songs;
    }

    public AnimeType getType() {
        return type;
    }

    public void setType(AnimeType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AnimeObject)) {
            return false;
        }
        AnimeObject c = (AnimeObject) o;
        return malId == c.malId;
    }

    @Override
    public int compareTo(@NotNull AnimeObject o) {
        return Long.compare(malId, o.malId);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(malId);
    }
}
