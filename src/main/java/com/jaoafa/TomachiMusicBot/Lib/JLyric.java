package com.jaoafa.TomachiMusicBot.Lib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JLyric {
	public static String search(String title, String artist) {
		try {
			String url = getLyricsUrl(title, artist);

			if(url == null || url.equals("")){
				return null;
			}
			Document musicPage = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0").get();
			Element lyricsEle = musicPage.body().select("p#Lyric").first();
			String html = lyricsEle.html();
			html = html.replaceAll(Pattern.quote("<br />"), "\n");
			html = html.replace(Pattern.quote("<br>"), "\n");
			return html;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static String getLyricsUrl(String title, String artist) throws IOException{
		String searchUrl;
		if(artist != null){
			searchUrl = String.format("http://search2.j-lyric.net/index.php?ct=0&ka=&ca=0&kl=&cl=0&kt=%s&ka=%s", URLEncoder.encode(title, "UTF-8"), URLEncoder.encode(artist, "UTF-8"));
		}else{
			searchUrl = String.format("http://search2.j-lyric.net/index.php?ct=0&ka=&ca=0&kl=&cl=0&kt=%s", URLEncoder.encode(title, "UTF-8"));
		}
		Document searchPage = Jsoup.connect(searchUrl)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0").get();
		Element searchEle = searchPage.body().select(".bdy").first();

		if(searchEle == null){
			return null;
		}
		Elements eles = searchEle.select(".mid > a");
		if(artist == null && eles.size() != 1){
			return null;
		}
		String url = eles.attr("href");
		return url;
	}
}
