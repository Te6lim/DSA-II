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

        synsets = getSynSetsFromFileInput(synSetsFileName);
        wordDigraph = getHypernymDigraph(hypernymFileName);

        if (!isRootDAG()) throw new IllegalArgumentException();

        nouns = getNouns();

        getNouns();

        sap = new SAP(wordDigraph);
    }

    private ArrayList<String> getNouns() {
        ArrayList<String> listOfNouns = new ArrayList<>();
        for (Bag<String> b : synsets) {
            for (String s : b) {
                if (Collections.binarySearch(listOfNouns, s) < 0) {
                    listOfNouns.add(s);
                    Collections.sort(listOfNouns);
                }
            }
        }
        return listOfNouns;
    }

    private ArrayList<Bag<String>> getSynSetsFromFileInput(String fileName) {
        In synSetFile = new In(fileName);
        ArrayList<Bag<String>> synSets = new ArrayList<>();

        while (synSetFile.hasNextChar()) {
            addWordsToSynSetsFromFile(synSetFile, synSets);
        }

        return synSets;
    }

    private void addWordsToSynSetsFromFile(In file, ArrayList<Bag<String>> synSets) {
        Bag<String> words;
        char last = pointOfExtraction(file);
        if (last != '\u0000') {
            words = getBagOfSynonyms(file);
            synSets.add(words);
        }
    }

    private char pointOfExtraction(In file) {
        char character = '\u0000';
        while (character != '\r') {
            character = file.readChar();
            if (character == ',') return character;
        }
        return character;
    }

    private Bag<String> getBagOfSynonyms(In file) {
        StringBuilder word = new StringBuilder();
        String wordString;
        Bag<String> wordBag = new Bag<>();

        char c = file.readChar();
        while (c != ',') {
            if (c != ' ') word.append(c);
            else {
                wordString = word.toString();
                wordBag.add(wordString);
                word = new StringBuilder();
            }
            c = file.readChar();
        }
        if (word.length() != 0) {
            wordString = word.toString();
            wordBag.add(wordString);
        }
        while (c != '\r') {
            try {
                c = file.readChar();
            } catch (Exception e) {
                break;
            }
        }
        return wordBag;
    }

    private Digraph getHypernymDigraph(String fileName) {
        In hypernymFile = new In(fileName);
        char c;
        Bag<Integer> synsetReferences;

        Digraph digraph = new Digraph(synsets.size());

        int counter = 0;
        while (hypernymFile.hasNextChar()) {
            c = pointOfExtraction(hypernymFile);
            if (c != '\u0000') {
                synsetReferences = getBagOfSynSetReferences(hypernymFile);
                if (synsetReferences != null) {
                    addReferencesToEdge(synsetReferences, digraph, counter);
                }
            }
            ++counter;
        }

        return digraph;
    }

    private Bag<Integer> getBagOfSynSetReferences(In file) {
        Bag<Integer> bagOfSynsets;
        bagOfSynsets = new Bag<>();

        StringBuilder referenceToSynSet = new StringBuilder();

        char c = file.readChar();
        while (c != '\r' && c != '\n') {
            if (c != ',' && c != '\u0000')
                referenceToSynSet.append(c);
            else {
                if (referenceToSynSet.length() != 0)
                    bagOfSynsets.add(Integer.parseInt(referenceToSynSet.toString()));
                referenceToSynSet = new StringBuilder();
            }
            try {
                c = file.readChar();
            } catch (Exception e) {
                break;
            }
        }
        if (referenceToSynSet.length() != 0) bagOfSynsets.add(Integer.parseInt(referenceToSynSet.toString()));

        if (bagOfSynsets.isEmpty())
            return null;
        else return bagOfSynsets;
    }

    private void addReferencesToEdge(Bag<Integer> synsetReferences, Digraph digraph, int counter) {
        for (int sy : synsetReferences) {
            digraph.addEdge(counter, sy);
        }
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