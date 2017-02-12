package mangaLib.scrapers;

import java.util.HashMap;
import java.util.List;

import mangaLib.MangaInfo;
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
					scrapers.put(scraper.url, scraper);
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
	
	
	public abstract List<MangaInfo> search(String query);
	
	
	public String getSearchURL(String query) {
		
		query = query.toLowerCase().trim();
		query = query.replace(" ", "%20");
		
		return searchString.replace(nameReplace, query);
	}
	
	
	public static Scraper getScraper(String url) {
		
		Scraper scraper = scrapers.get(url);
		
		if (scraper == null) {
			
			for (Scraper scr : scrapers.values()) {
				
				if (scr.url.contains(url)) { scraper = scr; break; }
			}
		}
		
		return scraper;
	}
	
}
