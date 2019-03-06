package com.jaoafa.TomachiMusicBot.Lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;

public class MusixMatch {
	private boolean status = true;
	private String lyrics;
	private String real_artist;

	public MusixMatch(String title, String artist) throws UnsupportedEncodingException{
		Map<String, String> map;
		if(artist != null){
			map = GetMusixMatchLyrics(title, artist);
			if(map != null && map.get("instrumental").equalsIgnoreCase("1")){
				lyrics = "Instrumental";
				return;
			}else if(map != null && map.containsKey("lyrics")){
				if(map.get("lyrics") == null){
					status = false;
					return;
				}
				real_artist = map.get("artist");
				lyrics = map.get("lyrics");
				return;
			}
		}
		map = GetMusixMatchLyrics(title);
		if(map != null && map.get("instrumental").equalsIgnoreCase("1")){
			lyrics = "Instrumental";
			return;
		}else if(map != null && map.containsKey("lyrics")){
			if(map.get("lyrics") == null){
				status = false;
				return;
			}
			real_artist = map.get("artist");
			lyrics = map.get("lyrics");
			return;
		}
		status = false;
		return;
	}

	public Boolean getStatus(){
		return status;
	}
	public String getLyrics(){
		return lyrics;
	}
	public String getRealArtist(){
		return real_artist;
	}

	// apikey = https://github.com/jiteshgupta/lyrickpick/blob/af7cfedd894854442ee4259748bb5c4d4ae546fe/LyrickPick/LyrickPick/Processors/FetchLyrics.cs
	private Map<String, String> GetMusixMatchLyrics(String title) throws UnsupportedEncodingException{
		JSONObject obj = TomachiMusicBot.getHttpJson(
			"http://api.musixmatch.com/ws/1.1/track.search?q_track=" + URLEncoder.encode(title, "UTF-8") + "&apikey=8b7654870c8395335a30eb19039218f6",
			null
		);
		try{
			obj.getJSONObject("message").getJSONObject("body");
		}catch(JSONException e){
			return null;
		}
		JSONArray track_list = obj.getJSONObject("message").getJSONObject("body").getJSONArray("track_list");
		if(track_list.length() != 1){
			return null;
		}
		JSONObject track = track_list.getJSONObject(0);
		Map<String, String> r = new HashMap<>();
		r.put("lyrics", GetMusixMatchLyrics(track.getJSONObject("track").getInt("track_id")));
		r.put("artist", track.getJSONObject("track").getString("artist_name"));
		r.put("instrumental", String.valueOf(track.getJSONObject("track").getInt("instrumental")));
		return r;
	}

	private Map<String, String> GetMusixMatchLyrics(String title, String artist) throws UnsupportedEncodingException{
		JSONObject obj = TomachiMusicBot.getHttpJson(
			"http://api.musixmatch.com/ws/1.1/track.search?q_track=" + URLEncoder.encode(title, "UTF-8") + "&q_track_artist=" + URLEncoder.encode(artist, "UTF-8") + "&apikey=8b7654870c8395335a30eb19039218f6",
			null
		);
		try{
			obj.getJSONObject("message").getJSONObject("body");
		}catch(JSONException e){
			return null;
		}
		JSONArray track_list = obj.getJSONObject("message").getJSONObject("body").getJSONArray("track_list");
		if(track_list.length() == 1){
			return null;
		}
		JSONObject track = track_list.getJSONObject(0);
		Map<String, String> r = new HashMap<>();
		r.put("lyrics", GetMusixMatchLyrics(track.getJSONObject("track").getInt("track_id")));
		r.put("artist", track.getJSONObject("track").getString("artist_name"));
		r.put("instrumental", String.valueOf(track.getJSONObject("track").getInt("instrumental")));
		return r;
	}
	private String GetMusixMatchLyrics(int track_id){
		// usertoken = https://github.com/AryToNeX/MPRISLyrics/blob/30d582bdad53e8c0628bd4b374166a961dea6e73/src/AryToNeX/MPRISLyrics/providers/Musixmatch.php
		// usertoken = https://github.com/theweavrs/BreadPlayer/blob/3abfe683b828fbb486d9e1c59d2413968e805396/BreadPlayer.Web/Musixmatch/MusixmatchClient.cs
		Map<String, String> headers = new HashMap<>();
		headers.put("cookie", "x-mxm-user-id=; x-mxm-token-guid=e08e6c63-edd1-4207-86dc-d350cdf7f4bc; mxm-encrypted-token=; AWSELB=55578B011601B1EF8BC274C33F9043CA947F99DCFF6AB1B746DBF1E96A6F2B997493EE03F2DD5F516C3BC8E8DE7FE9C81FF414E8E76CF57330A3F26A0D86825F74794F3C94");
		headers.put("cache-control", "no-cache");
		headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
		headers.put("upgrade-insecure-requests", "1");
		headers.put("accept-language", "en-US,en;q=0.8");
		headers.put("accept-encoding", "gzip, deflate");
		headers.put("dnt", "1");
		JSONObject obj = TomachiMusicBot.getHttpGZIPJson(
			"https://apic-desktop.musixmatch.com/ws/1.1/track.lyrics.get?format=json&track_id=" + track_id + "&user_language=ja&f_subtitle_length=0&f_subtitle_length_max_deviation=0&subtitle_format=lrc&app_id=web-desktop-app-v1.0&guid=e08e6c63-edd1-4207-86dc-d350cdf7f4bc&usertoken=1710144894f79b194e5a5866d9e084d48f227d257dcd8438261277",
			headers
		);
		try{
			obj.getJSONObject("message").getJSONObject("body");
		}catch(JSONException e){
			return null;
		}
		if(!obj.getJSONObject("message").getJSONObject("body").has("lyrics")){
			// なかったらreturn
			return null;
		}

		return obj.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics").getString("lyrics_body").replaceAll("\n", "\r\n");
	}

}
