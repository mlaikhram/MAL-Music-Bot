package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import listener.MalMusicListener;
import model.AnimeObject;
import model.MalSong;
import model.MalUser;
import model.YmlConfig;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AnimeType;
import util.CombineMethod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicSession {

    private static final Logger logger = LoggerFactory.getLogger(MusicSession.class);

    private static final int MAX_RECENT_ANIME = 20;

    private final YmlConfig config;
    private final Set<MalUser> malUsers;
    private final LinkedHashSet<Long> recentAnime;

    private MalSong currentSong;
    private String lastCommand;

    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioSendHandler audioSendHandler;

    public MusicSession(MalMusicListener listener, YmlConfig config, long guildId, AudioPlayerManager manager) {
        this.malUsers = new TreeSet<>();
        this.config = config;
        this.recentAnime = new LinkedHashSet<>();

        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(listener, guildId, this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.audioSendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void addUser(String username) throws Exception {
        MalUser newUser = new MalUser(username);
        if (malUsers.contains(newUser)) {
            throw new Exception(username + " is already added to this session!");
        }
        else {
            try {
                malUsers.add(newUser);
                newUser.populate(config.getJikan());
            }
            catch (Exception e) {
                throw new Exception("could not find " + username + "'s list on MAL", e);
            }
        }
    }

    public boolean removeUser(String username) {
        return malUsers.remove(new MalUser(username));
    }

    public AnimeObject selectAnime(CombineMethod combineMethod, Collection<AnimeType> animeTypes) {
        for (MalUser user : malUsers) {
            logger.info(user.getUsername() + ": " + user.getAnimeList().size());
        }
        List<AnimeObject> collection = getCombinedList(combineMethod).filter((animeObject) -> animeTypes.isEmpty() || animeTypes.contains(animeObject.getType())).filter((animeObject) -> !recentAnime.contains(animeObject.getMalId())).collect(Collectors.toList());
        logger.info("total: " + collection.size());
        if (collection.size() > 0) {
            AnimeObject nextAnime = collection.get(new Random().nextInt(collection.size()));
            recentAnime.add(nextAnime.getMalId());
            if (recentAnime.size() > MAX_RECENT_ANIME) {
                Iterator<Long> itr = recentAnime.iterator();
                if (itr.hasNext()) {
                    recentAnime.remove(itr.next());
                }
            }
            return nextAnime;
        }
        else if (!recentAnime.isEmpty()) {
            logger.info("ran out of anime, resetting list");
            recentAnime.clear();
            return selectAnime(combineMethod, animeTypes);
        }
        else {
            return null;
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
}
