package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class Cmd_Info {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Info");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		TomachiMusicBot.refreshMusicFiles(); // リフレッシュ
		embed.appendDesc("音源ファイルキャッシュをリフレッシュしました。");
		List<File> songDir = TomachiMusicBot.getMusicFiles();

		embed.appendField("累計ファイル数", songDir.size() + "ファイル", false);

		int okSong = 0;
		long size = 0;
		long sizeOK = 0;
		Map<String, Integer> artists = new HashMap<>();
		Map<String, Integer> albums = new HashMap<>();
		for(File file : songDir){
			if(file.isFile()) size = file.length();
			try {
				Mp3File mp3file = new Mp3File(file);

				if(mp3file.hasId3v2Tag()){
					ID3v2 id3v2Tag = mp3file.getId3v2Tag();

					String artist = id3v2Tag.getArtist();
					if(artists.containsKey(artist)){
						artists.put(artist, artists.get(artist) + 1);
					}else{
						artists.put(artist, 1);
					}
					String album = id3v2Tag.getAlbum();
					if(albums.containsKey(album)){
						albums.put(album, albums.get(album) + 1);
					}else{
						albums.put(album, 1);
					}
				}else if(mp3file.hasId3v1Tag()){
					ID3v1 id3v1Tag = mp3file.getId3v1Tag();

					String artist = id3v1Tag.getArtist();
					if(artists.containsKey(artist)){
						artists.put(artist, artists.get(artist) + 1);
					}else{
						artists.put(artist, 1);
					}
					String album = id3v1Tag.getAlbum();
					if(albums.containsKey(album)){
						albums.put(album, albums.get(album) + 1);
					}else{
						albums.put(album, 1);
					}
				}
				if(file.isFile()) sizeOK = file.length();

				okSong++;
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e) {
				continue;
			}
		}
		embed.appendField("そのうち、視聴可能曲数", okSong + "曲", false);
		embed.appendField("累計アーティスト数", "" + artists.size(), false);
		embed.appendField("累計アルバム数", "" + albums.size(), false);

		embed.appendField("音源ファイル分使用容量数", getSizeStr(size), false);
		embed.appendField("そのうち、視聴可能音源ファイル分使用容量数", getSizeStr(sizeOK), false);


		embed.withColor(Color.GREEN);

		channel.sendMessage("", embed.build());
	}
	static String getSizeStr(long size) {
		double dbytes = (double) size;
	    DecimalFormat df = new DecimalFormat("#.##");

	    if (dbytes < 1024) {
	        return df.format(size) + " bytes";
	    } else if (dbytes < 1024 * 1024) {
	        return df.format(dbytes / 1024) + " KB";
	    } else if (dbytes < 1024 * 1024 * 1024) {
	        return df.format(dbytes / 1024 / 1024) + " MB";
	    } else if (dbytes < 1024 * 1024 * 1024 * 1024L) {
	        return df.format(dbytes / 1024 / 1024 / 1024) + " GB";
	    } else {
	        return size + " bytes";
	    }
	}
}
