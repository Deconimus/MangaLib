package mangaLib.scrapers;

import java.util.HashMap;
import java.util.List;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Triplet;
import visionCore.dataStructures.tuples.Tuple;
import visionCore.reflection.Classes;

public abstract class Scraper {
	
	
	public static final HashMap<String, Scraper> scrapers;
	
	static {
		
		scrapers = new HashMap<String, Scraper>();
		
		List<Class> classes = Classes.getClassList("mangaLib.scrapers", Scraper.class, null);
		
		for (Class cl : classes) {
			
			try {
				Object obj = cl.newInstance();
				
				if (obj instanceof Scraper) {
					
					Scraper scraper = (Scraper)obj;
					scrapers.put(trimKey(scraper.url), scraper);
				}
				
			} catch (Exception | Error e) {}
		}
	}
	
	
	protected static final String nameReplace = "[{name}]";
	
	
	public String url, searchString;
	
	
	public Scraper(String url, String searchString) {
		
		this.url = url;
		this.searchString = searchString;
	}
	
	
	public abstract List<MangaInfo> searchManga(String query);
	
	public abstract MangaInfo getInfo(String url, String html, MangaInfo info);
	public abstract List<Triplet<String, Double, String>> getChapters(String html);
	public abstract List<String> getChapterImgUrls(String url);
	
	public abstract String getPosterUrl(String html);
	
	
	public String getSearchURL(String query) {
		
		query = query.toLowerCase().trim();
		query = query.replace(" ", "%20");
		
		return searchString.replace(nameReplace, query);
	}
	
	
	public static Scraper getScraper(String url) {
		
		url = trimKey(url);
		
		Scraper scraper = scrapers.get(url);
		
		if (scraper == null) {
			
			for (Scraper scr : scrapers.values()) {
				
				String k = trimKey(scr.url);
				
				if (url.contains(k) || k.contains(url)) { scraper = scr; break; }
			}
		}
		
		return scraper;
	}
	
	
	public static String trimKey(String key) {
		
		if (key.startsWith("http://") || key.startsWith("https://")) { key = key.substring(key.indexOf("://")+3); }
		if (key.startsWith("ww2.") || key.startsWith("www.")) { key = key.substring(key.indexOf(".")+1); }
		
		return key;
	}
	
	
	protected String cutBehind(String f, String html) {
		
		int ind = html.indexOf(f);
		return html.substring((ind < 0) ? 0 : ind + f.length());
	}
	
	protected String cutBehindIC(String f, String html) {
		
		int ind = html.toLowerCase().indexOf(f.toLowerCase());
		return html.substring((ind < 0) ? 0 : ind + f.length());
	}
	
	protected String cutTill(String f, String html) {
		
		int ind = html.indexOf(f);
		return html.substring(0, (ind < 0) ? html.length() : ind);
	}
	
	protected String cutTillIC(String f, String html) {
		
		int ind = html.toLowerCase().indexOf(f.toLowerCase());
		return html.substring(0, (ind < 0) ? html.length() : ind);
	}
	
}
