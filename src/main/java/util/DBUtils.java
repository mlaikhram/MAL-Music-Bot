package util;

import java.io.File;
import java.sql.*;

public class DBUtils {

    private static String connectionString = null;

    public static void init(String dbPath) throws Exception {
        connectionString = "jdbc:sqlite:" + dbPath;
        Class.forName("org.sqlite.JDBC");
        File dbFile = new File(dbPath);
        dbFile.createNewFile();
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS songs(name TEXT PRIMARY KEY, ytid TEXT NOT NULL)");
        }
    }

    public static void addSong(String songName, String ytid) throws Exception {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO songs values(?, ?)");
            statement.setString(1, songName);
            statement.setString(2, ytid);
            statement.execute();
        }
    }

    public static String getSongId(String songName) throws Exception {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String ytid = null;
            PreparedStatement statement = connection.prepareStatement("SELECT ytid FROM songs WHERE name=?");
            statement.setString(1, songName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ytid = resultSet.getString("ytid");
            }
            return ytid;
        }
    }

    public static void fixSongId(String songName, String newId) throws Exception {
        if (getSongId(songName) != null) {
            try (Connection connection = DriverManager.getConnection(connectionString)) {
                PreparedStatement statement = connection.prepareStatement("UPDATE songs SET ytid=? WHERE name=?");
                statement.setString(1, newId);
                statement.setString(2, songName);
                statement.execute();
            }
        }
        else {
            throw new Exception("`" + songName + "` isn't any song I know...");
        }
    }
}
