package com.jaoafa.TomachiMusicBot.Lib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JLyric {
	public static String search(String title, String artist) {
		try {
			Document searchPage = Jsoup.connect(String.format("http://search2.j-lyric.net/index.php?ct=0&ka=&ca=0&kl=&cl=0&kt=%s&ka=%s", URLEncoder.encode(title, "UTF-8"), URLEncoder.encode(artist, "UTF-8")))
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0").get();
			Element searchEle = searchPage.body().select(".bdy").first();

			if(searchEle == null){
				return "歌詞が取得できませんでした。(1)";
			}

			String url = searchEle.select(".mid > a").attr("href");
			if(url == null || url.equals("")){
				return "歌詞が取得できませんでした。(2)";
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
			return "歌詞が取得できませんでした。(3)";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return "歌詞が取得できませんでした。(4)";
		}
	}
}
