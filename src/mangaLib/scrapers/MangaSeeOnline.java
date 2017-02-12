package mangaLib.scrapers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Tuple;
import visionCore.util.Files;
import visionCore.util.StringUtils;
import visionCore.util.Web;

public class MangaSeeOnline extends Scraper {
	
	
	public MangaSeeOnline() {
		
		super("https://mangaseeonline.net", "http://mangaseeonline.net/search/?keyword="+nameReplace);
	}
	
	
	@Override
	public List<MangaInfo> search(String query) {
		
		List<MangaInfo> results = new ArrayList<MangaInfo>();
		
		String searchUrl = this.url+"/manga/"+query.toLowerCase().trim().replace(' ', '-').replace("(", "").replace(")", "");
		String html = Web.getHTML(this.url+"/manga/"+query.toLowerCase().trim().replace(' ', '-').replace("(", "").replace(")", ""));
		
		if (html != null) {
		
			html = html.toLowerCase();
			html = html.substring(html.indexOf("<body>"), html.indexOf("</body>")).trim();
			
			if (!html.equals("page not found")) {
				
				MangaInfo info = new MangaInfo();
				
				info.title = query;
				info.url = searchUrl;
				
				results.add(info);
				return results;
			}
		}
		
		List<Tuple<String, String>> directory = getFullDirectory();
		
		String qlc = query.toLowerCase();
		
		for (Tuple<String, String> entry : directory) {
			
			if (entry.x.toLowerCase().contains(qlc)) {
				
				MangaInfo info = new MangaInfo();
				info.title = entry.x;
				info.url = entry.y;
				
				results.add(info);
			}
		}
		
		/* Sane approach (won't work because the site uses javascript to fetch results..):
		
		searchUrl = getSearchURL(query);
		html = Web.getHTML(searchUrl, false);
		
		if (html == null || html.trim().length() < 20) { return results; }
		
		String f = "<div class=\"searchResults\">";
		html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
		
		String fq = "<div class=\"requested\">";
		for (int ind = -1; (ind = html.toLowerCase().indexOf(fq.toLowerCase())+fq.length()) > fq.length()-1;) {
			html = html.substring(ind);
			
			MangaInfo info = new MangaInfo();
			
			f = "<div class=\"resultLink\" href=";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			String link = html.substring(1, html.indexOf("\">")).trim();
			html = html.substring(html.indexOf("\">")+2);
			
			info.url = "";
			info.title = html.substring(0, html.indexOf("</a>")).trim();
			
			f = "<p>";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			f = "<a href=";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			f = ">";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			info.author = html.substring(0, html.indexOf("</a>")).trim();
			
			f = "<p>";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			String statusStr = html.substring(0, html.toLowerCase().indexOf("</p>")).toLowerCase();
			
			if (statusStr.contains("ongoing")) { info.status = "ongoing"; }
			else if (statusStr.contains("complete")) { info.status = "complete"; }
			
			f = "Genre:";
			html = html.substring(html.toLowerCase().indexOf(f.toLowerCase())+f.length());
			
			String genreStr = html.substring(0, html.toLowerCase().indexOf("</p>")).toLowerCase();
			
			String ffs = "<a href=";
			for (int i = -1; (i = genreStr.indexOf(ffs)+ffs.length()) > ffs.length()-1;) {
				genreStr = genreStr.substring(i);
				
				genreStr = genreStr.substring(genreStr.indexOf('>')+1);
				String genre = genreStr.substring(0, genreStr.indexOf('<'));
				
				genre = StringUtils.capitolWords(genre.trim());
				
				info.genres.add(genre);
			}
			
			results.add(info);
		}
		*/
		
		return results;
	}
	
	public List<Tuple<String, String>> getFullDirectory() {
		
		List<Tuple<String, String>> directory = new ArrayList<Tuple<String, String>>();
		
		String html = Web.getHTML(url+"/directory/", false);
		
		String f = "<div id=\"content";
		html = html.substring(html.indexOf(f)+f.length());
		
		String fq = "<p class=\"seriesList chapOnly\">";
		for (int ind = -1; (ind = html.indexOf(fq)+fq.length()) > fq.length()-1;) {
			html = html.substring(ind);
			
			f = "<a";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "href=";
			html = html.substring(html.indexOf(f)+f.length()+1);
			
			String link = url+html.substring(0, html.indexOf("\"")).trim();
			
			f = "</a>";
			String title = html.substring(0, html.indexOf(f));
			
			f = ">";
			title = title.substring(title.lastIndexOf(f)+f.length());
			title = Web.clean(title.trim());
			
			f = "</a>";
			html = html.substring(html.indexOf(f)+f.length());
			
			directory.add(new Tuple<String, String>(title, link));
		}
		
		return directory;
	}
	
}
