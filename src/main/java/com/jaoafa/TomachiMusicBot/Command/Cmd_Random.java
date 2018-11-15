package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

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

public class Cmd_Random {

	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Random");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		List<File> songDir = TomachiMusicBot.getMusicFiles();

		Collections.shuffle(songDir);

		int getCount = 1;
		if(args.length >= 2){
			try{
				getCount = Integer.valueOf(args[1]);

				if(getCount <= 0){
					embed.appendField("Error", "第一引数には1以上の数値を入力してください。", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}
			}catch(NumberFormatException e){
				embed.appendField("Error", "第一引数には数値(キューにいれる曲数)を入力してください。", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
		}

		boolean NOInstrumental = false;
		if(args[args.length - 1].equalsIgnoreCase("true")){
			// カラオケ等外す
			NOInstrumental = true;
		}

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

		int addedCount = 0;
		for(File file : songDir){
			if(!file.isFile()) continue;
			try {
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

				if(NOInstrumental){
					if(title.contains("instrumental") || title.contains("Instrumental") || title.contains("カラオケ") || title.contains("ドラマ")){
						continue;
					}
				}

				audioP.queue(file);
				embed.appendField("Track" + audioP.getPlaylistSize(), "`" + title + "` - `" + album + "`\n"
						+ "Author: " + artist + "\n"
						+ "Time: " + TimeStr, false);

				addedCount++;
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException | UnsupportedAudioFileException e) {
				continue;
			}
			getCount--;
			if(getCount == 0){
				break;
			}
		}
		embed.appendField("Result", "ランダムで" + addedCount + "曲を追加しました。", false);
		if(NOInstrumental) embed.appendField("NOInstrumental", "カラオケ等の曲を外しました。", false);
		embed.withColor(Color.ORANGE);

		channel.sendMessage("", embed.build());
	}
}
