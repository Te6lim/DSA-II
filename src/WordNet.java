import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.DirectedCycle;

import java.util.ArrayList;
import java.util.Collections;

public class WordNet {

    private final ArrayList<Bag<String>> synsets;
    private final Digraph wordDigraph;

    private final SAP sap;

    private final ArrayList<String> nouns;

    // constructor takes the name of the two input files
    public WordNet(String synSetsFileName, String hypernymFileName) {
        validateInput(synSetsFileName != null, hypernymFileName != null);

        nouns = new ArrayList<>();

        synsets = getSynSetsFromFileInput(synSetsFileName);
        wordDigraph = getHypernymDigraph(hypernymFileName);

        if (!isRootDAG()) throw new IllegalArgumentException();

        sap = new SAP(wordDigraph);

        Collections.sort(nouns);
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

    private Digraph getHypernymDigraph(String fileName) {
        In hypernymFile = new In(fileName);

        String line;
        int indexOfExtraction;
        Bag<Integer> synsetReferences;

        Digraph digraph = new Digraph(synsets.size());

        int counter = 0;
        while (hypernymFile.hasNextLine()) {
            line = hypernymFile.readLine();
            indexOfExtraction = pointOfExtraction(line);
            if (indexOfExtraction != -1) {
                synsetReferences = getBagOfSynSetReferences(indexOfExtraction, line);
                if (synsetReferences != null) {
                    addReferencesToEdge(synsetReferences, digraph, counter);
                }
            }
            ++counter;
        }

        return digraph;
    }

    private void addReferencesToEdge(Bag<Integer> synsetReferences, Digraph digraph, int counter) {
        for (int sy : synsetReferences) {
            digraph.addEdge(counter, sy);
        }
    }

    private Bag<Integer> getBagOfSynSetReferences(int index, String line) {
        Bag<Integer> bagOfSynsets;
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

        if (bagOfSynsets.isEmpty()) return null; else return bagOfSynsets;
    }

    private boolean isRootDAG() {
        DirectedCycle cycle = new DirectedCycle(wordDigraph);
        return !cycle.hasCycle();
    }

    private void validateInput(boolean b, boolean b2) {
         if (!b || !b2) throw new IllegalArgumentException();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns;
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException();
        return !(Collections.binarySearch(nouns, word) < 0);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        validateInput(isNoun(nounA), isNoun(nounB));
        if (nounA.equals(nounB)) return 0;

        ArrayList<Integer> positionsOfA = getNounPositions(nounA), positionsOfB = getNounPositions(nounB);

        return sap.length(positionsOfA, positionsOfB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validateInput(isNoun(nounA), isNoun(nounB));

        ArrayList<Integer> positionsOfA = getNounPositions(nounA), positionsOfB = getNounPositions(nounB);

        if (nounA.equals(nounB)) return getShortestAncestorString(positionsOfA.get(0));

        int ancestor = sap.ancestor(positionsOfA, positionsOfB);

        if (ancestor != -1) return getShortestAncestorString(ancestor); else return null;
    }

    private String getShortestAncestorString(int ancestor) {
        StringBuilder string = new StringBuilder();
        for (String s : synsets.get(ancestor)) {
            string.append(s);
            string.append(" ");
        }
        string.deleteCharAt(string.length() - 1);
        return string.toString();
    }

    private ArrayList<Integer> getNounPositions(String noun) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < synsets.size(); ++i) {
            for (String s : synsets.get(i)) if (s.equals(noun)) positions.add(i);
        }
        return positions;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet(
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src\\" +
                        "\\synsets.txt",
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src" +
                        "\\hypernyms.txt"
        );

        String nounA = "group_action", nounB = "action";

        StdOut.println(wn.sap(nounA, nounB));
        StdOut.println(wn.distance(nounA, nounB));
    }
}