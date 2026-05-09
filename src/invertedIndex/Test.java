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
    public static void main(String[] args) throws IOException{

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
//        for (Integer docId : index.docVectors.keySet()) {
//
//            System.out.println("\nDOC ID: " + docId);
//
//            HashMap<String, Double> vec =
//                    index.docVectors.get(docId);
//
//            for (String term : vec.keySet()) {
//
//                System.out.println(
//                        term + " → " + vec.get(term)
//                );
//            }
//        }

        // compute document norms -> to normalize cosine similarity score
        for (int docId: index.docVectors.keySet()) {
            double sumOfSquares = 0.0;
            for(double weight: index.docVectors.get(docId).values()) {
                sumOfSquares += weight * weight;
            }
            index.sources.get(docId).norm = Math.sqrt(sumOfSquares);
        }
        System.out.println("Document norms computed successfully");

        /////////////////////////////////////////
        // Query processing
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String queryString;
        System.out.println("\n===================================");
        System.out.println("Search Ready. ");
        do{
            System.out.println("Enter query string (blank to exit): ");
            queryString = in.readLine();
            if(queryString==null || queryString.trim().isEmpty())
                break;

            //////////////////////////
            // build query vector
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

            /// //////////////////////////////////
            /// //////////////////////////////////
            // cosine similarity calculations

            HashMap<Integer, Double> scores = new HashMap<>();


            // Step 1: Compute Query Norm
            double queryNorm = 0.0;

            // sum(weight²)
            for(double weight : queryVector.values()) {

                queryNorm += weight * weight;
            }

            // sqrt(sum(weight²))
            queryNorm = Math.sqrt(queryNorm);


            // Step 2: Compare Query with Docs
            for(int docId : index.docVectors.keySet()) {

                // get current document vector
                HashMap<String, Double> docVector =
                        index.docVectors.get(docId);

                double dotProduct = 0.0;

                // Step 3: Compute Dot Product
                // loop over all terms in the document
                for(String term : docVector.keySet()) {

                    // if the same term exists in query
                    if(queryVector.containsKey(term)) {

                        // dotProduct += queryWeight * docWeight
                        dotProduct +=
                                queryVector.get(term)
                                        *
                                        docVector.get(term);
                    }
                }

                // Step 4: Get Document Norm
                double docNorm =
                        index.sources.get(docId).norm;

                // Step 5: Compute Cosine Similarity
                double similarity = 0.0;

                // avoid division by zero
                if(docNorm > 0 && queryNorm > 0) {

                    similarity =
                            dotProduct /
                                    (docNorm * queryNorm);
                }

                // store final score
                scores.put(docId, similarity);
            }

            // rannk top K = 10 and print result

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

        }while(true);
    }


}