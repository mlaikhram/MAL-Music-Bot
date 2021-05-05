package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import listener.MalMusicListener;
import model.config.YmlConfig;
import model.mal.MalSong;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DBUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


public class SessionManager {
    private static SessionManager INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private MalMusicListener listener;
    private YmlConfig config;
    private final Map<Long, MusicSession> musicSessions;
    private final AudioPlayerManager audioPlayerManager;

    private SessionManager() {
        this.musicSessions = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public void init(MalMusicListener listener, YmlConfig config) {
        this.listener = listener;
        this.config = config;
    }

    public MusicSession getMusicSession(Guild guild) {
        return this.musicSessions.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final MusicSession musicSession = new MusicSession(this.listener, this.config, guild.getIdLong(), this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(musicSession.getAudioSendHandler());

            return musicSession;
        });
    }

    public void loadAndPlay(Guild guild, MessageChannel channel, final MalSong song, Consumer<String> onSongFailed) {
        final MusicSession musicSession = this.getMusicSession(guild);

        this.audioPlayerManager.loadItemOrdered(musicSession, song.getUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicSession.scheduler.queue(track);
                musicSession.setCurrentSong(song);
                logger.info("now playing: " + song.getUrl());
                channel.sendMessage("Try this one!").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                //
            }

            @Override
            public void noMatches() {
                logger.info("song doesn't exist anymore: " + song.getUrl());
                try {
                    DBUtils.deleteSong(song.getAnime().getEnglishTitle(), song.getName());
                    onSongFailed.accept(song.getName());
                }
                catch (Exception e) {
                    channel.sendMessage("Something's not right... " + e.getMessage()).queue();
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
                if (exception.severity == FriendlyException.Severity.COMMON) {
                    logger.info("Song is private or copyright claimed: " + song.getUrl());
                    try {
                        DBUtils.deleteSong(song.getAnime().getEnglishTitle(), song.getName());
                        onSongFailed.accept(song.getName());
                    }
                    catch (Exception e) {
                        channel.sendMessage("Something's not right... " + e.getMessage()).queue();
                    }
                }
                else {
                    channel.sendMessage("Something's not right... " + exception.getMessage()).queue();
                }
            }
        });
    }

    public static SessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }

        return INSTANCE;
    }

}
