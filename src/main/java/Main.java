import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import listener.MalMusicListener;
import model.config.YmlConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import util.DBUtils;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File("bot.yml");
        YmlConfig config = new ObjectMapper(new YAMLFactory()).readValue(file, YmlConfig.class);
        DBUtils.init(config.getDb().getPath());
        MalMusicListener malMusicListener = new MalMusicListener(config);
        JDA jda = JDABuilder.createDefault(config.getDiscord().getToken())
                .addEventListeners(malMusicListener)
                .setActivity(Activity.listening("music"))
                .build();
        malMusicListener.setJda(jda);
        jda.awaitReady();
    }
}
