package model;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MusicSession {

    private static final Logger logger = LoggerFactory.getLogger(MusicSession.class);

    private YmlConfig config;
    private Set<MalUser> malUsers;
    private AudioSendHandler audioSendHandler;

    public MusicSession(YmlConfig config) {
        this.malUsers = new TreeSet<>();
        this.config = config;
    }

    public void addUser(String username) throws Exception {
        MalUser newUser = new MalUser(username);
        if (malUsers.contains(newUser)) {
            throw new Exception(username + " is already added to this session!");
        }
        else {
            try {
                newUser.populate(config.getJikan());
                malUsers.add(newUser); // TODO: retrieve anime lists watching + finished and store as user profile
            }
            catch (Exception e) {
                throw new Exception("could not find " + username + "'s list on MAL", e);
            }
        }
    }

    public AnimeObject selectAnime() {
        for (MalUser user : malUsers) {
            logger.info(user.getUsername() + ": " + user.getAnimeList().size());
        }
        List<AnimeObject> collection = malUsers.stream().map((user) -> user.getAnimeList()).flatMap(Set::stream).collect(Collectors.toList());
        logger.info("total: " + collection.size());
        if (collection.size() > 0) {
            return collection.get(new Random().nextInt(collection.size()));
        }
        else {
            return null;
        }
    }

    public Set<MalUser> getMalUsers() {
        return malUsers;
    }
}
