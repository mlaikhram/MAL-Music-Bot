package listener;

import audio.SessionManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import model.mal.*;
import audio.MusicSession;
import model.config.YmlConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import util.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                        sourceChannel.sendMessage("Attempting to add users. This could take a while...").queue();
                        for (int i = 2; i < messageTokens.length; ++i) {
                            String user = messageTokens[i];
                            try {
                                JikanUserResponse jikanUser = SessionManager.getInstance().getMusicSession(guild).addUser(config.getJikan().getUrl(), user);

                                MessageBuilder messageBuilder = new MessageBuilder();
                                messageBuilder.append("Successfully added!" + (i < messageTokens.length - 1 ? " Now for the next user..." : " Done!"));

                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setTitle(jikanUser.getUsername());
                                embedBuilder.setThumbnail(jikanUser.getImage());
                                embedBuilder.setColor(3035554);
                                embedBuilder.setDescription(String.format("[MyAnimeList Page](%s)", jikanUser.getUrl()));

                                embedBuilder.addField("Completed", jikanUser.getAnimeStats().getCompleted() + "", true);
                                embedBuilder.addField("Watching", jikanUser.getAnimeStats().getWatching() + "", true);

                                messageBuilder.setEmbed(embedBuilder.build());
                                sourceChannel.sendMessage(messageBuilder.build()).queue();
                            }
                            catch (Exception e) {
                                sourceChannel.sendMessage("Failed to add " + user + ": " + e.getMessage() + "." + (i < messageTokens.length - 1 ? " Now for the next user..." : " Done!")).queue();
                                e.printStackTrace();
                            }
                        }
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
                                        ResponseEntity<JikanAnimeResponse> response = JikanUtils.getAnime(config.getJikan().getUrl(), selectedAnime.getMalId(), false);
                                        selectedAnime.applyJikan(response.getBody());
                                        if (SessionManager.getInstance().getMusicSession(guild).filterRecent(selectedAnime.getSongs()).isEmpty()) {
                                            selectedAnime = null;
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
                                    Set<String> filteredSongs = SessionManager.getInstance().getMusicSession(guild).filterRecent(selectedAnime.getSongs());
                                    String songName = (String) filteredSongs.toArray()[new Random().nextInt(filteredSongs.size())];
                                    lookupAndPlaySong(guild.getIdLong(), sourceChannel.getIdLong(), selectedAnime, songName);
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
                else if (messageTokens.length >= 5 && messageTokens[1].equalsIgnoreCase("fix")) {
                    if (config.getDiscord().getFixers().contains(member.getId())) {
                        try {
                            DBUtils.fixSongId(messageTokens[2], messageTokens[3], messageTokens[4]);
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
        String usersWithSong = commaJoin(musicSession.getMalUsers().stream().filter((user) -> user.getAnimeList().contains(lastSong.getAnime())).map(MalUser::getUsername).collect(Collectors.toList()));

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(MessageUtils.getSongEndMessage(endReason));
        messageBuilder.append((Arrays.asList(6547L, 9062L, 10067L).contains(lastSong.getAnime().getMalId()) ? " That was one of my favorites \u2764\uFE0F\n" : " The song was..."));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(15017372);
        embedBuilder.setTitle(lastSong.getName());
        embedBuilder.setDescription(String.format("[YouTube](%s)", lastSong.getUrl()));
        embedBuilder.setThumbnail(lastSong.getAnime().getImage());

        StringBuilder animeInfo = new StringBuilder();
        for (String otherTitle : lastSong.getAnime().getOtherTitles()) {
            animeInfo.append(otherTitle);
            animeInfo.append('\n');
        }
        animeInfo.append(String.format("\n[MyAnimeList Page](%s)", lastSong.getAnime().getUrl()));
        embedBuilder.addField(lastSong.getAnime().getEnglishTitle(), animeInfo.toString(), false);

        embedBuilder.setFooter(usersWithSong + " should've known that one");

        messageBuilder.setEmbed(embedBuilder.build());

        guild.getTextChannelById(lastSong.getPlayedFromMessageChannelId()).sendMessage(messageBuilder.build()).queue();
        musicSession.setCurrentSong(null);
    }

    private void lookupAndPlaySong(long guildId, long sourceChannelId, AnimeObject selectedAnime, String songName) {
        Guild guild = jda.getGuildById(guildId);
        MessageChannel sourceChannel = jda.getTextChannelById(sourceChannelId);
        try {
            MalSong song = YoutubeUtil.getMalSong(config.getYoutube().getUrl(), config.getYoutube().getToken(), songName, sourceChannel.getIdLong(), selectedAnime);
            SessionManager.getInstance().getMusicSession(guild).addToRecent(song.getName());
            SessionManager.getInstance().loadAndPlay(guild, sourceChannel, song, (retrySongName) -> lookupAndPlaySong(guildId, sourceChannelId, selectedAnime, retrySongName));
        }
        catch (Exception e) {
            e.printStackTrace();
            sourceChannel.sendMessage("What's wrong with my music player? Oh, " + e.getMessage()).queue();
        }
    }

    private String commaJoin(List<String> items) {
        if (items.isEmpty()) {
            return "Nobody";
        }
        else if (items.size() == 1) {
            return items.get(0);
        }
        String ans = String.join(", ", items.subList(0, items.size() - 1));
        return ans + " and " + items.get(items.size() - 1);
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
