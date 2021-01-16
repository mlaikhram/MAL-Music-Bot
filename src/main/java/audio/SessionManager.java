package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import listener.MalMusicListener;
import model.YmlConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager INSTANCE;

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

    public void loadAndPlay(Guild guild, MessageChannel channel, String trackUrl) {
        final MusicSession musicSession = this.getMusicSession(guild);

        this.audioPlayerManager.loadItemOrdered(musicSession, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicSession.scheduler.queue(track);
                channel.sendMessage("Try this one!").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                //
            }

            @Override
            public void noMatches() {
                //
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                //
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
