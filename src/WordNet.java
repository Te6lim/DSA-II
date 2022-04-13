import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.Collections;

public class WordNet {

    private final ArrayList<Bag<String>> synsets;
    private final ArrayList<Bag<Integer>> hypernyms;

    private final ArrayList<String> nouns;

    private Integer rootPosition = null;

    // constructor takes the name of the two input files
    public WordNet(String synSetsFileName, String hypernymFileName) {
        validateInput(synSetsFileName == null, hypernymFileName == null);

        nouns = new ArrayList<>();

        synsets = getSynSetsFromFileInput(synSetsFileName);
        hypernyms = getHypernymsFromInputFile(hypernymFileName);

        Collections.sort(nouns);

        if (!isRootDAG()) throw new IllegalArgumentException();
    }

    private ArrayList<Bag<String>> getSynSetsFromFileInput(String fileName) {
        In synSetFile = new In(fileName);
        ArrayList<Bag<String>> synSets = new ArrayList<>();

        while (synSetFile.hasNextLine()) addWordsToSynSetsFromFile(synSetFile, synSets);

        return synSets;
    }

    private void addWordsToSynSetsFromFile(In synSetFile, ArrayList<Bag<String>> synSets) {
        int indexOfExtraction;
        Bag<String> words;
        String line;
        line = synSetFile.readLine();
        indexOfExtraction = pointOfExtraction(line);
        if (indexOfExtraction != -1) {
            words = getBagOfSynonyms(indexOfExtraction, line);
            synSets.add(words);
        }
    }

    private int pointOfExtraction(String line) {
        int counter = 0;
        char c = line.charAt(counter);
        while (c != ',') {
            if (++counter == line.length()) {
                return -1;
            }
            c = line.charAt(counter);
        }
        return counter + 1;
    }

    private Bag<String> getBagOfSynonyms(int index, String line) {
        char c = line.charAt(index);
        StringBuilder word = new StringBuilder();
        String wordToString;
        Bag<String> wordBag = new Bag<>();
        while (c != ',') {
            if (c != ' ') word.append(c);
            else {
                wordToString = word.toString();
                wordBag.add(wordToString);
                nouns.add(wordToString);
                word = new StringBuilder();
            }
            if (++index == line.length()) break;
            c = line.charAt(index);
        }
        if (word.length() != 0) {
            wordToString = word.toString();
            wordBag.add(word.toString());
            nouns.add(wordToString);
        }
        return wordBag;
    }

    private ArrayList<Bag<Integer>> getHypernymsFromInputFile(String fileName) {
        In hypernymFile = new In(fileName);
        ArrayList<Bag<Integer>> hypernyms = new ArrayList<>();

        String line;
        int indexOfExtraction;
        Bag<Integer> synsetReferences;

        while (hypernymFile.hasNextLine()) {
            line = hypernymFile.readLine();
            indexOfExtraction = pointOfExtraction(line);
            synsetReferences = getBagOfSynSetReferences(indexOfExtraction, line);
            hypernyms.add(synsetReferences);
            if (indexOfExtraction == -1) rootPosition = hypernyms.size() - 1;
        }

        return hypernyms;
    }

    private Bag<Integer> getBagOfSynSetReferences(int index, String line) {
        Bag<Integer> bagOfSynsets = null;
        if (index != -1) {
            char c = line.charAt(index);
            bagOfSynsets = new Bag<>();

            StringBuilder referenceToSynSet = new StringBuilder();
            while (true) {
                if (c != ',') referenceToSynSet.append(c);
                else {
                    bagOfSynsets.add(Integer.parseInt(referenceToSynSet.toString()));
                    referenceToSynSet = new StringBuilder();
                }
                if (++index == line.length()) break;
                c = line.charAt(index);
            }
            if (referenceToSynSet.length() != 0) bagOfSynsets.add(Integer.parseInt(referenceToSynSet.toString()));
        }

        return bagOfSynsets;
    }

    private boolean isRootDAG() {
        return rootPosition != null;
    }

    private void validateInput(boolean b, boolean b2) {
        if (b || b2) throw new IllegalArgumentException();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() { return nouns; }

    // is the word a WordNet noun?
    public boolean isNoun(String word) { return false; }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) { return 0; }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) { return null; }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet(
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src\\" +
                        "\\synsets.txt",
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src" +
                        "\\hypernyms.txt"
        );
    }

}
