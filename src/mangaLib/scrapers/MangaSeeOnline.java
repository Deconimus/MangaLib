package mangaLib.scrapers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Triplet;
import visionCore.dataStructures.tuples.Tuple;
import visionCore.util.Web;

public class MangaSeeOnline extends Scraper {
	
	
	public MangaSeeOnline() {
		
		super("https://mangaseeonline.net", "http://mangaseeonline.net/search/?keyword="+nameReplace);
	}
	
	
	@Override
	public MangaInfo getInfo(String url, String html, MangaInfo info) {
		
		if (html == null) { html = Web.getHTML(url, false); }
		
		if (info == null) { info = new MangaInfo(); }
		MangaInfo std = new MangaInfo();
		
		if (info.url.equals(std.url)) {
			
			info.url = url;
		}
		
		if (info.title.equals(std.title)) {
			
			html = cutBehind("<h1 class=\"SeriesName\">", html);
			
			info.title = cutTill("</h1>", html).trim();
		}
		
		if (info.author.equals(std.author) || info.artist.equals(std.artist)) {
			
			String fq = "/search/?author=";
			for (int ind = -1, i = 0; (ind = html.indexOf(fq)) > -1; i++) {
				html = html.substring(ind + fq.length());
				
				String s = cutTill("\">", html.replace('\'', '"')).trim();
				s = s.replace('_', ' ');
				
				if (i == 0) {
					
					info.artist = s;
					
				} else {
					
					info.author = s;
					break;
				}
			}
		}
		
		if (info.genres.isEmpty()) {
			
			String fq = "/search/?genre=";
			for (int ind = -1; (ind = html.indexOf(fq)) > -1;) {
				html = html.substring(ind + fq.length());
				
				String s = cutTill("\">", html.replace('\'', '"')).trim().replace('_', ' ');
				info.genres.add(s);
			}
		}
		
		if (info.released == std.released) {
			
			html = cutBehind("/search/?year=", html);
			String s = cutTill("\">", html.replace('\'', '"')).trim();
			
			try { info.released = (int)Double.parseDouble(s); } catch (Exception | Error e) {}
		}
		
		html = cutBehind("/search/?status=", html);
		info.status = cutTill("\">", html).trim().toLowerCase();
		
		if (info.synopsis.equals(std.synopsis)) {
			
			html = cutBehind("<div class=\"description\">", html.replace('\'', '"'));
			
			String s = cutTill("</div>", html);
			
			info.synopsis = s.trim();
		}
		
		return info;
	}
	
	@Override
	public List<Triplet<String, Double, String>> getChapters(String html) {
		
		List<Triplet<String, Double, String>> chapters = new ArrayList<Triplet<String, Double, String>>();
		
		html = cutBehind("<div class=\"list chapter-list\">", html);
		
		boolean fixChapterNrs = false;
		
		String fq = "<a";
		for (int ind = -1; (ind = html.indexOf(fq) + fq.length()) > fq.length()-1;) {
			html = html.substring(ind);
			
			String a = cutTill(">", html);
			html = cutBehind(">", html);
			
			String chf = "chapter=\"";
			int chind = a.toLowerCase().indexOf(chf);
			if (chind < 0) { continue; }
			
			a = a.substring(chind+chf.length());
			String chnrstr = cutTill("\"", a).trim();
			
			double chnr = -1.0;
			
			if (!chnrstr.trim().isEmpty()) {
				
				try { chnr = Double.parseDouble(chnrstr); } catch (Exception | Error e) { continue; }
			}
			
			a = cutBehind("href=\"", a);
			
			String url = cutTill("\"", a).trim();
			url = cutTill("-page", url) + ".html";
			url = this.url+url;
			
			chapters.add(new Triplet<String, Double, String>(url, chnr, ""));
			
			if (!fixChapterNrs && url.toLowerCase().contains("index")) {
				
				fixChapterNrs = true;
			}
			
		}
		
		if (fixChapterNrs) {
			
			List<List<Triplet<String, Double, String>>> indexChapters = new ArrayList<List<Triplet<String, Double, String>>>();
			
			for (Triplet<String, Double, String> ch : chapters) {
				
				int ind = 0;
				
				if (ch.x.contains("index")) {
				
					String s = cutTill("-", cutBehind("index-", ch.x));
					try { ind = (int)Double.parseDouble(s); } catch (Exception | Error e) { continue; }
					
					ind -= 1;
				}
				
				while (ind+1 > indexChapters.size()) { indexChapters.add(new ArrayList<Triplet<String, Double, String>>()); }
				
				indexChapters.get(ind).add(ch);
			}
			
			chapters.clear();
			
			for (int ind = 0, off = 0, ln = indexChapters.size(); ind < ln; ind++, off += indexChapters.get(ind-1).size()) {
				List<Triplet<String, Double, String>> chlst = indexChapters.get(ind);
				
				for (Triplet<String, Double, String> ch : chlst) {
					
					ch.y += off;
					chapters.add(ch);
				}
			}
		}
		
		Collections.sort(chapters, (ch0, ch1) -> Double.compare(ch0.y, ch1.y));
		
		return chapters;
	}
	
	
	@Override
	public List<String> getChapterImgUrls(String url) {
		
		List<String> imgUrls = new ArrayList<String>();
		
		if (url.contains("-page-")) { url = url.substring(0, url.lastIndexOf("-page-"))+".html"; }
		
		String html = Web.getHTML(url, false);
		if (html == null || html.trim().isEmpty()) { return imgUrls; }
		
		String f = "<div class=\"image-container-manga\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		html = cutTill("<div style=", html);
		
		f = "<div class=\"fullchapimage";
		String f1 = "<div class='fullchapimage";
		
		for (int indf0 = -1, indf1 = -1; (indf0 = html.indexOf(f)) >= 0 || (indf1 = html.indexOf(f1)) >= 0;) {
			
			int ind = (indf0 > -1) ? indf0 : indf1;
			if (indf0 >= 0 && indf1 >= 0) { ind = Math.min(indf0, indf1); }
			
			html = html.substring(ind+f.length());
			html = html.substring(html.indexOf("<img src=")+10);
			
			String imgurl = html.substring(0, html.indexOf(">")-1);
			
			imgUrls.add(imgurl);
		}
		
		return imgUrls;
	}
	
	
	@Override
	public String getPosterUrl(String html) {
		
		html = html.replace('\'', '"');
		html = cutBehind("<div class=\"well mainWell\">", html);
		html = cutBehind("<div class=\"row\">", html);
		html = cutBehind("<div", html);
		html = cutBehind("<img src=\"", html);
		html = cutTill("\">", html);
		
		html = html.replace(" ", "").replace("\t", "").replace("\n", "");
		if (html.contains("\"/>")) { html = cutTill("\"/>", html); }
		
		return html;
	}
	
	
	@Override
	public List<MangaInfo> searchManga(String query) {
		
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
