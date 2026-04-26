/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index

     //--------------------------------------------
     // Constructor function that creates an empty index
     // It initializes: sources & index
    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();   // create empty "documents table"
        index = new HashMap<String, DictEntry>();         // create empty "words table"
    }

    public void setN(int n) {
        N = n;
    }  // store the total number of documents in the collection


    //---------------------------------------------
    // It prints the posting list of one word as a neat bracketed list, in format [0,1,3]
    // This shows which document IDs contain that word
    // It walks the linked list node by node
    // Prints the docId of each node
   // Adds a comma after every docId except the last one
    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");      // print open bracket
        while (p != null) {         // walk through every node in the linked list

            /// -4- **** complete here ****
            // fix get rid of the last comma
            if (p.next == null) {
                System.out.print("" + p.docId);     // last item so NO comma after it
            }
            else {System.out.print("" + p.docId + "," );}  // not last so add comma
            p = p.next;         // move to next node
        }
        System.out.println("]");  // print close bracket
    }

     //---------------------------------------------
     //  Prints the complete inverted index to the screen
     //  Displays each indexed word, its document frequency and the list of document IDs where it appears
     //  Iterates through all entries in the index HashMap and uses printPostingList() to print the posting lists
     //  It prints the total number of unique indexed words
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();      // get all words in the index
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();           // get one word at a time
            DictEntry dd = (DictEntry) pair.getValue();       // get that word's value
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);     // print the list of documents
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());  // total unique words
    }
 
    //-----------------------------------------------
    // Reads all files in the list and builds the sources and index tables
    // Each file is assigned an ID, read line by line, and processed using indexOneLine()
    // After processing the total word count is stored
    // If a file cannot be opened it is skipped with a warning message
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 0;   // document ID counter, it starts at 0
        for (String fileName : files) {     // loop over every file path given
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {

                if (!sources.containsKey(fileName)) {       // stores this file information in the sources table
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int currentPosition = 1;        // word counter, it starts at 1 for each document
                
                while ((ln = file.readLine()) != null) {    // read file line by line
                   
                    currentPosition = indexOneLine(ln, fid, currentPosition); 
                }
                sources.get(fid).length = currentPosition - 1;  // save total word count of doc

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");     // skip missing files
            }
            fid++;  // next document gets the next ID number
        }
    }

    //----------------------------------------------------------------------------
    // Processes one line of text and updates the inverted index
    // Splits the line into words, removes punctuation, converts to lowercase, skips stop words, and updates each word’s posting list with its position
    // If the word is new, a new DictEntry is created
    // If it already exists, its posting list, document frequency, and term frequency are updated
    // Returns the next word position after processing the line
    public int indexOneLine(String ln, int fid, int currentPosition) {
        String[] words = ln.split("\\W+");      // split line into words
        for (String word : words) {
            word = word.toLowerCase();      // make lowercase
            
            if (stopWord(word)) {       // skip useless words
                currentPosition++;      // still count the position
                continue;
            }
            word = stemWord(word);      // reduce to root form
            
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());   // first time seeing this word add to index
            }
            
            if (!index.get(word).postingListContains(fid)) {
                // First time this word appears in this document
                index.get(word).doc_freq += 1;      // one more document contains this word

                // Create a new posting
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid, currentPosition);  // first posting
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid, currentPosition);  // append
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.addPosition(currentPosition);  // Word already seen in this document — just add the new position
            }
            
            index.get(word).term_freq += 1;     // total occurrences across ALL documents
            currentPosition++;           // move to next word position
        }
        return currentPosition;         // return updated position counter for next line
    }

    //----------------------------------------------------------------------------
    // It checks if a word should be ignored during indexing
    // Common words like "the", "a", and short words with less than 2 characters are skipped to keep the index smaller and more useful
    // It returns true if the word should be skipped, otherwise false.
    boolean stopWord(String word) {

        // skip common words add no search value
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {    // skip single characters like I or empty strings
            return true;
        }
        return false;

    }

    //----------------------------------------------------------------------------
    String stemWord(String word) { //skip for now
        return word;
        //        Stemmer s = new Stemmer();
        //        s.addString(word);
        //        s.stem();
        //        return s.toString();
    }


    //----------------------------------------------------------------------------
    // Finds documents where two words appear consecutively
    // Returns a new posting list containing only valid phrase matches
    // It matches documents using a merge approach on both posting lists
    // If both words appear in the same document it compares their positions
    // Check if any position of word2 is exactly word1 position + 1
    // If a match is found, store it and build a new posting node
    // The result carries positions of word2 for further chaining in multi-word phrases
    // It returns a posting list of documents where the two words appear consecutively or null if no matches are found.
    Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null;      // result list initially empty
        Posting last = null;        // tail pointer for fast appending

        while (pL1 != null && pL2 != null) {     // walk both posting lists at the same time
            if (pL1.docId == pL2.docId) {       // if same document check positions
                
                java.util.ArrayList<Integer> pos1 = pL1.positions;
                java.util.ArrayList<Integer> pos2 = pL2.positions;
                java.util.ArrayList<Integer> matchPositions = new java.util.ArrayList<>();

                int i = 0, j = 0;
                while (i < pos1.size() && j < pos2.size()) {
                    if (pos2.get(j) == pos1.get(i) + 1) {
                        // word2 appears immediately after word1
                        matchPositions.add(pos2.get(j)); 
                        i++;
                        j++;
                    } else if (pos2.get(j) > pos1.get(i) + 1) {
                        i++;     // move first word position forward
                    } else {
                        j++;    // move second word position forward
                    }
                }

                
                if (!matchPositions.isEmpty()) {
                    // keep this document if phrase match exists
                    Posting newPosting = new Posting(pL1.docId, matchPositions.get(0));
                    newPosting.positions = matchPositions; 
                    newPosting.dtf = matchPositions.size();

                    if (answer == null) {
                        answer = newPosting;
                        last = newPosting;
                    } else {
                        last.next = newPosting;
                        last = newPosting;
                    }
                }
        
                pL1 = pL1.next;     // move to next document in both lists
                pL2 = pL2.next;
                
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;     // skip smaller docId in first list
            } else {
                pL2 = pL2.next;     // skip smaller docId in second list
            }
        }
        return answer;    // return matching documents
    }

    //---------------------------------------------------------------
    // Main search method used to find documents containing an exact phrase in the same order
    // Splits the phrase into words and removes stop words
    // Gets the posting list of the first word
    // Applies intersect() repeatedly with the next words
    // Each intersection narrows results to exact phrase matches
    // If a word is missing it returns no results safely
    // It returns a formatted list of matching documents or empty result if none found
    public String find_24_01(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

       // handle empty input or spaces
        if (words.length == 0 || words[0].isEmpty()) {
            return "";
        }
        // check if first word not exists in index
        if (!index.containsKey(words[0].toLowerCase())) {
            return "";
        }
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        while (i < len) {

            // check if each next word not exists in index
            if (!index.containsKey(words[i].toLowerCase())) {
                return "";
            }
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            // stop early if no matches left, the intersection is empty
            if (posting == null) {
                return "";
            }
            i++;
        }
        while (posting != null) {
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title
                    + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }


    
    
    //--------------------------------------------
    // Sorts file names alphabetically to ensure consistent document IDs every time the program runs
    // It uses bubble sort by comparing neighboring elements and swapping them until the array is fully sorted
    // It returns the same array after sorting in ascending alphabetical order
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {   // keep repeating until no swaps needed
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {      // if out of order
                    sTmp = words[i];        // swap them
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //--------------------------------------------
     // Saves the entire inverted index from memory into a text file on disk
    // It stores all source records
    // Then it writes section2 as a separator before saving the dictionary entries and posting lists
    // Each term is stored with its document frequency, total term frequency, and all postings (docId and dtf)
    // It writes "end" to mark the end of the file and closes the writer
    public void store(String storageName) {
        try {
            String pathToStorage = "tmp11/tmp11/rl/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------
    // Checks whether the storage file already exists in the specified directory
    // It creates a File object using the given storage name and verifies that the file exists and it is not a directory
    // Returns true if the file is found and valid, otherwise returns false
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }

    //----------------------------------------------------
    // Creates a new empty storage file for the inverted index
    // It opens a new file using the given storage name and writes only "end" inside it
    // This creates a valid empty index structure that can be used later for saving data
    // It is mainly used when no previous storage file exists
    public void createStore(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //----------------------------------------------------
    // Loads the inverted index from the storage file back into memory.
    // It reads all source records until it reaches "section2", then reconstructs SourceRecord objects.
    // After that it reads all dictionary entries and posting lists until it reaches "end".
    // For each term it restores document frequency, total term frequency, and rebuilds the linked posting list.
    // It returns the reconstructed HashMap containing the full inverted index.
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/"+storageName;         
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

//=====================================================================
