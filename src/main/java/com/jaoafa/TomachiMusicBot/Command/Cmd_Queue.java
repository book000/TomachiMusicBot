package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
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

public class Cmd_Queue {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Queue");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);
		List<Track> tracks = audioP.getPlaylist();

		if(tracks.isEmpty()){
			embed.appendField("Result", "キューに曲がありません！ `*search`コマンドを使用して追加しよう！", false);
			embed.withColor(Color.DARK_GRAY);

			channel.sendMessage("", embed.build());
			return;
		}

		int i = 1;
		for(Track track : tracks){
			File file = (File) track.getMetadata().get("file");
			if(file == null){
				embed.appendField("Track No." + i, "Load Error.", false);
				i++;
				continue;
			}
			try{
				Mp3File mp3file = new Mp3File(file);
				String title = "", artist = "", album = "";
				if(mp3file.hasId3v2Tag()){
					title = mp3file.getId3v2Tag().getTitle();
					artist = mp3file.getId3v2Tag().getArtist();
					album = mp3file.getId3v2Tag().getAlbum();
				}else if(mp3file.hasId3v1Tag()){
					title = mp3file.getId3v1Tag().getTitle();
					artist = mp3file.getId3v1Tag().getArtist();
					album = mp3file.getId3v1Tag().getAlbum();
				}
				long sec = mp3file.getLengthInSeconds();
				String TimeStr = Cmd_Search.SecToHIS(sec);

				embed.appendField("Track No." + i, "`" + title + "` - `" + album + "`\n"
						+ "Author: " + artist + "\n"
						+ "Time: " + TimeStr, false);
			}catch(InvalidDataException | UnsupportedTagException | IOException | IllegalArgumentException e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				channel.sendMessage("[" + sdf.format(new Date()) + "] " + "ERROR: トラックのパースに失敗しました。。 ```" + sw.toString() + "```");
			}
			i++;
		}
		embed.withColor(Color.ORANGE);

		channel.sendMessage("", embed.build());
	}
}