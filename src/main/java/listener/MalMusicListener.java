package listener;

import model.AnimeObject;
import model.MalUser;
import model.MusicSession;
import model.YmlConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HtmlUtils;
import util.MessageUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class MalMusicListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MalMusicListener.class);

    private JDA jda;

    // Used to keep track of which guilds have an active session
    private final Map<Long, MusicSession> activeGuilds;
    private YmlConfig config;

    public MalMusicListener(YmlConfig config) {
        activeGuilds = new HashMap<>();
        this.config = config;
    }

    public MusicSession getMusicSession(long guildId) {
        if (!activeGuilds.containsKey(guildId)) {
            activeGuilds.put(guildId, new MusicSession(config));
        }
        return activeGuilds.get(guildId);
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

        if (event.isFromType(ChannelType.TEXT)) {
            if (MessageUtils.isUserMention(messageTokens[0].trim()) && MessageUtils.mentionToUserID(messageTokens[0].trim()).toString().equals(myID)) {
                logger.info("message received from " + author + ": " + rawMessage);

                if (messageTokens.length <= 1 || (messageTokens.length >= 2 && messageTokens[1].trim().equals("help"))) {
                    sourceChannel.sendMessage(MessageUtils.HELP_TEXT).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].trim().equals("add")) {
                    if (messageTokens.length > 2) {
                        StringBuilder message = new StringBuilder();
                        for (int i = 2; i < messageTokens.length; ++i) {
                            String user = messageTokens[i].trim();
                            try {
                                getMusicSession(guild.getIdLong()).addUser(user);
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
                else if (messageTokens.length >= 2 && messageTokens[1].trim().equals("users")) {
                    StringBuilder message = new StringBuilder();
                    MusicSession currentSession = getMusicSession(guild.getIdLong());
                    message.append("Current MAL Users: " + currentSession.getMalUsers().size() + '\n');
                    for (MalUser user : currentSession.getMalUsers()) {
                        message.append(user.getUsername() + '\n');
                    }
                    message.deleteCharAt(message.length() - 1);
                    sourceChannel.sendMessage(message.toString()).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].trim().equals("play")) {
                    if (member.getVoiceState().inVoiceChannel() && guild.getVoiceChannelById(member.getVoiceState().getChannel().getIdLong()) != null) {
                        VoiceChannel currentChannel = member.getVoiceState().getChannel();
                        try {
                            AudioManager audioManager = guild.getAudioManager();
                            audioManager.openAudioConnection(currentChannel);
                            AnimeObject selectedAnime = getMusicSession(guild.getIdLong()).selectAnime(); // TODO: the rest
                            if (selectedAnime != null) {
                                StringBuilder message = new StringBuilder();
                                message.append("Try [" + selectedAnime.getMalId() + "] " + selectedAnime.getTitle() + "!\n");
                                try {
                                    Set<String> songs = HtmlUtils.getSongsFromMAL(config.getMal(), selectedAnime.getMalId());
                                    for (String song : songs) {
                                        message.append(song + "\n");
                                    }
                                    message.deleteCharAt(message.length() - 1);
                                    sourceChannel.sendMessage(message.toString()).queue();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                sourceChannel.sendMessage("None of these users have watched any anime...").queue();
                            }
                        }
                        catch (PermissionException e) {
                            sourceChannel.sendMessage("I don't have permission to join " + currentChannel.getName() + "! Rude...").queue();
                        }
                        catch (Exception e) {
                            sourceChannel.sendMessage("I couldn't join " + currentChannel.getName() + ": " + e.getMessage()).queue();
                        }
                    }
                    else {
                        sourceChannel.sendMessage("How can we play if you're not in a voice channel?").queue();
                    }
                }
                else if (messageTokens.length >= 2 && messageTokens[1].trim().equals("end")) {

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

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        VoiceChannel eventChannel = event.getChannelLeft();
        if (eventChannel.getMembers().stream().anyMatch((member) -> member.getIdLong() == jda.getSelfUser().getIdLong()) && eventChannel.getMembers().size() == 1) {
            logger.info("I'm the only one here...");
            event.getMember().getUser().openPrivateChannel().queue((channel) -> {
                channel.sendMessage("You left me alone in the voice channel!").queue();
            });
            event.getGuild().getAudioManager().closeAudioConnection();
        }
    }
}
