package com.jaoafa.TomachiMusicBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.jaoafa.TomachiMusicBot.Command.MainEvent;
import com.jaoafa.TomachiMusicBot.Event.Event_TrackFinish;
import com.jaoafa.TomachiMusicBot.Event.Event_TrackStart;
import com.jaoafa.TomachiMusicBot.Lib.JLyric;
import com.jaoafa.TomachiMusicBot.Lib.JoySound;
import com.jaoafa.TomachiMusicBot.Lib.KasiTime;
import com.jaoafa.TomachiMusicBot.Lib.MusixMatch;
import com.jaoafa.TomachiMusicBot.Lib.Petitlyrics;
import com.jaoafa.TomachiMusicBot.Lib.UtaNet;
import com.jaoafa.TomachiMusicBot.Lib.Utamap;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

public class TomachiMusicBot {
	/*
	public static String ImgurKey = null;
	public static String ImgurKeySECRET = null;
	*/
	public static String GOOGLE_API_KEY = null;
	public static String CUSTOM_SEARCH_ENGINE_ID = null;
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
			/*
			props.setProperty("ImgurKey", "PLEASETOKEN");
			props.setProperty("ImgurKeySECRET", "PLEASETOKEN");
			*/
			props.setProperty("GOOGLE_API_KEY", "PLEASETOKEN");
			props.setProperty("CUSTOM_SEARCH_ENGINE_ID", "PLEASETOKEN");
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
		/*
		ImgurKey = props.getProperty("ImgurKey");
		if(ImgurKey.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please ImgurKey!");
			return;
		}
		ImgurKeySECRET = props.getProperty("ImgurKeySECRET");
		if(ImgurKeySECRET.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please ImgurKeySECRET!");
			return;
		}
		*/
		GOOGLE_API_KEY = props.getProperty("GOOGLE_API_KEY");
		if(GOOGLE_API_KEY.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please GOOGLE_API_KEY!");
			return;
		}
		CUSTOM_SEARCH_ENGINE_ID = props.getProperty("CUSTOM_SEARCH_ENGINE_ID");
		if(CUSTOM_SEARCH_ENGINE_ID.equalsIgnoreCase("PLEASETOKEN")){
			System.out.println("Please CUSTOM_SEARCH_ENGINE_ID!");
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

	public static Map<String, String> getLyrics(String title, String artist){
		Map<String, String> r = new HashMap<String, String>();
		String lyrics = null;
		// J-Lyrics
		JLyric jlyric = new JLyric(title, artist);
		if(jlyric.getStatus()){
			lyrics = jlyric.getLyrics();
			if(lyrics != null){
				lyrics = replaceEscapeLyrics(lyrics);
				String realArtist = jlyric.getRealArtist();

				r.put("status", "true");
				r.put("lyrics", lyrics);
				r.put("realartist", realArtist);
				r.put("source", "j-lyric.net");
				return r;
			}
		}

		// KasiTime
		try {
			lyrics = KasiTime.search(title, artist);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "kasi-time.com");
			return r;
		}

		// JoySound
		try {
			lyrics = JoySound.search(title, artist);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "joysound.com");
			return r;
		}

		// UtaNet
		try {
			lyrics = UtaNet.search(title, artist);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "uta-net.com");
			return r;
		}

		// Petitlyrics
		try {
			lyrics = Petitlyrics.search(title, artist);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "petitlyrics.com");
			return r;
		}

		// MusixMatch
		try {
			MusixMatch musixmatch = new MusixMatch(title, artist);
			if(musixmatch.getStatus()){
				lyrics = musixmatch.getLyrics();
				if(lyrics != null){
					lyrics = replaceEscapeLyrics(lyrics);
					String realArtist = musixmatch.getRealArtist();

					r.put("status", "true");
					r.put("lyrics", lyrics);
					r.put("realartist", realArtist);
					r.put("source", "musixmatch.com");
					return r;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		// Utamap
		try {
			lyrics = Utamap.search(title, artist);
			if(lyrics != null){
				lyrics = replaceEscapeLyrics(lyrics);
				r.put("status", "true");
				r.put("lyrics", lyrics);
				r.put("source", "utamap.com");
				return r;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		/* ---------------- 以下、アーティストなし ---------------- */

		// KasiTime
		try {
			lyrics = KasiTime.search(title, null);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "kasi-time.com [ARITST NULL]");
			return r;
		}

		// JoySound
		try {
			lyrics = JoySound.search(title, null);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "joysound.com [ARITST NULL]");
			return r;
		}

		// UtaNet
		try {
			lyrics = UtaNet.search(title, null);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "uta-net.com [ARITST NULL]");
			return r;
		}

		// Petitlyrics
		try {
			lyrics = Petitlyrics.search(title, null);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
		if(lyrics != null){
			r.put("status", "true");
			r.put("lyrics", lyrics);
			r.put("source", "petitlyrics.com [ARITST NULL]");
			return r;
		}

		r.put("status", "false");
		return r;
	}
	static String replaceEscapeLyrics(String lyrics){
		lyrics = lyrics.replaceAll("&quot;", "\"");
		return lyrics;
	}
	public static JSONObject getHttpJson(String address, Map<String, String> headers){
		StringBuilder builder = new StringBuilder();
		try{
			URL url = new URL(address);

			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			if(headers != null){
				for(Map.Entry<String, String> header : headers.entrySet()) {
					connect.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			connect.connect();

			if(connect.getResponseCode() != HttpURLConnection.HTTP_OK){
				InputStream in = connect.getErrorStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				in.close();
				connect.disconnect();

				System.out.println("HTTPWARN: " + connect.getResponseMessage());
				return null;
			}

			InputStream in = connect.getInputStream();

			JSONTokener tokener = new JSONTokener(in);
			JSONObject json = new JSONObject(tokener);
			in.close();
			connect.disconnect();
			return json;
		}catch(Exception e){
			return null;
		}
	}
	public static String getHttpHTML(String address, Map<String, String> headers){
		StringBuilder builder = new StringBuilder();
		try{
			URL url = new URL(address);

			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			if(headers != null){
				for(Map.Entry<String, String> header : headers.entrySet()) {
					connect.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			connect.connect();

			if(connect.getResponseCode() != HttpURLConnection.HTTP_OK){
				InputStream in = connect.getErrorStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				in.close();
				connect.disconnect();

				System.out.println("HTTPWARN: " + connect.getResponseMessage());
				return null;
			}

			InputStream in = connect.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
			    sb.append(line + "\n");
			}
			in.close();
			connect.disconnect();
			return sb.toString();
		}catch(Exception e){
			return null;
		}
	}
	public static JSONObject getHttpGZIPJson(String address, Map<String, String> headers){
		StringBuilder builder = new StringBuilder();
		try{
			URL url = new URL(address);

			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			if(headers != null){
				for(Map.Entry<String, String> header : headers.entrySet()) {
					connect.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			connect.connect();

			if(connect.getResponseCode() != HttpURLConnection.HTTP_OK){
				InputStream in = connect.getErrorStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				in.close();
				connect.disconnect();

				System.out.println("HTTPWARN: " + connect.getResponseMessage());
				return null;
			}

			InputStream in = connect.getInputStream();
			GZIPInputStream gin = new GZIPInputStream(in);

			JSONTokener tokener = new JSONTokener(gin);
			JSONObject json = new JSONObject(tokener);
			in.close();
			connect.disconnect();
			return json;
		}catch(Exception e){
			return null;
		}
	}
}
