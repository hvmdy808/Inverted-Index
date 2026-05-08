/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

/**
 *
 * @author ehab
 */
/*
public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();

        String files = "tmp11/tmp11/rl/collection/";

        File file = new File(files);
        String[] fileList = file.list();

        if (fileList == null) {
            System.out.println("Directory does not exist or is empty! Please check the path.");
            return;
        }

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {

            File checkDir = new File(files + fileList[i]);
            if (checkDir.isDirectory())
                continue;

            fileList[i] = files + fileList[i];
        }

        index.buildIndex(fileList);

        index.printDictionary();

        String test3 = "data should plain";
        System.out.println("\nTest Phrase result: \n" + index.find_24_01(test3));

        String phrase = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.println("\nPrint search phrase (or press Enter to exit): ");
            phrase = in.readLine();

            if (phrase == null || phrase.trim().isEmpty()) {
                break;
            }

            /// -3- **** complete here ****
            try {

                String result = index.find_24_01(phrase);

                if (result != null && !result.isEmpty()) {
                    System.out.println("Found in:\n" + result);
                } else {
                    System.out.println("No matching documents found for this exact phrase.");
                }
            } catch (Exception e) {
                System.out.println("One or more words in your phrase are not found in the dictionary!");
            }

        } while (true);

        System.out.println("Goodbye!");
    }
}

 */


public class Test {
    public static void main(String[] args) {

        WebCrawler crawler =
                new WebCrawler(10);

        crawler.crawl(
                "https://en.wikipedia.org/wiki/List_of_pharaohs"
        );

        List<WebCrawler.CrawledPage> pages =
                crawler.getPages();

        Index5 index = new Index5();

        // build index
        index.buildIndexFromPages(pages);
        for (Integer docId : index.docVectors.keySet()) {

            System.out.println("\nDOC ID: " + docId);

            HashMap<String, Double> vec =
                    index.docVectors.get(docId);

            for (String term : vec.keySet()) {

                System.out.println(
                        term + " → " + vec.get(term)
                );
            }
        }
    }


}