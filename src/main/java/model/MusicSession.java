package model;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AnimeType;
import util.CombineMethod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicSession {

    private static final Logger logger = LoggerFactory.getLogger(MusicSession.class);

    private static final int MAX_RECENTS = 20;

    private YmlConfig config;
    private Set<MalUser> malUsers;
    private LinkedHashSet<Long> recentAnime;
    private AudioSendHandler audioSendHandler;

    public MusicSession(YmlConfig config) {
        this.malUsers = new TreeSet<>();
        this.config = config;
        this.recentAnime = new LinkedHashSet<>();
    }

    public void addUser(String username) throws Exception {
        MalUser newUser = new MalUser(username);
        if (malUsers.contains(newUser)) {
            throw new Exception(username + " is already added to this session!");
        }
        else {
            try {
                newUser.populate(config.getJikan());
                malUsers.add(newUser);
            }
            catch (Exception e) {
                throw new Exception("could not find " + username + "'s list on MAL", e);
            }
        }
    }

    public AnimeObject selectAnime(CombineMethod combineMethod, Collection<AnimeType> animeTypes) {
        for (MalUser user : malUsers) {
            logger.info(user.getUsername() + ": " + user.getAnimeList().size());
        }
        List<AnimeObject> collection = getCombinedList(combineMethod).filter((animeObject) -> animeTypes.isEmpty() || animeTypes.contains(animeObject.getType())).filter((animeObject) -> !recentAnime.contains(animeObject.getMalId())).collect(Collectors.toList());
        logger.info("total: " + collection.size());
        if (collection.size() > 0) {
            AnimeObject nextAnime = collection.get(new Random().nextInt(collection.size()));
            recentAnime.add(nextAnime.getMalId());
            if (recentAnime.size() > MAX_RECENTS) {
                Iterator<Long> itr = recentAnime.iterator();
                long lastId = -1;
                while (itr.hasNext()) {
                    lastId = itr.next();
                }
                recentAnime.remove(lastId);
            }
            return nextAnime;
        }
        else if (!recentAnime.isEmpty()) {
            logger.info("ran out of anime, resetting list");
            recentAnime.clear();
            return selectAnime(combineMethod, animeTypes);
        }
        else {
            return null;
        }
    }

    private Stream<AnimeObject> getCombinedList(CombineMethod combineMethod) { // TODO: implement other combine methods
        switch (combineMethod) {
            default:
                return malUsers.stream().map((user) -> user.getAnimeList()).flatMap(Set::stream);
        }
    }

    public Set<MalUser> getMalUsers() {
        return malUsers;
    }
}
