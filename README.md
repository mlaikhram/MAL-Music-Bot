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
  - https://api.jikan.moe/v3/user/{user}/animelist/watching/{page}
  - https://api.jikan.moe/v3/user/{user}/animelist/completed/{page}

mal: https://myanimelist.net/anime/{id}/
yt: https://youtube.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q={query}&type=video&videoDuration=short&key=<API_KEY>

dbPath: <FILE_PATH>

fixers:
 - <Discord ID of a user>
 - ...

voiceLines:
 - Example voice line.
 - ...
```
Replace `<BOT_TOKEN>` with the token obtained from your own Discord Bot (More details on creating a Discord Bot can be found [here](https://discord.com/developers/docs/intro)).
Replace `<API_KEY>` with an api key you own from Google's Youtube API (More details can be found [here](https://developers.google.com/youtube/v3/docs)).
Replace `<FILE_PATH>` with the path to your db file for caching (if the file does not exist, it will be created).
Fixers will be allowed to modify the bot's cached song map to help improve the quality and accuracy of music being played. This role should only be given to trusted users.
VoiceLines are used as filler text if someone mentions the bot, but gives an invalid command. You may use as many as you want, and they will be selected at random when needed.

Once this is set up, you can run the .jar file using the following command:
```
java -jar mal-music-bot-1.0-SNAPSHOT.jar
```
Once the Bot is running, you can invite it to your Discord Server from the [Discord Developer Portal](https://discord.com/developers/applications) and interact with it from your text channels.

### Usage
Tag the bot or type !iwa and send one of the following messages into a text channel to use the bot:

`add <mal username> ...`

Add a user (or users) to the list of anime to choose from

`remove <mal username> ...`

Remove a user (or users) from the list of anime to choose from

`users`

List all users that are currently added to the list

`play [combine method id] [anime type] ...`

Play a song from the current list of users, combining with the specified combine method (optional) and only selecting from the listed anime types (optional)

`stop`

Stop the song that is currently playing

`methods`

List all combine methods

`types`

List all anime types

`again`

Repeat the last play command

``fix `<english anime name>` `<song name>` <ytid>``

Fix a broken video ID for a given song (Authorized users only)