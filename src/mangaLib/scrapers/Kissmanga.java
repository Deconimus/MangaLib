package mangaLib.scrapers;

import java.util.ArrayList;
import java.util.List;

import mangaLib.MangaInfo;
import visionCore.util.Web;

public class Kissmanga extends Scraper {

	
	public Kissmanga() {
		
		super("https://kissmanga.com", "https://kissmanga.com/Search/Manga/"+nameReplace);
	}
	
	
	@Override
	public List<MangaInfo> search(String query) {
		
		List<MangaInfo> results = new ArrayList<MangaInfo>();
		
		String searchUrl = getSearchURL(query);
		String html = Web.getHTML(searchUrl, false);
		
		
		
		if (html.toLowerCase().contains("<div class=\"barcontent chapterlist\">")) { // direct hit!
			
			MangaInfo info = new MangaInfo();
			
			String f = "<div class=\"leftSide\">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			f = "<div class=\"barContent\">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			f = "<a class=\"bigChar\" href=";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			String link = html.substring(1, html.toLowerCase().indexOf(">")-1).trim();
			info.url = this.url+link;
			
			f = ">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			info.title = html.substring(0, html.indexOf('<')).trim();
			
			results.add(info);
			
		} else {
		
			String f = "<div class=\"barContent\">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			f = "<table class=\"listing\">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			html = html.substring(0, html.toLowerCase().indexOf("</table>"));
			
			f = "</tr>";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			for (int ind = -1; (ind = html.toLowerCase().indexOf("<tr")) > -1;) {
				html = html.substring(ind+3);
				html = html.substring(html.indexOf(">")+1);
				
				MangaInfo info = new MangaInfo();
				
				f = "<td>";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				f = "<a href=";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				String link = html.substring(1, html.indexOf(">")-1).trim();
				info.url = this.url+link;
				
				f = ">";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				info.title = html.substring(0, html.indexOf('<')).trim();
				
				f = "<td>";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				f = "<a href=";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				f = ">";
				html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
				
				String snd = html.substring(0, html.toLowerCase().indexOf("</a>")).trim().toLowerCase();
				
				if (snd.contains("completed")) {
					
					info.status = "completed";
				}
				
				results.add(info);
			}
			
		}
		
		System.out.println(results);
		
		return results;
	}
	
}
