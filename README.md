# MAL-Music-Bot
Discord Bot that allows you to play MAL music trivia with friends

### How to Install
You will need Gradle to run the Bot. Clone this repository, and run the following Gradle command (I use Intellij to open and run the project):
```
gradle clean jar
```
This will build the .jar file into the `target` folder. Before running the .jar file, you will need to create a file called `bot.yml` and place it in the same directory as the .jar file. `bot.yml` should contain the following lines:
```
token: <BOT_TOKEN>
jikan:
  - https://api.jikan.moe/v3/user/{user}/animelist/watching
  - https://api.jikan.moe/v3/user/{user}/animelist/completed

mal: https://myanimelist.net/anime/{id}/
yt: https://www.youtube.com/results?search_query={query}
```
Replace `<BOT_TOKEN>` with the token obtained from your own Discord Bot (More details on creating a Discord Bot can be found [here](https://discord.com/developers/docs/intro)).

Once this is set up, you can run the .jar file using the following command:
```
java -jar mal-music-bot-1.0-SNAPSHOT.jar
```
Once the Bot is running, you can invite it to your Discord Server from the [Discord Developer Portal](https://discord.com/developers/applications) and interact with it from your text channels.

### Usage
TODO
