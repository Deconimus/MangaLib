package mangaLib.scrapers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mangaLib.MangaInfo;
import visionCore.dataStructures.tuples.Triplet;
import visionCore.util.StringUtils;
import visionCore.util.Web;

public class MangaFox extends Scraper {

	
	public MangaFox() {
		
		super("http://mangafox.me", srchurl());
	}
	
	
	@Override
	public List<MangaInfo> searchManga(String query) {
		
		List<MangaInfo> results = new ArrayList<MangaInfo>();
		
		String html = Web.getHTML(getSearchURL(query), false);
		
		String f = "<div id=\"mangalist\">";
		String list = html.substring(html.indexOf(f)+f.length());
		
		f = "<ul class=\"list\">";
		list = list.substring(list.indexOf(f)+f.length(), list.indexOf("</ul>"));
		
		for (int i = 0; i < 10000 && list.contains("<li>") && list.contains("</li>"); i++) {
			
			String entry = list.substring(list.indexOf("<li>")+4);
			entry = entry.substring(0, entry.indexOf("</li>"));
			
			list = list.substring(entry.length());
			
			f = "<div class=\"manga_text\">";
			entry = entry.substring(entry.indexOf(f)+f.length());
			
			f = "href=";
			entry = entry.substring(entry.indexOf(f)+f.length()+1);
			
			String entryUrl = entry.substring(0, entry.indexOf("rel=")-2);
			
			entry = entry.substring(entry.indexOf(">")+1);
			
			f = "</a>";
			String entryTitle = entry.substring(0, entry.indexOf(f));
			
			MangaInfo info = new MangaInfo();
			info.title = entryTitle;
			info.url = entryUrl;
			
			if (info.url.startsWith("//")) { info.url = "http:"+info.url; }
			
			results.add(info);
		}
		
		sortResults(results, query);
		
		return results;
	}
	
	
	@Override
	public MangaInfo getInfo(String url, String html, MangaInfo info) {
		
		if (info == null) { info = new MangaInfo(); }
		MangaInfo std = new MangaInfo();
		
		if (html == null) { html = Web.getHTML(url, false); }
		
		if (info.url.equals(std.url)) { info.url = url; }
		if (info.title.equals(std.title)) { info.title = getTitle(html); }
		
		info.status = getStatus(html);
		
		if (info.synopsis.equals(std.synopsis) || info.artist.equals(std.artist) || info.author.equals(std.author) ||
			info.genres.isEmpty() || info.released == std.released) {
		
			String f = "<div id=\"title\"";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "<table>";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "<tbody>";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "<a";
			html = html.substring(html.indexOf(f)+f.length());
			html = html.substring(html.indexOf(">")+1);
			
			String released = html.substring(0, html.indexOf("</a>"));
			released = released.trim();
			
			int year = -1;
			try { year = Integer.parseInt(released); } catch (Exception | Error e1) {}
			
			if (info.released == std.released) {
				
				info.released = year;
			}
			
			f = "<a";
			html = html.substring(html.indexOf(f)+f.length());
			html = html.substring(html.indexOf(">")+1);
			
			String author = html.substring(0, html.indexOf("</a>")).trim();
			
			if (info.author.equals(std.author)) {
			
				info.author = Web.clean(author.replace("<", "").replace(">", ""));
			}
			
			f = "<a";
			html = html.substring(html.indexOf(f)+f.length());
			html = html.substring(html.indexOf(">")+1);
			
			String artist = html.substring(0, html.indexOf("</a>")).trim();
			
			if (info.artist.equals(std.artist)) {
			
				info.artist = Web.clean(artist.replace("<", "").replace(">", ""));
			}
			
			f = "<td valign=\"top\">";
			html = html.substring(html.indexOf(f)+f.length());
			
			String genreHtml = html.substring(0, html.indexOf("</td>"));
			
			boolean addGenres = info.genres.isEmpty();
			
			for (int i = 0; i < 1000 && genreHtml.contains("<a href") && genreHtml.contains("</a>"); i++) {
				
				genreHtml = genreHtml.substring(genreHtml.indexOf("<a href"));
				genreHtml = genreHtml.substring(genreHtml.indexOf(">")+1);
				
				String g = genreHtml.substring(0, genreHtml.indexOf("</a>")).trim();
				
				if (addGenres) {
				
					info.genres.add(Web.clean(g));
				}
				
				genreHtml = genreHtml.substring(genreHtml.indexOf("</a>")+4);
			}
			
			f = "<p class=\"summary less\">";
			
			int ind = html.indexOf(f);
			if (ind == -1) { f = "<p class=\"summary\">"; ind = html.indexOf(f); }
			
			html = html.substring(html.indexOf(f)+f.length());
			
			String synopsis = html.substring(0, html.indexOf("</p>"));
			synopsis = MangaInfo.cleanSynopsis(synopsis);
			
			if (info.synopsis.equals(std.synopsis)) {
			
				info.synopsis = synopsis;
			}
		}
		
		return info;
	}
	
	
	@Override
	public List<Triplet<String, Double, String>> getChapters(String html) {
		
		String f = "id=\"chapters";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = ">";
		html = html.substring(html.indexOf(f)+f.length());
		
		List<Triplet<String, Double, String>> chapters = new ArrayList<Triplet<String, Double, String>>();
		
		for (int i = 0; i < 10000 && html.contains("<li>") && ((html.contains("<h3>") && html.contains("</h3>")) || (html.contains("<h4>") && html.contains("</h4>"))); i++) {
			
			html = html.substring(html.indexOf("<li>")+4);
			
			boolean h3 = true;
			
			int ind = html.indexOf("<h3>");
			if (ind == -1 || (html.indexOf("<h4>") != -1 && ind > html.indexOf("<h4>"))) {
				
				h3 = false;
			}
			
			f = h3 ? "<h3>" : "<h4>";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "<a href=";
			String chapterUrl = html.substring(html.indexOf(f)+f.length()+1, html.indexOf(" title=")-1).trim();
			
			String parsenr = html.substring(html.indexOf(">")+1, html.indexOf("</a>")).trim();
			parsenr = parsenr.substring(parsenr.lastIndexOf(' ')).trim();
			
			double chapterNr = -1;
			
			try {
				
				chapterNr = Double.parseDouble(parsenr);
				
			} catch (Exception | Error e) { try { chapterNr = Integer.parseInt(parsenr); } catch (Exception | Error e1) {} }
			
			html = html.substring(html.indexOf("</a>")+4);
			
			String chapterTitle = "";
			
			f = h3 ? "</h3>" : "</h4>";
			int nindx = html.indexOf(f);
			
			if (nindx != -1 && nindx > html.indexOf("<span") && html.indexOf("<span") != -1) {
				
				chapterTitle = html.substring(html.indexOf(">")+1, html.indexOf("</span>"));
				chapterTitle = chapterTitle.replace("\\", "-");
				chapterTitle = chapterTitle.replace("/", "-");
				chapterTitle = chapterTitle.replace("\"", "");
				chapterTitle = chapterTitle.replace("'", "");
				chapterTitle = chapterTitle.replace("*", "");
				chapterTitle = chapterTitle.replace("?", "");
				chapterTitle = chapterTitle.replace("<", "[");
				chapterTitle = chapterTitle.replace(">", "]");
				chapterTitle = chapterTitle.replace("|", "-");
				chapterTitle = chapterTitle.replace(":", " -");
				chapterTitle = chapterTitle.replace("�", "ss");
				chapterTitle = chapterTitle.replace(".....", "");
				chapterTitle = chapterTitle.replace("....", "");
				chapterTitle = chapterTitle.replace("...", "");
				chapterTitle = chapterTitle.replace("..", "");
				chapterTitle = Web.clean(chapterTitle).trim();
				chapterTitle = chapterTitle.replaceAll("[^ -~]", "");
				
				while (chapterTitle.endsWith(".")) { chapterTitle = chapterTitle.substring(0, chapterTitle.length()-1); }
			}
			
			if (chapterNr > 0) { // not saving pilots
			
				if (chapterNr != -1) {
					
					chapters.add(new Triplet<String, Double, String>(chapterUrl, chapterNr, chapterTitle));
					
				} else { System.out.println("Invalid chapter nr \""+parsenr+"\""); }
			}
			
			f = h3 ? "</h3>" : "</h4>";
			html = html.substring(html.indexOf(f)+f.length());
		}
		
		Collections.sort(chapters, (ch0, ch1) -> Double.compare(ch0.y, ch1.y));
		
		return chapters;
	}
	
	
	@Override
	public List<String> getChapterImgUrls(String url) {
		
		if (url.startsWith("//")) { url = url.substring(2); }
		if (!url.startsWith("https://") && !url.startsWith("http://")) { url = "http://"+url; }
		
		List<String> imgUrls = new ArrayList<String>(20);
		
		String html = Web.getDecodedHTML(url, false);
		
		for (int t = 0; t < 100 && (html == null || html.trim().length() <= 5); t++) {
			
			html = Web.getDecodedHTML(url, false);
			try { Thread.sleep(50); } catch (Exception | Error e3) { }
		}
		
		try {
		
			html = cutBehind("<div class=\"widepage page\">", html);
			html = cutBehind("<div id=\"top_center_bar\">", html);
			html = cutBehind("<div class=\"r m\">", html);
			html = cutBehind("<div class=\"1\">", html);
			html = cutBehind("</select>", html);
			html = cutBehind("of", html);
			
			String toparse = html.substring(0, html.indexOf("<")).trim();
			toparse = toparse.replace("\"", "");
			toparse = toparse.trim();
			
			int chlength = 0;
			try { chlength = (int)Double.parseDouble(toparse); } catch (Exception | Error e1) {}
			
			String[] urls = new String[chlength];
			
			ExecutorService exec = Executors.newCachedThreadPool();
			
			try {
			
				for (int i = 0; i < chlength; i++) {
					
					final int ind = i;
					final String link = url.substring(0, url.lastIndexOf("/")+1)+(ind+1)+".html";
					
					exec.submit(new Runnable(){
						
						@Override
						public void run() {
							
							String htm = Web.getDecodedHTML(link, false);
							
							for (int t = 0; t < 100 && htm == null || htm.trim().length() <= 5; t++) {
								
								htm = Web.getDecodedHTML(link, false);
								try { Thread.sleep(50); } catch (Exception | Error e3) { }
							}
							
							String f = "id=\"viewer\">";
							htm = htm.substring(htm.indexOf(f)+f.length());
							
							f = "<div class=\"read_img\">";
							htm = htm.substring(htm.indexOf(f)+f.length());
							
							f = "<a href=";
							htm = htm.substring(htm.indexOf(f)+f.length());
							
							f = ">";
							htm = htm.substring(htm.indexOf(f)+f.length());
							
							String imgurl = htm.substring(htm.indexOf("<img src=")+10, htm.indexOf("width="));
							imgurl = imgurl.substring(0, imgurl.indexOf("\""));
							
							urls[ind] = imgurl;
						}
					});
				}
				
			} finally { exec.shutdown(); }
			
			try { exec.awaitTermination(30, TimeUnit.MINUTES); } 
			catch (Exception | Error e2) { e2.printStackTrace(); }
			
			for (String u : urls) {
				
				imgUrls.add(u);
			}
			
		} catch (Exception | Error e) { e.printStackTrace(); return new ArrayList<String>(1); }
		
		return imgUrls;
	}
	
	
	@Override
	public String getPosterUrl(String html) {
		
		html = cutBehind("<div class=\"cover\">", html);
		html = cutBehind("<img", html);
		html = cutBehind("src=\"", html);
		
		return cutTill("\"", html);
	}
	
	
	private static String getTitle(String html) {
		
		String title = "";
		
		String f = "<div id=\"title\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<h2>";
		int h2ind = html.indexOf(f);
		
		if (h2ind == -1 || html.indexOf("</div>") < h2ind) {
			
			f = "<h1";
			html = html.substring(html.indexOf(f)+f.length());
			
			f = ">";
			html = html.substring(html.indexOf(f)+f.length());
			
			title = html.substring(0, html.toLowerCase().indexOf("manga"));
			title = title.toLowerCase().trim();
			title = StringUtils.capitolWords(title);
			
		} else {
			
			html = html.substring(html.indexOf(f)+f.length());
			
			f = "<a href=";
			html = html.substring(html.indexOf(f)+f.length());
			
			title = html.substring(html.indexOf(">")+1, html.indexOf("Manga</a>")-1);
		}
		
		return title;
	}
	
