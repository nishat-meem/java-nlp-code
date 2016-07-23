package shafin.web.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import shafin.nlp.util.FileHandler;

public class Spider {

	public String FOLDER_PATH = "D:/home/dw/";
	public String HISTORY_PATH = FOLDER_PATH + "dw.q";
	public String FILE_PATH = FOLDER_PATH + "dw.txt";
	public Queue<String> urlQueue;
	public LinkExtractor extractor;
	public UrlDB db;

	private int counter;

	public Spider(Config config) {
		this.extractor = new LinkExtractor(config);
		this.db = new UrlDB();
		this.urlQueue = new LinkedList<>();
	}

	private void loadStoredLinksInQueue() {
		File file = new File(FILE_PATH);
		String queueHead = FileHandler.readFileAsSingleString(HISTORY_PATH);
		boolean headFound = false;

		if (file.exists()) {
			List<String> list = FileHandler.readFile(FILE_PATH);
			for (String l : list) {
				this.db.insert(l);

				if (l.equals(queueHead)) {
					headFound = true;
				}
				if (headFound) {
					this.urlQueue.add(l);
				}

				System.out.println("LOADING: " + ++counter + " Q[" + this.urlQueue.size() + "] " + l);
			}
		}
	}

	public void process(String seed) {

		loadStoredLinksInQueue();

		do {
			try {
				this.extractor.setURL(seed);
				List<String> urlList = this.extractor.extractURL();

				for (String url : urlList) {
					if (!db.isExists(url)) {
						urlQueue.add(url);
						db.insert(url);
						System.out.println("INSERTING: " + ++counter + " Q[" + this.urlQueue.size() + "] " + url);
						FileHandler.appendFile(FILE_PATH, url + "\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			seed = urlQueue.poll();
			FileHandler.writeFile(HISTORY_PATH, seed);
		} while (!urlQueue.isEmpty());
	}

	public static void main(String[] args) {
		String DOMAIN = "http://www.dw.com";
		String FILTER = "\\/bn\\/\\.*";
		List<String> excludeStrings = new ArrayList<>();
		excludeStrings.add("m.dw.com");
		excludeStrings.add("/search/");

		Config config = new Config(DOMAIN, FILTER, excludeStrings);

		Spider spider = new Spider(config);
		spider.process("http://www.dw.com/bn/");
	}
}
