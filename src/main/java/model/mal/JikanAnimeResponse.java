package model.mal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JikanAnimeResponse {

    @JsonAlias("mal_id")
    private long malId;
    private String url;
    @JsonAlias("image_url")
    private String image;

    private String title;
    @JsonAlias("title_english")
    private String englishTitle;
    @JsonAlias("title_japanese")
    private String japaneseTitle;
    @JsonAlias("title_synonyms")
    private List<String> otherTitles;

    @JsonAlias("opening_themes")
    private List<String> openingSongs;
    @JsonAlias("ending_themes")
    private List<String> endingSongs;

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

    public String getJapaneseTitle() {
        return japaneseTitle;
    }

    public void setJapaneseTitle(String japaneseTitle) {
        this.japaneseTitle = japaneseTitle;
    }

    public List<String> getOtherTitles() {
        return otherTitles;
    }

    public void setOtherTitles(List<String> otherTitles) {
        this.otherTitles = otherTitles;
    }

    public List<String> getOpeningSongs() {
        return openingSongs;
    }

    public void setOpeningSongs(List<String> openingSongs) {
        this.openingSongs = openingSongs;
    }

    public List<String> getEndingSongs() {
        return endingSongs;
    }

    public void setEndingSongs(List<String> endingSongs) {
        this.endingSongs = endingSongs;
    }
}
