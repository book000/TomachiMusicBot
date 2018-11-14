package com.jaoafa.TomachiMusicBot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jaoafa.TomachiMusicBot.Command.MainEvent;
import com.jaoafa.TomachiMusicBot.Event.Event_TrackFinish;
import com.jaoafa.TomachiMusicBot.Event.Event_TrackStart;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

public class TomachiMusicBot {
	public static String ImgurKey = null;
	public static String ImgurKeySECRET = null;
	private static IChannel Channel = null;
	public static void main(String[] args) {
		File f = new File("conf.properties");
		Properties props;
		try{
			InputStream is = new FileInputStream(f);

			// プロパティファイルを読み込む
			props = new Properties();
			props.load(is);
		}catch(FileNotFoundException e){
			// ファイル生成
			props = new Properties();
			props.setProperty("token", "PLEASETOKEN");
			props.setProperty("ImgurKey", "PLEASETOKEN");
			props.setProperty("ImgurKeySECRET", "PLEASETOKEN");
			try {
				props.store(new FileOutputStream("conf.properties"), "Comments");
				System.out.println("Please Config Token!");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// キーを指定して値を取得する
		String token = props.getProperty("token");
		if(token.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please Token!");
			return;
		}
		ImgurKey = props.getProperty("ImgurKey");
		if(ImgurKey.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please Token!");
			return;
		}
		ImgurKeySECRET = props.getProperty("ImgurKeySECRET");
		if(ImgurKeySECRET.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please Token!");
			return;
		}

		IDiscordClient client = createClient(token, true);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new MainEvent());

		dispatcher.registerListener(new Event_TrackStart());
		dispatcher.registerListener(new Event_TrackFinish());

		Channel = client.getChannelByID(512242412635029514L);
	}
	public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			if (login) {
				return clientBuilder.login(); // Creates the client instance and logs the client in
			} else {
				return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
			}
		} catch (DiscordException e) { // This is thrown if there was a problem building the client
			e.printStackTrace();
			return null;
		}
	}
	public static void check(IMessage message){
		Emoji e = EmojiManager.getForAlias("eye");
		message.addReaction(e);
	}
	public static void setChannel(IMessage message){
		Channel = message.getChannel();
	}
	public static void setChannel(IChannel channel){
		Channel = channel;
	}
	public static IChannel getChannel(){
		return Channel;
	}
}
