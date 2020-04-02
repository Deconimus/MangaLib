package mangaLib.scrapers;

import java.util.ArrayList;
import java.util.List;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Triplet;
import visionCore.util.StringUtils;
import visionCore.util.Web;

public class Mangakakalot extends Scraper {
	
	
	public Mangakakalot() {
		
		super("https://mangakakalot.com", "https://mangakakalot.com/search/"+nameReplace);
	}
	
	
	@Override
	public List<Triplet<String, Double, String>> getChapters(String html) {
		
		List<Triplet<String, Double, String>> chapters = new ArrayList<Triplet<String, Double, String>>();
		
		html = cutBehind("<div class=\"chapter-list\">", html);
		html = cutTill("<div class=\"comment-info", html);
		
		String fq = "<a";
		for (int ind = -1; (ind = html.indexOf(fq) + fq.length()) > fq.length()-1;) {
			html = html.substring(ind);
			
			String a = cutTill(">", html);
			a = cutBehind("href=\"", a);
			String url = cutTill("\"", a).trim();
			
			html = cutBehind(">", html);
			
			String chstr = cutTill("<", html);
			html = cutBehind(">", html);
			
			String nrStr = cutBehind("chapter", chstr.toLowerCase());
			if (nrStr.contains(":"))
				nrStr = cutTill(":", nrStr).trim();
			
			double nr = -1.0;
			try { nr = Double.parseDouble(nrStr); } catch (Exception e) {}
			
			String title = "";
			if (chstr.contains(":"))
				title = cutBehind(":", chstr).trim();
			title = MangaInfo.cleanTitle(title);
			
			if (!url.isEmpty() && nr > -1.0)
				chapters.add(new Triplet<String, Double, String>(url, nr, title));
		}
		
		chapters.sort((lhs, rhs) -> Double.compare(lhs.y, rhs.y));
		
		return chapters;
	}
	
	@Override
	public List<String> getChapterImgUrls(String url) {
		
		List<String> urls = new ArrayList<String>();
		
		String html = Web.getHTML(url, false);
		if (html == null || html.trim().isEmpty()) { return urls; }
		
		String f = "<div class=\"image-container-manga\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		html = cutBehind("<div class=\"vung-doc", html);
		html = cutBehind(">", html);
		html = cutTill("</div>", html);
		
		String fq = "<img";
		for (int ind = -1; (ind = html.indexOf(fq) + fq.length()) > fq.length()-1;) {
			html = html.substring(ind);
			
			String u = cutTill(">", html);
			u = cutBehind("src=\"", u);
			u = cutTill("\"", u);
			
			if (u != null && !u.isEmpty() && !u.endsWith(".gif"))
				urls.add(u);
		}
		
		return urls;
	}
	
	
	@Override
	public List<MangaInfo> searchManga(String query) {
		
		List<MangaInfo> mangas = new ArrayList<MangaInfo>();
		
		return mangas;
	}
	
	@Override
	public MangaInfo getInfo(String url, String html, MangaInfo info) {
		
		if (html == null) { html = Web.getHTML(url, false); }
		
		if (info == null) { info = new MangaInfo(); }
		MangaInfo std = new MangaInfo();
		
		if (info.url.equals(std.url))
			info.url = url;
		
		html = cutBehind("<ul class=\"manga-info-text\">", html);
		
		html = cutBehind("<h1>", html);
		
		if (info.title == std.title)
			info.title = MangaInfo.cleanTitle(cutTill("</h1>", html).trim());
		
		html = cutBehind("Author(s) :", html);
		html = cutBehind("<a", html);
		html = cutBehind(">", html);
		
		if (info.author == std.author)
			html = MangaInfo.cleanTitle(cutTill("<", html));
		
		html = cutBehind("Status :", html);
		
		info.status = cutTill("<", html).toLowerCase().trim();
		
		html = cutBehind("Genres :", html);
		
		if (info.genres.isEmpty()) {
		
			String genresStr = cutTill("</li>", html);
			
			String fq = "<a";
			for (int ind = -1; (ind = genresStr.indexOf(fq) + fq.length()) > fq.length()-1;) {
				genresStr = genresStr.substring(ind);
				
				String g = cutBehind(">", genresStr);
				g = cutTill("<", g);
				g = MangaInfo.cleanTitle(g);
				
				if (g != null && !g.isEmpty())
					info.genres.add(g);
			}
		}
		
		if (info.synopsis == null || (std.synopsis != null && info.synopsis.equals(std.synopsis))) {
			
			html = cutBehind("<div id=\"noidungm", html);
			html = cutBehind("</h2>", html);
			
			info.synopsis = MangaInfo.cleanSynopsis(cutTill("</div>", html));
		}
		
		return info;
	}
	
	@Override
	public String getPosterUrl(String html) {
		
		html = cutBehind("<div class=\"manga-info-pic", html);
		html = cutBehind("<img src=\"", html);
		
		String url = cutTill("\" onerror=", html);
		
		return url;
	}
	
}
