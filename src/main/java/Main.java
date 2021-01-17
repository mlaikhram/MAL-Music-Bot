import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import listener.MalMusicListener;
import model.YmlConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class Main { // TODO: update readme

    public static void main(String[] args) throws LoginException, IOException, InterruptedException {
        File file = new File("bot.yml");
        YmlConfig config = new ObjectMapper(new YAMLFactory()).readValue(file, YmlConfig.class);

        MalMusicListener malMusicListener = new MalMusicListener(config);
        JDA jda = JDABuilder.createDefault(config.getToken())
                .addEventListeners(malMusicListener)
                .setActivity(Activity.listening("music"))
                .build();
        malMusicListener.setJda(jda);
        jda.awaitReady();
    }
}
