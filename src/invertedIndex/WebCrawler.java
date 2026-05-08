package invertedIndex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;

public class WebCrawler {

    public static class CrawledPage {
        public int id;          // sequential document ID (0-based)
        public String url;      // full URL of this page
        public String title;    // page title (from <title> tag)
        public String text;     // visible body text (cleaned)

        public CrawledPage(int id, String url, String title, String text) {
            this.id    = id;
            this.url   = url;
            this.title = title;
            this.text  = text;
        }
    }

    private final int maxPages;                        // hard cap on pages to visit
    private final List<CrawledPage> pages;             // crawled results in order
    private final Set<String> visited;                 // URLs already seen

    public WebCrawler(int maxPages) {
        this.maxPages = maxPages;
        this.pages    = new ArrayList<>();
        this.visited  = new HashSet<>();
    }

    public void crawl(String seedUrl) {
        Queue<String> queue = new LinkedList<>();
        queue.add(seedUrl);

        System.out.println(" Starting Web Crawl ");
        System.out.println("Seed URL : " + seedUrl);
        System.out.println("Max pages: " + maxPages);

        while (!queue.isEmpty() && pages.size() < maxPages) {
            String url = queue.poll();

            // skip if already visited
            if (visited.contains(url)) continue;
            visited.add(url);

            try {
                // Fetch the page with a browser-like user-agent so Wikipedia doesn't block us
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (compatible; JavaCrawler/1.0)")
                        .timeout(8000)
                        .get();

                // Extract title
                String title = doc.title();
                if (title == null || title.isEmpty()) title = url;

                Element contentDiv = doc.selectFirst("#mw-content-text");
                String text;
                if (contentDiv != null) {
                    // Remove unwanted clutter: citation superscripts, nav boxes, tables
                    contentDiv.select("sup, .navbox, .infobox, .reflist, table, .mw-editsection").remove();
                    text = contentDiv.text();
                } else {
                    text = doc.body().text();
                }

                // Store the crawled page
                CrawledPage page = new CrawledPage(pages.size(), url, title, text);
                pages.add(page);

                System.out.println("[" + pages.size() + "/" + maxPages + "] Crawled: " + title);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String href = link.attr("abs:href");   // absolute URL

                    if (isValidWikipediaLink(href) && !visited.contains(href)) {
                        queue.add(href);
                    }
                }

            } catch (IOException e) {
                System.out.println(" Could not fetch: " + url + " (" + e.getMessage() + ")");
            }

            // Small polite delay so we don't hammer Wikipedia's servers
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }

        System.out.println("\n=== Crawl complete. Total pages indexed: " + pages.size() + " ===\n");
    }

    private boolean isValidWikipediaLink(String url) {
        if (url == null || url.isEmpty())                     return false;
        if (!url.startsWith("https://en.wikipedia.org/wiki/")) return false;

        // Exclude non-article namespaces
        String path = url.substring("https://en.wikipedia.org/wiki/".length());
        if (path.contains(":"))   return false;
        if (path.contains("#"))   return false;
        if (path.isEmpty())       return false;

        return true;
    }

    public List<CrawledPage> getPages() {
        return pages;
    }
}