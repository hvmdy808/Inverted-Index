package invertedIndex;

import java.util.*;
public class FullTest {

    static int passed = 0;
    static int failed = 0;

    // Check a numeric value
    static void check(String testName, double expected, double actual) {
        double diff = Math.abs(expected - actual);
        if (diff < 0.0001) {
            System.out.printf("  PASS  %-45s expected=%.6f  got=%.6f%n",
                    testName, expected, actual);
            passed++;
        } else {
            System.out.printf("  FAIL  %-45s expected=%.6f  got=%.6f  diff=%.6f%n",
                    testName, expected, actual, diff);
            failed++;
        }
    }

    // Check a boolean condition
    static void checkBool(String testName, boolean condition, String detail) {
        if (condition) {
            System.out.printf("  PASS  %-45s (%s)%n", testName, detail);
            passed++;
        } else {
            System.out.printf("  FAIL  %-45s (%s)%n", testName, detail);
            failed++;
        }
    }

    public static void main(String[] args) {

        System.out.println("=======================================================");
        System.out.println("   TF-IDF Correctness Test  (doc1.txt + doc2.txt)      ");
        System.out.println("=======================================================");
        System.out.println("  doc0 = 'Cairo University Zayed City is a new CU branch'");
        System.out.println("  doc1 = 'Zayed attending in AinShams University in Cairo'");
        System.out.println();

        // ====================================================
        // SETUP
        // ====================================================
        Index5 index = new Index5();
        String[] files = { "C:\\Users\\LIFEBOOK T936\\Downloads\\IR-A1\\tmp11\\tmp11\\rl\\collection\\doc1.txt", "C:\\Users\\LIFEBOOK T936\\Downloads\\IR-A1\\tmp11\\tmp11\\rl\\collection\\doc2.txt" };
        index.N = 2;
        index.buildIndex(files);
        index.computeIDF();
        index.buildTFIDFVectors();

        // Compute norms (same as Test.java)
        for (int docId : index.docVectors.keySet()) {
            double sumOfSquares = 0.0;
            for (double weight : index.docVectors.get(docId).values()) {
                sumOfSquares += weight * weight;
            }
            index.sources.get(docId).norm = Math.sqrt(sumOfSquares);
        }

        // ====================================================
        // TEST 1 - IDF values
        // Words in BOTH docs  : IDF = log10(2/2) = 0.0000
        // Words in ONE doc    : IDF = log10(2/1) = 0.3010
        // ====================================================
        System.out.println("--- TEST 1: IDF Values ---");

        check("IDF(cairo)      both docs = 0.0",    0.0000, index.idf.getOrDefault("cairo",      -1.0));
        check("IDF(university) both docs = 0.0",    0.0000, index.idf.getOrDefault("university", -1.0));
        check("IDF(zayed)      both docs = 0.0",    0.0000, index.idf.getOrDefault("zayed",      -1.0));
        check("IDF(city)       one doc   = 0.3010", 0.3010, index.idf.getOrDefault("city",       -1.0));
        check("IDF(branch)     one doc   = 0.3010", 0.3010, index.idf.getOrDefault("branch",     -1.0));
        check("IDF(cu)         one doc   = 0.3010", 0.3010, index.idf.getOrDefault("cu",         -1.0));
        check("IDF(attending)  one doc   = 0.3010", 0.3010, index.idf.getOrDefault("attending",  -1.0));
        check("IDF(ainshams)   one doc   = 0.3010", 0.3010, index.idf.getOrDefault("ainshams",   -1.0));

        // ====================================================
        // TEST 2 - Document vectors
        // IDF=0 words excluded (TF * 0 = 0)
        // doc0 unique: city, new, cu, branch  each = 0.301
        // doc1 unique: attending, ainshams     each = 0.301
        // ====================================================
        System.out.println("\n--- TEST 2: Document Vectors ---");

        HashMap<String, Double> vec0 = index.docVectors.get(0);
        HashMap<String, Double> vec1 = index.docVectors.get(1);

        check("doc0[city]",      0.3010, vec0.getOrDefault("city",      0.0));
        check("doc0[branch]",    0.3010, vec0.getOrDefault("branch",    0.0));
        check("doc0[new]",       0.3010, vec0.getOrDefault("new",       0.0));
        check("doc0[cu]",        0.3010, vec0.getOrDefault("cu",        0.0));
        check("doc1[attending]", 0.3010, vec1.getOrDefault("attending", 0.0));
        check("doc1[ainshams]",  0.3010, vec1.getOrDefault("ainshams",  0.0));

        // cairo/university/zayed excluded because IDF=0
        check("doc0[cairo] excluded = 0",      0.0, vec0.getOrDefault("cairo",      0.0));
        check("doc0[university] excluded = 0", 0.0, vec0.getOrDefault("university", 0.0));
        check("doc1[cairo] excluded = 0",      0.0, vec1.getOrDefault("cairo",      0.0));

        // ====================================================
        // TEST 3 - Document Norms
        // doc0: 5 terms each 0.301 -> norm = sqrt(5 * 0.301^2) = 0.6731
        // doc1: 2 terms each 0.301 -> norm = sqrt(2 * 0.301^2) = 0.4257
        // ====================================================
        System.out.println("\n--- TEST 3: Document Norms ---");

        check("norm(doc0) = 0.6731", 0.6731, index.sources.get(0).norm);
        check("norm(doc1) = 0.4257", 0.4257, index.sources.get(1).norm);

        // ====================================================
        // TEST 4 - Query "cairo branch"
        // cairo  IDF=0 -> excluded
        // branch IDF=0.301 -> queryVec[branch]=0.301
        // dot(query,doc0) = 0.301*0.301 = 0.0906
        // dot(query,doc1) = 0
        // score(doc0) = 0.0906 / 0.6731 = 0.1346
        // score(doc1) = 0.000
        // ====================================================
        System.out.println("\n--- TEST 4: Query 'cairo branch' ---");

        HashMap<Integer, Double> s1 = runQuery(index, "cairo branch");
        check("score doc0 = 0.1346", 0.1346, s1.getOrDefault(0, 0.0));
        check("score doc1 = 0.0000", 0.0000, s1.getOrDefault(1, 0.0));
        checkBool("doc0 ranks #1",
                s1.getOrDefault(0, 0.0) > s1.getOrDefault(1, 0.0),
                "doc0=" + String.format("%.4f", s1.getOrDefault(0, 0.0))
                        + "  doc1=" + String.format("%.4f", s1.getOrDefault(1, 0.0)));

        // ====================================================
        // TEST 5 - Query "ainshams attending"
        // ainshams  IDF=0.301 -> weight=0.301
        // attending IDF=0.301 -> weight=0.301
        // dot(query,doc0) = 0
        // dot(query,doc1) = 0.301*0.301 + 0.301*0.301 = 0.1812
        // score(doc0) = 0.000
        // score(doc1) = 0.1812 / 0.4257 = 0.4257
        // ====================================================
        System.out.println("\n--- TEST 5: Query 'ainshams attending' ---");

        HashMap<Integer, Double> s2 = runQuery(index, "ainshams attending");
        check("score doc0 = 0.0000", 0.0000, s2.getOrDefault(0, 0.0));
        check("score doc1 = 0.4257", 0.4257, s2.getOrDefault(1, 0.0));
        checkBool("doc1 ranks #1",
                s2.getOrDefault(1, 0.0) > s2.getOrDefault(0, 0.0),
                "doc1=" + String.format("%.4f", s2.getOrDefault(1, 0.0))
                        + "  doc0=" + String.format("%.4f", s2.getOrDefault(0, 0.0)));

        // ====================================================
        // TEST 6 - Query "zayed"  (in BOTH docs, IDF=0)
        // Query vector EMPTY -> all scores = 0
        // ====================================================
        System.out.println("\n--- TEST 6: Query 'zayed' (IDF=0, no results expected) ---");

        HashMap<Integer, Double> s3 = runQuery(index, "zayed");
        check("score doc0 = 0.0000", 0.0000, s3.getOrDefault(0, 0.0));
        check("score doc1 = 0.0000", 0.0000, s3.getOrDefault(1, 0.0));

        // ====================================================
        // TEST 7 - Query "city cu branch"  (all unique to doc0)
        // each weight=0.301
        // dot(query,doc0) = 3 * 0.301*0.301 = 0.2718
        // dot(query,doc1) = 0
        // score(doc0) = 0.2718 / 0.6731 = 0.4039
        // score(doc1) = 0.000
        // ====================================================
        System.out.println("\n--- TEST 7: Query 'city cu branch' (all in doc0 only) ---");

        HashMap<Integer, Double> s4 = runQuery(index, "city cu branch");
        check("score doc0 = 0.4039", 0.4039, s4.getOrDefault(0, 0.0));
        check("score doc1 = 0.0000", 0.0000, s4.getOrDefault(1, 0.0));
        checkBool("doc0 ranks #1",
                s4.getOrDefault(0, 0.0) > s4.getOrDefault(1, 0.0),
                "doc0=" + String.format("%.4f", s4.getOrDefault(0, 0.0)));

        // ====================================================
        // FINAL REPORT
        // ====================================================
        System.out.println();
        System.out.println("=======================================================");
        System.out.printf( "  Results:  %d passed,  %d failed%n", passed, failed);
        if (failed == 0) {
            System.out.println("  ALL TESTS PASSED - implementation is correct!");
        } else {
            System.out.println("  SOME TESTS FAILED - check your implementation.");
        }
        System.out.println("=======================================================");
    }

