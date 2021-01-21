package model;

public class MalSong {

    private final String name;
    private final AnimeObject anime;
    private final String url;
    private final long playedFromMessageChannelId;

    public MalSong(String name, AnimeObject anime, String url, long playedFromMessageChannelId) {
        this.name = name;
        this.anime = anime;
        this.url = url;
        this.playedFromMessageChannelId = playedFromMessageChannelId;
    }

    public String getName() {
        return name;
    }

    public AnimeObject getAnime() {
        return anime;
    }

    public String getUrl() {
        return url;
    }

    public long getPlayedFromMessageChannelId() {
        return playedFromMessageChannelId;
    }
}
