package listener;

import audio.SessionManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import model.mal.AnimeObject;
import model.mal.MalSong;
import model.mal.MalUser;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import util.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String[] messageTokens = inputToCommand(rawMessage).toArray(new String[0]);

        boolean isAgain = false;
        if (event.isFromType(ChannelType.TEXT)) {
            if (messageTokens.length > 0 && ((MessageUtils.isUserMention(messageTokens[0]) && MessageUtils.mentionToUserID(messageTokens[0]).toString().equals(myID)) || messageTokens[0].equalsIgnoreCase(MessageUtils.COMMAND_PROMPT))) {
                logger.info("message received from " + author + ": " + rawMessage);
                if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("again") && SessionManager.getInstance().getMusicSession(guild).getLastCommand() != null) {
                    isAgain = true;
                    messageTokens = SessionManager.getInstance().getMusicSession(guild).getLastCommand().split("[ ]+");
                    for (int i = 1; i < messageTokens.length; ++i) {
                        messageTokens[i] = messageTokens[i].toLowerCase();
                    }
                }

                if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("methods")) {
                    sourceChannel.sendMessage(CombineMethod.getInfoText()).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("types")) {
                    StringBuilder message = new StringBuilder();
                    message.append("Here's all of the anime types I know:\n\n");
                    for (AnimeType animeType : AnimeType.values()) {
                        message.append('`' + animeType.name() + "`\n");
                    }
                    message.deleteCharAt(message.length() - 1);
                    sourceChannel.sendMessage(message.toString()).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("add")) {
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
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("remove")) {
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
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("users")) {
                    StringBuilder message = new StringBuilder();
                    MusicSession currentSession = SessionManager.getInstance().getMusicSession(guild);
                    message.append("Current MAL Users (" + currentSession.getMalUsers().size() + "): \n");
                    for (MalUser user : currentSession.getMalUsers()) {
                        message.append(user.getUsername() + '\n');
                    }
                    message.deleteCharAt(message.length() - 1);
                    sourceChannel.sendMessage(message.toString()).queue();
                }
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("play")) {
                    if (SessionManager.getInstance().getMusicSession(guild).getCurrentSong() != null && !isAgain) {
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
                                    combineMethod = CombineMethod.fromInt(Integer.parseInt(messageTokens[2]));
                                }
                                catch (Exception e) {
                                    throw new IllegalArgumentException("what is " + messageTokens[2] + "? I need a number from 0 to " + (CombineMethod.values().length - 1) + "...", e);
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
                                            logger.info("anime name: " + selectedAnime.getEnglishTitle());
                                            selectedAnime.setSongs(HtmlUtils.getSongsFromMAL(malPage));
                                            logger.info("post add");
                                            for (String malSong : selectedAnime.getSongs()) {
                                                logger.info(malSong);
                                            }
                                            if (SessionManager.getInstance().getMusicSession(guild).filterRecent(selectedAnime.getSongs()).isEmpty()) {
                                                selectedAnime = null;
                                            }
                                        }
                                    }
                                    else if (selectedAnime == null) {
                                        sourceChannel.sendMessage("None of these users have watched any anime..." + (animeTypes.isEmpty() ? "" : ("well not any with your restrictions, anyways..."))).queue();
                                        return;
                                    }
                                }
                                if (isAgain && SessionManager.getInstance().getMusicSession(guild).getCurrentSong() != null) {
                                    SessionManager.getInstance().getMusicSession(guild).scheduler.stopTrack();
                                }
                                else if (!isAgain) {
                                    SessionManager.getInstance().getMusicSession(guild).setLastCommand(rawMessage);
                                }
                                try {
                                    MalSong song = YoutubeUtil.pickSong(SessionManager.getInstance().getMusicSession(guild), sourceChannel.getIdLong(), config.getYt(), selectedAnime);
                                    SessionManager.getInstance().getMusicSession(guild).addToRecent(song.getName());
                                    SessionManager.getInstance().loadAndPlay(guild, sourceChannel, song.getUrl());
                                    logger.info("Now Playing: " + song.getUrl());
                                    SessionManager.getInstance().getMusicSession(guild).setCurrentSong(song);
                                }
                                catch (HttpClientErrorException e) {
                                    if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                        throw new Exception("Youtube is not letting me search for any more videos...", e);
                                    }
                                    else {
                                        throw e;
                                    }
                                }
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
                else if (messageTokens.length >= 2 && messageTokens[1].equalsIgnoreCase("stop")) {
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
                else if (messageTokens.length >= 4 && messageTokens[1].equalsIgnoreCase("fix")) {
                    if (config.getFixers().contains(member.getId())) {
                        try {
                            DBUtils.fixSongId(messageTokens[2], messageTokens[3]);
                            sourceChannel.sendMessage("Done! The song should be fixed now!").queue();
                        } catch (Exception e) {
                            e.printStackTrace();
                            sourceChannel.sendMessage("Hmm..." + e.getMessage()).queue();
                        }
                    }
                    else {
                        sourceChannel.sendMessage("Sorry, but you're not allowed to fix songs").queue();
                    }
                }
                else if (messageTokens.length <= 1 || (containsIgnoreCase("help", messageTokens, "[^a-zA-Z]"))) {
                    sourceChannel.sendMessage(MessageUtils.HELP_TEXT).queue();
                }
                else {
                    logger.error("unknown command");
                    sourceChannel.sendMessage(config.getRandomVoiceLine() + "If you need `help`, just tag me and ask for it!").queue();
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
        guild.getTextChannelById(lastSong.getPlayedFromMessageChannelId()).sendMessage(MessageUtils.getSongEndMessage(endReason) + " The song was " + lastSong.toString() + (Arrays.asList(6547L, 9062L, 10067L).contains(lastSong.getAnime().getMalId()) ? "! That was a really good one!\n" : " in case you were wondering\n") + lastSong.getUrl()).queue();
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
            if (SessionManager.getInstance().getMusicSession(leftChannel.getGuild()).getCurrentSong() != null) {
                SessionManager.getInstance().getMusicSession(leftChannel.getGuild()).scheduler.stopTrack();
            }
        }
    }

    private List<String> inputToCommand(String message) {
        List<String> command = new ArrayList<>();
        Matcher m = Pattern.compile("([^`]\\S*|`.+?`)\\s*").matcher(message);
        while (m.find()) {
            command.add(m.group(1).replace("`", ""));
        }
        return command;
    }

    private boolean containsIgnoreCase(String target, String[] args, String filterRegex) {
        for (String arg : args) {
            if (arg.replaceAll(filterRegex, "").equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