    // -------------------------------------------------------
    // Runs a query and returns cosine similarity scores
    // Same logic as Test.java
    // -------------------------------------------------------
    static HashMap<Integer, Double> runQuery(Index5 index, String queryString) {

        HashMap<String, Integer> queryTF = new HashMap<>();
        for (String word : queryString.split("\\W+")) {
            word = word.toLowerCase();
            if (index.stopWord(word)) continue;
            word = index.stemWord(word);
            queryTF.put(word, queryTF.getOrDefault(word, 0) + 1);
        }

        HashMap<String, Double> queryVector = new HashMap<>();
        for (String word : queryTF.keySet()) {
            if (index.idf.containsKey(word)) {
                double weight = queryTF.get(word) * index.idf.get(word);
                if (weight > 0) queryVector.put(word, weight);
            }
        }

        HashMap<Integer, Double> scores = new HashMap<>();
        for (int docId : index.docVectors.keySet()) {
            double dot = 0.0;
            for (String term : index.docVectors.get(docId).keySet()) {
                if (queryVector.containsKey(term)) {
                    dot += queryVector.get(term) * index.docVectors.get(docId).get(term);
                }
            }
            double norm = index.sources.get(docId).norm;
            scores.put(docId, norm > 0 ? dot / norm : 0.0);
        }
        return scores;
    }}