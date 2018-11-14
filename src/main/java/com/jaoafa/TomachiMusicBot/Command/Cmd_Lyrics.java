package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.jaoafa.TomachiMusicBot.Lib.JLyric;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;

public class Cmd_Lyrics {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);

		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Skip");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");
		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

		Track track = audioP.getCurrentTrack();

		if(track == null){
			embed.appendField("Error", "現在なにも曲は流れていません！", false);
			embed.withColor(Color.RED);
			channel.sendMessage("", embed.build());
			return;
		}

		File file = (File) track.getMetadata().get("file");
		try{
			Mp3File mp3file = new Mp3File(file);
			String title = "", artist = "";
			if(mp3file.hasId3v2Tag()){
				title = mp3file.getId3v2Tag().getTitle();
				artist = mp3file.getId3v2Tag().getArtist();
			}else if(mp3file.hasId3v1Tag()){
				title = mp3file.getId3v1Tag().getTitle();
				artist = mp3file.getId3v1Tag().getArtist();
			}

			if(!title.equals("") && !artist.equals("")){
				String lyrics = JLyric.search(title, artist);
				if(lyrics != null){
					channel.sendMessage("`" +  title + "` - `" + artist + "` ```" + lyrics + "```");
				}
			}
		}catch(InvalidDataException | UnsupportedTagException | IOException | IllegalArgumentException e){
			e.printStackTrace();
			return;
		}
	}
}
