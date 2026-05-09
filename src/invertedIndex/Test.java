/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package invertedIndex;

/*
 *
 * @author ehab
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

public class Test {

    public static void main(String[] args) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("===================================");
        System.out.println("  Information Retrieval System    ");
        System.out.println("===================================");
        System.out.println("1. Phrase Search (local documents)");
        System.out.println("2. Cosine Similarity Search (web crawling)");
        System.out.println("===================================");
        System.out.print("Enter your choice (1 or 2): ");

        String choice = in.readLine();

        if (choice == null || choice.trim().isEmpty()) {
            System.out.println("No choice entered. Exiting.");
            return;
        }

        switch (choice.trim()) {
            case "1":
                runAssignment1(in);
                break;
            case "2":
                runAssignment2(in);
                break;
            default:
                System.out.println("Invalid choice. Please enter 1 or 2.");
        }

        System.out.println("Goodbye!");
    }

    // Phrase Search on local documents
    static void runAssignment1(BufferedReader in) throws IOException {

        System.out.println("\n===================================");
        System.out.println("  Phrase Search     ");
        System.out.println("===================================");

        Index5 index = new Index5();

        String files = "tmp11/tmp11/rl/collection/";
        File file = new File(files);
        String[] fileList = file.list();

        // Sort files for consistent document IDs
        fileList = index.sort(fileList);
        index.N = fileList.length;

        // Build full paths
        for (int i = 0; i < fileList.length; i++) {
            File checkDir = new File(files + fileList[i]);
            if (checkDir.isDirectory()) continue;
            fileList[i] = files + fileList[i];
        }

        index.buildIndex(fileList);
        System.out.println("Index built from " + index.N + " documents.");
        index.printDictionary();

        // Interactive phrase search loop
        String phrase;
        do {
            System.out.println("\nEnter search phrase (or press Enter to exit): ");
            phrase = in.readLine();

            if (phrase == null || phrase.trim().isEmpty()) break;

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
    }

    // Cosine Similarity Search using Web Crawler
    static void runAssignment2(BufferedReader in) throws IOException {

        System.out.println("\n===================================");
        System.out.println("  Cosine Similarity ");
        System.out.println("===================================");

        // Step 1: Crawl
        System.out.println("\n Step 1: Crawling Wikipedia...");
        WebCrawler crawler = new WebCrawler(10);
        crawler.crawl("https://en.wikipedia.org/wiki/List_of_pharaohs");

        List<WebCrawler.CrawledPage> pages = crawler.getPages();

        if (pages.isEmpty()) {
            System.out.println("No pages crawled. Check your internet connection.");
            return;
        }

        // Step 2: Build index + IDF + TF-IDF vectors
        Index5 index = new Index5();
        index.buildIndexFromPages(pages);

        // Step 3: Compute document norms
        for (int docId : index.docVectors.keySet()) {
            double sumOfSquares = 0.0;
            for (double weight : index.docVectors.get(docId).values()) {
                sumOfSquares += weight * weight;
            }
            index.sources.get(docId).norm = Math.sqrt(sumOfSquares);
        }
        System.out.println("Document norms computed successfully.");

        // Print crawled documents list
        System.out.println("\n--- Crawled Documents ---");
        for (WebCrawler.CrawledPage p : pages) {
            System.out.printf("  [Doc %2d] %s%n", p.id, p.title);
        }

        // Step 4: Interactive query loop
        System.out.println("\n Step 4: Search Ready.");
        System.out.println("===================================");

        String queryString;
        do {
            System.out.println("\nEnter query string (or press Enter to exit): ");
            queryString = in.readLine();

            if (queryString == null || queryString.trim().isEmpty()) break;

            // Build query TF
            HashMap<String, Double> queryVector = new HashMap<>();
            String[] words = queryString.split("\\W+");

            HashMap<String, Integer> queryTF = new HashMap<>();
            for(String word: words) {
                word = word.toLowerCase();
                if(index.stopWord(word)) // skip stop words
                    continue;
                word = index.stemWord(word);
                queryTF.put(word, queryTF.getOrDefault(word, 0) + 1);
            }

            for(String word: queryTF.keySet()) {
                if(index.idf.containsKey(word)){ // if term exists in index get its weight
                    double weight = queryTF.get(word) * index.idf.get(word);
                    queryVector.put(word, weight);
                }
            }

            if(queryVector.isEmpty()){
                System.out.println("No query terms found in the index. Try again.");
                continue;
            }

            // a query can appear in all the docs so, the idf = log(N/df) = log(N/N) = 0 => tf-idf = 0
            boolean hasNonZeroWeight = false;
            for(double weight : queryVector.values()) {
                if(weight > 0) {
                    hasNonZeroWeight = true;
                    break;
                }
            }
            if(!hasNonZeroWeight){
                System.out.println(
                        "No meaningful query terms found in the index (appears in all documents -> idf = 0). Try again."
                );
                continue;
            }

            // Addition: we will also use the query vector's norm so cos = dot(query, doc) / (norm(doc) * norm(query))
            double sumOfSquares = 0.0;
            for (double weight : queryVector.values()) {
                sumOfSquares += weight * weight;
            }
            double queryNorm = Math.sqrt(sumOfSquares);

            System.out.println("Query norm computed successfully.");


            // Cosine similarity: score = dot(query, doc) / (norm(doc) * norm(query))
            HashMap<Integer, Double> scores = new HashMap<>();
            for(int docId: index.docVectors.keySet()) {
                HashMap<String, Double> docVector = index.docVectors.get(docId);
                double dotProduct = 0.0;

                // get dot product
                for(String term: docVector.keySet()) {
                    if(queryVector.containsKey(term))
                        dotProduct += queryVector.get(term) * docVector.get(term);
                }

                // calculate norm then the score and add it into scores
                double norm = index.sources.get(docId).norm;
                if(norm>0)
                    scores.put(docId, dotProduct/(norm * queryNorm));
                else scores.put(docId, 0.0);
            }

            // Rank top K=10
            List<Map.Entry<Integer,Double>> ranked = new ArrayList<>(scores.entrySet());
            ranked.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));

            int k = Math.min(10,ranked.size());
            System.out.println("===  Top " + k + " Results for: " + queryString + "  ===" );

            for(int i=0; i<k; i++) {
                int docId = ranked.get(i).getKey();
                double score = ranked.get(i).getValue();

                if(score == 0.0)
                    break;

                System.out.printf("%d. Score: %.4f%n", i + 1, score);
                System.out.println("Title   : " + index.sources.get(docId).title);
                System.out.println("URL     : " + index.sources.get(docId).URL);
            }

        } while (true);
    }
}