	private static String getStatus(String html) {
		
		String f = "<div id=\"series_info\">";
		html = html.substring(html.toLowerCase().indexOf(f)+f.length());
		
		f = "<div class=\"data\">";
		html = html.substring(html.toLowerCase().indexOf(f)+f.length());
		
		f = "<h5>status:</h5>";
		html = html.substring(html.toLowerCase().indexOf(f)+f.length());
		
		f = "<span>";
		html = html.substring(html.toLowerCase().indexOf(f)+f.length());
		
		String status = html.substring(0, html.indexOf("</span>")).trim();
		if (status.contains(" ")) { status = status.substring(0, status.indexOf(" ")); }
		
		status = status.toLowerCase();
		status = status.replace(".", "").replace(",", "");
		status = status.replaceAll("[^ -~]", "");
		status = status.trim();
		
		return status;
	}
	
	@SuppressWarnings("unchecked")
	public static void sortResults(List results, String title) {
		
		String t = title; // �\_(`-`)_/�
		String[] words = t.split(" ");
		
		for (int i = 0; i < words.length; i++) {
			
			words[i] = words[i].trim().toLowerCase();
		}
		
		Collections.sort(results, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				
				String s0 = getS(arg0);
				String s1 = getS(arg1);
				
				return compare(s0, s1);
			}
			
			public String getS(Object o) {
				
				if (o instanceof String) { return ((String)o).trim(); }
				if (o instanceof MangaInfo) { return ((MangaInfo)o).title.trim(); }
				if (o instanceof File) { return ((File)o).getName().trim(); }
				return o.toString();
			}
			
			public int compare(String o1, String o2) {
				
				if (o1.toLowerCase().equals(t)) { return -1; }
				if (o2.toLowerCase().equals(t)) { return 1; }
				if (o1.toLowerCase().contains(t)) { return -1; }
				if (o2.toLowerCase().contains(t)) { return 1; }
				
				for (int i = words.length; i >= 1; i--) {
					
					boolean b1 = StringUtils.containsNum(o1.toLowerCase(), i, words);
					boolean b2 = StringUtils.containsNum(o2.toLowerCase(), i, words);
					
					if (b1 != b2) {
						
						return (b1) ? -1 : 1;
					}
					
				}
				
				return 0;
			}

		});
		
	}
	
	protected static String srchurl() {
		
		return "http://mangafox.me/search.php?name_method=cw&name="+nameReplace
				+"&type=&author_method=cw&author=&artist_method=cw&artist="
				+"&genres%5BAction%5D=0&genres%5BAdult%5D=0&genres%5BAdventure%5D"
				+"=0&genres%5BComedy%5D=0&genres%5BDoujinshi%5D=2&genres%5BDrama%5D="
				+"0&genres%5BEcchi%5D=0&genres%5BFantasy%5D=0&genres%5BGender+Bender"
				+"%5D=0&genres%5BHarem%5D=0&genres%5BHistorical%5D=0&genres%5BHorror%"
				+"5D=0&genres%5BJosei%5D=0&genres%5BMartial+Arts%5D=0&genres%5BMature%"
				+"5D=0&genres%5BMecha%5D=0&genres%5BMystery%5D=0&genres%5BOne+Shot%5D=0"
				+"&genres%5BPsychological%5D=0&genres%5BRomance%5D=0&genres%5BSchool+"
				+"Life%5D=0&genres%5BSci-fi%5D=0&genres%5BSeinen%5D=0&genres%5BShoujo%"
				+"5D=0&genres%5BShoujo+Ai%5D=0&genres%5BShounen%5D=0&genres%5BShounen+Ai%"
				+"5D=0&genres%5BSlice+of+Life%5D=0&genres%5BSmut%5D=0&genres%5BSports%5D=0"
				+"&genres%5BSupernatural%5D=0&genres%5BTragedy%5D=0&genres%5BWebtoons%5D=0&"
				+"genres%5BYaoi%5D=0&genres%5BYuri%5D=0&released_method=eq&released=&rating_"
				+"method=eq&rating=&is_completed=&advopts=1";
	}

	
}
