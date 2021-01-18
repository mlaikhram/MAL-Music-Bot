# MAL-Music-Bot
Discord Bot that allows you to play MAL music trivia with friends. The bot will utilize Youtube's search API to find a song that best fits the song name randomly selected from each user's MAL page, and store the metadata locally in a sqlite db to allow for faster and less restricted lookups over time.

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
yt: https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q={query}&type=video&videoDuration=short&key=<API_KEY>
```
Replace `<BOT_TOKEN>` with the token obtained from your own Discord Bot (More details on creating a Discord Bot can be found [here](https://discord.com/developers/docs/intro)).
Replace `<API_KEY>` with an api key you own from Google's Youtube API (More details can be found [here](https://developers.google.com/youtube/v3/docs)).

Once this is set up, you can run the .jar file using the following command:
```
java -jar mal-music-bot-1.0-SNAPSHOT.jar
```
Once the Bot is running, you can invite it to your Discord Server from the [Discord Developer Portal](https://discord.com/developers/applications) and interact with it from your text channels.

### Usage
TODO
