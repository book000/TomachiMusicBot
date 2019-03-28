package com.jaoafa.TomachiMusicBot.Lib;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;

public class Utamap {
	public static String search(String title, String artist) throws UnsupportedEncodingException{
		JSONObject obj;
		if(artist != null){
			obj = TomachiMusicBot.getHttpJson(
				"https://www.googleapis.com/customsearch/v1?key=" + TomachiMusicBot.GOOGLE_API_KEY + "&cx=" + TomachiMusicBot.CUSTOM_SEARCH_ENGINE_ID + "&q=" + URLEncoder.encode(title, "UTF-8") + " " + URLEncoder.encode(artist, "UTF-8"),
				null
			);
			if(!obj.has("items")){
				// タイトルのみ
				obj = TomachiMusicBot.getHttpJson(
					"https://www.googleapis.com/customsearch/v1?key=" + TomachiMusicBot.GOOGLE_API_KEY + "&cx=" + TomachiMusicBot.CUSTOM_SEARCH_ENGINE_ID + "&q=" + URLEncoder.encode(title, "UTF-8"),
					null
				);
				if(!obj.has("items")){
					return null;
				}
			}
		}else{
			obj = TomachiMusicBot.getHttpJson(
				"https://www.googleapis.com/customsearch/v1?key=" + TomachiMusicBot.GOOGLE_API_KEY + "&cx=" + TomachiMusicBot.CUSTOM_SEARCH_ENGINE_ID + "&q=" + URLEncoder.encode(title, "UTF-8"),
				null
			);
		}
		if(!obj.has("items")){
			return null;
		}
		JSONArray items = obj.getJSONArray("items");
		if(items.length() == 0){
			return null;
		}
		String address = items.getJSONObject(0).getString("link");
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			return null;
		}
		if(url.getQuery() == null){
			return null;
		}
		String[] params = url.getQuery().split("&");
		Map<String, String> querys = new HashMap<String, String>();
        for (String param : params){
			String[] splitted = param.split("=");
			querys.put(splitted[0], splitted[1]);
	    }
        if(!querys.containsKey("surl")){
			return null;
        }
        String surl = querys.get("surl");
        String html = TomachiMusicBot.getHttpHTML("http://www.utamap.com/phpflash/flashfalsephp.php?unum=" + surl, null);
        if(html.indexOf("test2=") == -1){
			return null;
        }
        int i = html.indexOf("test2=") + "test2=".length();

        String lyrics = html.substring(i).replaceAll("\r\n", "\n");
        return lyrics;
	}
}
