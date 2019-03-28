package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.jaoafa.TomachiMusicBot.Lib.MusicFilesDB;
import com.mpatric.mp3agic.Mp3File;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
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

		// VCにいるか
		Iterator<IVoiceChannel> itr = client.getConnectedVoiceChannels().iterator();
		boolean JoinedVCBool = false;
		while(itr.hasNext()){
			IVoiceChannel vc = itr.next();
			if(vc.getGuild().getLongID() != guild.getLongID()){
				continue;
			}
			JoinedVCBool = true;
		}
		if(!JoinedVCBool){ // VCにいなかったら
			embed.appendField("Error", "ボイスチャンネルに入っていません。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

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

		List<Mp3File> files;
		try {
			files = MusicFilesDB.Random(getCount, NOInstrumental);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			embed.appendField("Error", "SQLの実行に失敗しました。", false);
			embed.appendField("Message", e.getMessage(), false);
			embed.appendField("Cause", "" + e.getCause(), false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

		int addedCount = 0;
		for(Mp3File mp3file : files){
			try{
				File file = new File(mp3file.getFilename());
				String title = "";
				String artist = "";
				String album = "";
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

				audioP.queue(file);
				embed.appendField("Track" + audioP.getPlaylistSize(), "`" + title + "` - `" + album + "`\n"
						+ "Author: " + artist + "\n"
						+ "Time: " + TimeStr, false);

				addedCount++;
			}catch(IOException | UnsupportedAudioFileException e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				embed.appendField("Error", "キューへの追加に失敗しました。", false);
				embed.appendField("StackTrace", sw.toString(), false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
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
