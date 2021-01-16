package model;

public class MalSong {

    private final AnimeObject anime;
    private final String url;
    private final long playedFromMessageChannelId;

    public MalSong(AnimeObject anime, String url, long playedFromMessageChannelId) {
        this.anime = anime;
        this.url = url;
        this.playedFromMessageChannelId = playedFromMessageChannelId;
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
