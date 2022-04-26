import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.DirectedCycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WordNet {

    private final ArrayList<String[]> synsets;

    private final SAP sap;

    private final ArrayList<String> nouns;

    private final HashMap<String, ArrayList<Integer>> nounPositions;

    // constructor takes the name of the two input files
    public WordNet(String synSetsFileName, String hypernymFileName) {
        validateInput(synSetsFileName != null, hypernymFileName != null);

        nounPositions = new HashMap<>();

        synsets = getSynSetsFromFileInput(synSetsFileName);
        Digraph wordDigraph = getHypernymDigraph(hypernymFileName, synsets);

        if (!isRootDAG(wordDigraph)) throw new IllegalArgumentException();

        sap = new SAP(wordDigraph);

        nouns = getNouns(synsets);
    }

    private ArrayList<String[]> getSynSetsFromFileInput(String fileName) {
        In synSetFile = new In(fileName);
        ArrayList<String[]> synSets = new ArrayList<>();

        while (synSetFile.hasNextLine()) addWordsToSynSetsFromFile(synSetFile, synSets);

        return synSets;
    }

    private void addWordsToSynSetsFromFile(In synSetFile, ArrayList<String[]> synSets) {
        String line = synSetFile.readLine();

        String[] x = line.split(",", 2);
        String[] y = x[1].split(",", 2);
        String[] z = y[0].split(" ");

        synSets.add(z);
    }

    private Digraph getHypernymDigraph(String fileName, ArrayList<String[]> parameterSynsets) {
        In hypernymFile = new In(fileName);

        String line;
        int[] synsetReferences;

        Digraph digraph = new Digraph(parameterSynsets.size());

        int counter = 0;
        while (hypernymFile.hasNextLine()) {
            line = hypernymFile.readLine();
            synsetReferences = getBagOfSynSetReferences(line);
            if (synsetReferences != null) {
                addReferencesToEdge(synsetReferences, digraph, counter);
            }
            ++counter;
        }

        return digraph;
    }

    private void addReferencesToEdge(int[] synsetReferences, Digraph digraph, int counter) {
        for (int sy : synsetReferences) {
            digraph.addEdge(counter, sy);
        }
    }

    private int[] getBagOfSynSetReferences(String line) {
        int[] arrayOfSynsets = null;

        String[] x = line.split(",");

        if (x.length > 1) {
            arrayOfSynsets = new int[x.length - 1];
            for (int i = 1; i < x.length; ++i) {
                arrayOfSynsets[i - 1] = Integer.parseInt(x[i]);
            }
        }

        return arrayOfSynsets;
    }

    private boolean isRootDAG(Digraph digraph) {
        DirectedCycle cycle = new DirectedCycle(digraph);
        return !cycle.hasCycle();
    }

    private void validateInput(boolean b, boolean b2) {
         if (!b || !b2) throw new IllegalArgumentException();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns;
    }

    private ArrayList<String> getNouns(ArrayList<String[]> parameterSynsets) {
        ArrayList<String> localNouns = new ArrayList<>();
        ArrayList<Integer> positions;

        for (int i = 0; i < parameterSynsets.size(); ++i) {
            for (String s : parameterSynsets.get(i)) {
                if (nounPositions.get(s) == null) {
                    localNouns.add(s);
                    positions = new ArrayList<>();
                    positions.add(i);
                    nounPositions.put(s, positions);
                } else nounPositions.get(s).add(i);
            }
        }

        Collections.sort(localNouns);
        return localNouns;
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

        ArrayList<Integer> positionsOfA = nounPositions.get(nounA), positionsOfB = nounPositions.get(nounB);

        return sap.length(positionsOfA, positionsOfB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validateInput(isNoun(nounA), isNoun(nounB));

        ArrayList<Integer> positionsOfA = nounPositions.get(nounA), positionsOfB = nounPositions.get(nounB);

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