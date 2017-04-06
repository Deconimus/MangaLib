package mangaLib.scrapers;

public class MangaSeeOnlineUS extends MangaSeeOnline {

	public MangaSeeOnlineUS() {
		
		super.url = "https://mangaseeonline.us";
		super.searchString = "http://mangaseeonline.us/search/?keyword="+nameReplace;
	}
	
}
