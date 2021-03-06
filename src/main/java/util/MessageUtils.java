package util;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class MessageUtils {

    public static String ID_FORMAT = "<@%s>";
    public static String ID_REGEX = "<@[0-9]+>";

    public static Long mentionToUserID(String mention) {
        return Long.parseLong(mention.replaceAll("[<@!>]", ""));
    }

    public static String userIDToMention(String id) {
        return String.format(ID_FORMAT, id);
    }

    public static boolean isUserMention(String mention) {
        return mention.replaceAll("!", "").matches(ID_REGEX);
    }

    public static String getSongEndMessage(AudioTrackEndReason endReason) {
        switch (endReason) {
            case FINISHED:
                return "That's the end of the song!";

            case STOPPED:
                return "Fine I'll end it...";

            default:
                return "Uhh...not sure what happened there...";
        }
    }

    public static final String COMMAND_PROMPT = "!iwa";
    public static final String HELP_TEXT =
        "Tag me or type `" + COMMAND_PROMPT + "` and send one of the following messages:\n" +
        "\n" +
        "`add <mal username> ...`\n" +
        "Add a user (or users) to the list of anime to choose from\n" +
        "\n" +
        "`remove <mal username> ...`\n" +
        "Remove a user (or users) from the list of anime to choose from\n" +
        "\n" +
        "`users`\n" +
        "List all users that are currently added to the list\n" +
        "\n" +
        "`play [combine method id] [anime type] ...`\n" +
        "Play a song from the current list of users, combining with the specified combine method (optional) and only selecting from the listed anime types (optional)\n" +
        "\n" +
        "`stop`\n" +
        "Stop the song that I'm currently playing\n" +
        "\n" +
        "`methods`\n" +
        "List all of the combine methods I know\n" +
        "\n" +
        "`types`\n" +
        "List all of the anime types I know\n" +
        "\n" +
        "`again`\n" +
        "Repeat the last play command\n" +
        "\n" +
        "``fix `<english anime name>` `<song name>` <ytid>``\n" +
        "Fix a broken video ID for a given song (Authorized users only)";
}
