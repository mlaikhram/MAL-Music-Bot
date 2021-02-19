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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS songs(anime_name TEXT, name TEXT, ytid TEXT NOT NULL, PRIMARY KEY(anime_name, name))");
        }
    }

    public static void addSong(String animeName, String songName, String ytid) throws Exception {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO songs values(?, ?, ?)");
            statement.setString(1, animeName);
            statement.setString(2, songName);
            statement.setString(3, ytid);
            statement.execute();
        }
    }

    public static String getSongId(String animeName, String songName) throws Exception {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String ytid = null;
            PreparedStatement statement = connection.prepareStatement("SELECT ytid FROM songs WHERE anime_name=? AND name=?");
            statement.setString(1, animeName);
            statement.setString(2, songName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ytid = resultSet.getString("ytid");
            }
            return ytid;
        }
    }

    public static void fixSongId(String animeName, String songName, String newId) throws Exception {
        if (getSongId(animeName, songName) != null) {
            try (Connection connection = DriverManager.getConnection(connectionString)) {
                PreparedStatement statement = connection.prepareStatement("UPDATE songs SET ytid=? WHERE anime_name=? AND name=?");
                statement.setString(1, newId);
                statement.setString(2, animeName);
                statement.setString(3, songName);
                statement.execute();
            }
        }
        else {
            throw new Exception("`" + songName + "` isn't any song I know...");
        }
    }

    public static void fixAnimeName(String oldAnimeName, String animeName, String songName) throws Exception {
        if (getSongId(oldAnimeName, songName) != null) {
            try (Connection connection = DriverManager.getConnection(connectionString)) {
                PreparedStatement statement = connection.prepareStatement("UPDATE songs SET anime_name=? WHERE anime_name=? AND name=?");
                statement.setString(1, animeName);
                statement.setString(2, oldAnimeName);
                statement.setString(3, songName);
                statement.execute();
            }
        }
        else {
            throw new Exception("`" + songName + "` isn't any song I know...");
        }
    }
}
