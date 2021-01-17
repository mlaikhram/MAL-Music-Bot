package listener;

import audio.SessionManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import model.AnimeObject;
import model.MalSong;
import model.MalUser;
import audio.MusicSession;
import model.YmlConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import javax.annotation.Nonnull;
import java.util.*;

public class MalMusicListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MalMusicListener.class);

    private JDA jda;
    private YmlConfig config;

    public MalMusicListener(YmlConfig config) {
        SessionManager.getInstance().init(this, config);
        this.config = config;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String myID = jda.getSelfUser().getId();
        User author = event.getAuthor();
        Member member = event.getMember();
        MessageChannel sourceChannel = event.getChannel();
        Guild guild = event.getGuild();
        String rawMessage = event.getMessage().getContentRaw();
        String[] messageTokens = rawMessage.split("[ ]+");
        for (int i = 1; i < messageTokens.length; ++i) {
            messageTokens[i] = messageTokens[i].toLowerCase();
        }

        if (event.isFromType(ChannelType.TEXT)) {
            if (MessageUtils.isUserMention(messageTokens[0]) && MessageUtils.mentionToUserID(messageTokens[0]).toString().equals(myID)) {
                logger.info("message received from " + author + ": " + rawMessage);

                if (messageTokens.length <= 1 || (messageTokens.length >= 2 && messageTokens[1].equals("help"))) {
                    sourceChannel.sendMessage(MessageUtils.HELP_TEXT).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equals("add")) {
                    if (messageTokens.length > 2) {
                        StringBuilder message = new StringBuilder();
                        for (int i = 2; i < messageTokens.length; ++i) {
                            String user = messageTokens[i];
                            try {
                                SessionManager.getInstance().getMusicSession(guild).addUser(user);
                                message.append("Successfully added " + user + "!\n");
                            }
                            catch (Exception e) {
                                message.append("Failed to add " + user + ": " + e.getMessage() + '\n');
                                e.printStackTrace();
                            }
                        }
                        message.deleteCharAt(message.length() - 1);
                        sourceChannel.sendMessage(message.toString()).queue();
                    }
                    else {
                        sourceChannel.sendMessage("...Add who? You're gonna have to give me a MAL username (or more than one)").queue();
                    }
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equals("remove")) {
                    if (messageTokens.length > 2) {
                        StringBuilder message = new StringBuilder();
                        for (int i = 2; i < messageTokens.length; ++i) {
                            String user = messageTokens[i];
                            if (SessionManager.getInstance().getMusicSession(guild).removeUser(user)) {
                                message.append("Successfully removed " + user + "!\n");
                            }
                            else {
                                message.append("I don't even know who " + user + " is...\n");
                            }
                        }
                        message.deleteCharAt(message.length() - 1);
                        sourceChannel.sendMessage(message.toString()).queue();
                    }
                    else {
                        sourceChannel.sendMessage("...Remove who? You're gonna have to give me a MAL username (or more than one)").queue();
                    }
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equals("users")) {
                    StringBuilder message = new StringBuilder();
                    MusicSession currentSession = SessionManager.getInstance().getMusicSession(guild);
                    message.append("Current MAL Users: " + currentSession.getMalUsers().size() + '\n');
                    for (MalUser user : currentSession.getMalUsers()) {
                        message.append(user.getUsername() + '\n');
                    }
                    message.deleteCharAt(message.length() - 1);
                    sourceChannel.sendMessage(message.toString()).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equals("play")) {
                    if (SessionManager.getInstance().getMusicSession(guild).getCurrentSong() != null) {
                        sourceChannel.sendMessage("Can't you see I'm already playing a song??").queue();
                        return;
                    }
                    if (SessionManager.getInstance().getMusicSession(guild).getMalUsers().isEmpty()) {
                        sourceChannel.sendMessage("You haven't even given me any MAL users yet!").queue();
                        return;
                    }
                    if (member.getVoiceState().inVoiceChannel() && guild.getVoiceChannelById(member.getVoiceState().getChannel().getIdLong()) != null) {
                        VoiceChannel currentChannel = member.getVoiceState().getChannel();
                        CombineMethod combineMethod = CombineMethod.WEIGHTED;
                        Set<AnimeType> animeTypes = new HashSet<>();
                        try {
                            if (messageTokens.length >= 3) {
                                try {
                                    combineMethod = CombineMethod.valueOf(messageTokens[2].toUpperCase());
                                }
                                catch (IllegalArgumentException e) {
                                    throw new IllegalArgumentException("what is " + messageTokens[2] + "? It's not any combine method I've ever heard of...", e);
                                }
                            }
                            if (messageTokens.length >= 4) {
                                int i = 3;
                                try {
                                    for (; i < messageTokens.length; ++i) {
                                        animeTypes.add(AnimeType.valueOf(messageTokens[i].toUpperCase()));
                                    }
                                }
                                catch (IllegalArgumentException e) {
                                    throw new IllegalArgumentException("is " + messageTokens[i] + " some kind of new anime type?", e);
                                }
                            }
                            AudioManager audioManager = guild.getAudioManager();
                            if (!audioManager.isConnected()) {
                                audioManager.openAudioConnection(currentChannel);
                            }
                            else if (audioManager.getConnectedChannel().getIdLong() != currentChannel.getIdLong()) {
                                throw new Exception("you're not in the same voice channel as me!");
                            }
                            try {
                                AnimeObject selectedAnime = null;
                                while (selectedAnime == null || selectedAnime.getSongs() == null || selectedAnime.getSongs().isEmpty()) {
                                    selectedAnime = SessionManager.getInstance().getMusicSession(guild).selectAnime(combineMethod, animeTypes);
                                    if (selectedAnime != null && (selectedAnime.getSongs() == null || selectedAnime.getEnglishTitle() == null)) {
                                        Document malPage = HtmlUtils.getMALPage(config.getMal(), selectedAnime.getMalId());
                                        if (selectedAnime.getEnglishTitle() == null) {
                                            selectedAnime.setEnglishTitle(HtmlUtils.getEnglishTitleFromMAL(malPage, selectedAnime.getTitle()));
                                        }
                                        if (selectedAnime.getSongs() == null) {
                                            selectedAnime.setSongs(HtmlUtils.getSongsFromMAL(malPage));
                                        }
                                    }
                                    else if (selectedAnime == null) {
                                        sourceChannel.sendMessage("None of these users have watched any anime..." + (animeTypes.isEmpty() ? "" : ("well not any that you're asking for, anyways..."))).queue();
                                        return;
                                    }
                                }
                                String songUrl = YoutubeUtil.pickSong(config.getYt(), selectedAnime);
                                SessionManager.getInstance().loadAndPlay(guild, sourceChannel, songUrl);
                                logger.info("Now Playing: " + songUrl);
                                SessionManager.getInstance().getMusicSession(guild).setCurrentSong(new MalSong(selectedAnime, songUrl, sourceChannel.getIdLong()));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                sourceChannel.sendMessage("What's wrong with my music player? Oh, " + e.getMessage()).queue();
                            }
                        }
                        catch (PermissionException e) {
                            sourceChannel.sendMessage("I don't have permission to join " + currentChannel.getName() + "! Rude...").queue();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            sourceChannel.sendMessage("Something's not right..." + e.getMessage()).queue();
                        }
                    }
                    else {
                        sourceChannel.sendMessage("How can we play if you're not in a voice channel?").queue();
                    }
                }
                else if (messageTokens.length >= 2 && messageTokens[1].trim().equals("stop")) {
                    MusicSession musicSession = SessionManager.getInstance().getMusicSession(guild);
                    MalSong currentSong = musicSession.getCurrentSong();
                    if (currentSong != null) {
                        if (!member.getVoiceState().inVoiceChannel() || member.getVoiceState().getChannel().getIdLong() != guild.getAudioManager().getConnectedChannel().getIdLong()) {
                            sourceChannel.sendMessage("You can't tell me to stop! You're not even in this voice channel!").queue();
                        }
                        else {
                            musicSession.scheduler.stopTrack();
                        }
                    }
                    else {
                        sourceChannel.sendMessage("I'm not even playing a song right now!").queue();
                    }
                }
                else {
                    logger.error("unknown command");
                    sourceChannel.sendMessage("Were you talking to me?").queue();
                }
            }
        }
        else if (event.isFromType(ChannelType.PRIVATE)) {
            logger.info("private message received from " + author);
            author.openPrivateChannel().queue((channel) -> {
                channel.sendMessage("Sorry, I don't talk to you on a personal level.").queue();
            });
        }
    }

    public void songEnded(long guildId, AudioTrackEndReason endReason) {
        Guild guild = jda.getGuildById(guildId);
        MusicSession musicSession = SessionManager.getInstance().getMusicSession(guild);
        MalSong lastSong = musicSession.getCurrentSong();
        guild.getTextChannelById(lastSong.getPlayedFromMessageChannelId()).sendMessage(MessageUtils.getSongEndMessage(endReason) + " The song was from " + lastSong.getAnime().getEnglishTitle() + (lastSong.getAnime().getTitle().equals(lastSong.getAnime().getEnglishTitle()) ? "" : (" (" + lastSong.getAnime().getTitle() + ")")) + " in case you were wondering\n" + lastSong.getUrl()).queue();
        musicSession.setCurrentSong(null);
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            checkIfAlone(event.getChannelLeft(), event.getEntity());
        }
    }

    private void checkIfAlone(VoiceChannel leftChannel, Member leftMember) {
        if (leftChannel.getMembers().stream().anyMatch((member) -> member.getIdLong() == jda.getSelfUser().getIdLong()) && leftChannel.getMembers().size() == 1) {
            logger.info("I'm the only one here...");
            leftMember.getUser().openPrivateChannel().queue((channel) -> {
                channel.sendMessage("You left me alone in the voice channel!").queue();
            });
            leftChannel.getGuild().getAudioManager().closeAudioConnection();
        }
    }
}