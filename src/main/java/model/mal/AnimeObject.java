package model.mal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AnimeType;
import util.JikanUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeObject implements Comparable<AnimeObject> {

    private static final Logger logger = LoggerFactory.getLogger(AnimeObject.class);

    @JsonAlias("mal_id")
    private long malId;
    private String url;
    @JsonAlias("image_url")
    private String image;
    private AnimeType type;

    private String englishTitle;
    private Set<String> otherTitles;
    private Set<String> songs;

    public long getMalId() {
        return malId;
    }

    public void setMalId(long malId) {
        this.malId = malId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public AnimeType getType() {
        return type;
    }

    public void setType(AnimeType type) {
        this.type = type;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public Set<String> getOtherTitles() {
        return otherTitles;
    }

    public void setOtherTitles(Set<String> otherTitles) {
        this.otherTitles = otherTitles;
    }

    public Set<String> getSongs() {
        return songs;
    }

    public void setSongs(Set<String> songs) {
        this.songs = songs;
    }

    public void applyJikan(JikanAnimeResponse jikan) {
        this.englishTitle = jikan.getEnglishTitle();
        this.otherTitles = new LinkedHashSet<>();
        if (this.englishTitle == null) {
            this.englishTitle = jikan.getTitle();
        }
        else if (!jikan.getTitle().equals(this.englishTitle)) {
            this.otherTitles.add(jikan.getTitle());
        }
        this.otherTitles.add(jikan.getJapaneseTitle());
        for (String otherTitle : jikan.getOtherTitles()) {
            if (!otherTitle.equals(this.englishTitle)) {
                this.otherTitles.add(otherTitle);
            }
        }

        this.songs = new HashSet<>();
        Pattern regexWithEpisodes = Pattern.compile("(#?[0-9]+: ?)?\"?(.+?)( \\(.+\\))?\"? ?by ?.+( \\(ep[s]? ([0-9]+(\\-[0-9]+)?(, )?)+\\))?"); // excludes japanese song name and artist name to (hopefully) refine search
        logger.info("songs:");
        for (String song : jikan.getOpeningSongs()) {
            logger.info(song);
            Matcher matcher = regexWithEpisodes.matcher(song);
            if (matcher.matches()) {
                this.songs.add(matcher.group(2).replace("\"", "").replaceAll("(^\\h*)|(\\h*$)","").strip());
            }
        }
        for (String song : jikan.getEndingSongs()) {
            logger.info(song);
            Matcher matcher = regexWithEpisodes.matcher(song);
            if (matcher.matches()) {
                this.songs.add(matcher.group(2).replace("\"", "").replaceAll("(^\\h*)|(\\h*$)","").strip());
            }
        }
        logger.info("post add:");
        for (String song : this.songs) {
            logger.info(song);
        }
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
