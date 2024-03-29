package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import listener.MalMusicListener;
import model.mal.AnimeObject;
import model.mal.JikanUserResponse;
import model.mal.MalSong;
import model.mal.MalUser;
import model.config.YmlConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import util.AnimeType;
import util.CombineMethod;
import util.JikanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MusicSession {

    private static final Logger logger = LoggerFactory.getLogger(MusicSession.class);

    private static final int MAX_RECENT_SONGS = 100;

    private final YmlConfig config;
    private final Set<MalUser> malUsers;
    private final LinkedHashSet<String> recentSongs;

    private MalSong currentSong;
    private String lastCommand;

    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioSendHandler audioSendHandler;

    public MusicSession(MalMusicListener listener, YmlConfig config, long guildId, AudioPlayerManager manager) {
        this.malUsers = new TreeSet<>();
        this.config = config;
        this.recentSongs = new LinkedHashSet<>();

        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(listener, guildId, this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.audioSendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public JikanUserResponse addUser(String url, Message message, EmbedBuilder embedBuilder, String username) throws Exception {
        ResponseEntity<JikanUserResponse> response = JikanUtils.getUser(url, username, true);
        MalUser newUser = new MalUser(response.getBody());
        embedBuilder.setTitle(response.getBody().getUsername());
        embedBuilder.setThumbnail(response.getBody().getImage());
        if (malUsers.contains(newUser)) {
            throw new Exception("This user is already added!");
        }
        else {
            try {
                malUsers.add(newUser);
                newUser.populate(config.getJikan().getUrl(), message, embedBuilder);
                return response.getBody();
            }
            catch (HttpServerErrorException e) {
                malUsers.remove(newUser);
                throw new Exception("Could not add user: " + e.getMessage() + (e.getStatusCode().value() / 100 == 5 ? ". MAL might be having server issues" : ""), e);
            }
            catch (Exception e) {
                malUsers.remove(newUser);
                throw new Exception("could not add user: " + e.getMessage(), e);
            }
        }
    }

    public boolean removeUser(String user) {
        return malUsers.remove(new MalUser(user));
    }

    public AnimeObject selectAnime(CombineMethod combineMethod, Collection<AnimeType> animeTypes) {
        for (MalUser user : malUsers) {
            logger.info(user.getUsername() + ": " + user.getAnimeList().size());
        }
        List<AnimeObject> collection = getCombinedList(combineMethod).filter((animeObject) -> animeTypes.isEmpty() || animeTypes.contains(animeObject.getType())).filter((animeObject) -> (animeObject.getSongs() == null || !recentSongs.containsAll(animeObject.getSongs()))).collect(Collectors.toList());
        logger.info("total: " + collection.size());
        if (collection.size() > 0) {
            AnimeObject nextAnime = collection.get(new Random().nextInt(collection.size()));
            return nextAnime;
        }
        else if (!recentSongs.isEmpty()) {
            logger.info("ran out of anime, resetting list");
            recentSongs.clear();
            return selectAnime(combineMethod, animeTypes);
        }
        else {
            return null;
        }
    }

    public void addToRecent(String songName) {
        recentSongs.add(songName);
        if (recentSongs.size() > MAX_RECENT_SONGS) {
            Iterator<String> itr = recentSongs.iterator();
            if (itr.hasNext()) {
                recentSongs.remove(itr.next());
            }
        }
    }

    private Stream<AnimeObject> getCombinedList(CombineMethod combineMethod) {
        switch (combineMethod) {
            case UNIFORM:
                return malUsers.stream().map((user) -> user.getAnimeList()).flatMap(Set::stream).collect(Collectors.toSet()).stream();

            case BALANCED:
                int selectedUserIndex = new Random().nextInt(malUsers.size());
                int i = 0;
                for (MalUser malUser : malUsers) {
                    if (i == selectedUserIndex) {
                        return malUser.getAnimeList().stream();
                    }
                    ++i;
                }

            case OVERLAP:
                List<AnimeObject> woAnimeList = new LinkedList<>();
                for (MalUser malUser : malUsers) {
                    for (AnimeObject anime : malUser.getAnimeList()) {
                        if (malUsers.stream().filter((otherUser) -> !otherUser.getUsername().equals(malUser.getUsername())).anyMatch((otherUser) -> otherUser.getAnimeList().contains(anime))) {
                            woAnimeList.add(anime);
                        }
                    }
                }
                return woAnimeList.stream();

            case INTERSECT:
                Set<AnimeObject> soAnimeList = null;
                for (MalUser malUser : malUsers) {
                    if (soAnimeList == null) {
                        soAnimeList = new HashSet<>(malUser.getAnimeList());
                    }
                    else {
                        soAnimeList.retainAll(malUser.getAnimeList());
                    }
                }
                return soAnimeList.stream();

            case WEIGHTED:
            default:
                return malUsers.stream().map((user) -> user.getAnimeList()).flatMap(Set::stream);
        }
    }

    public Set<MalUser> getMalUsers() {
        return malUsers;
    }

    public AudioSendHandler getAudioSendHandler() {
        return audioSendHandler;
    }

    public MalSong getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(MalSong song) {
        this.currentSong = song;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    public Set<String> filterRecent(Collection<String> songNames) {
        return songNames.stream().filter((songName) -> !recentSongs.contains(songName)).collect(Collectors.toSet());
    }
}
