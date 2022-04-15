import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WordNet {

    private final ArrayList<Bag<String>> synsets;
    private final ArrayList<Bag<Integer>> hypernyms;

    private HashMap<Integer, Boolean> markerA;
    private HashMap<Integer, Boolean> markerB;
    private HashMap<Integer, Integer> childCountA;
    private HashMap<Integer, Integer> childCountB;

    private final ArrayList<String> nouns;

    private Integer rootPosition = null;

    // constructor takes the name of the two input files
    public WordNet(String synSetsFileName, String hypernymFileName) {
        validateInput(synSetsFileName != null, hypernymFileName != null);

        nouns = new ArrayList<>();

        synsets = getSynSetsFromFileInput(synSetsFileName);
        hypernyms = getHypernymsFromInputFile(hypernymFileName);

        Collections.sort(nouns);

        if (!isRootDAG()) throw new IllegalArgumentException();

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
        if (!b || !b2) throw new IllegalArgumentException();
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() { return nouns; }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return !(Collections.binarySearch(nouns, word) < 0);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        ArrayList<Integer> positionsOfA = getNounPositions(nounA), positionsOfB = getNounPositions(nounB);
        HashMap<Integer, Integer> lengthsMap = createShortestAncestralPathsFromVertices(positionsOfA, positionsOfB);

        return getShortestLength(lengthsMap);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validateInput(isNoun(nounA), isNoun(nounB));
        ArrayList<Integer> positionsOfA = getNounPositions(nounA), positionsOfB = getNounPositions(nounB);
        HashMap<Integer, Integer> lengthsMap = createShortestAncestralPathsFromVertices(positionsOfA, positionsOfB);

        int shortestLength = getShortestLength(lengthsMap);

        int ancestor = getAncestorOfShortestLength(lengthsMap, shortestLength);

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

    private int getAncestorOfShortestLength(HashMap<Integer, Integer> lengthsMap, int shortestLength) {
        for (int l : lengthsMap.keySet()) {
            if (lengthsMap.get(l) == shortestLength) return l;
        }
        return -1;
    }

    private int getShortestLength(HashMap<Integer, Integer> lengthsMap) {
        int shortestLength = -1;
        for (int k : lengthsMap.values()) {
            if (shortestLength != -1) {
                if (k < shortestLength) shortestLength = k;
            } else shortestLength = k;
        }
        return shortestLength;
    }

    private HashMap<Integer, Integer> createShortestAncestralPathsFromVertices(
            ArrayList<Integer> positionsOfA, ArrayList<Integer> positionsOfB
    ) {
        HashMap<Integer, Integer> lengthsMap = new HashMap<>();
        int length;
        int ancestor;
        for (int i : positionsOfA) {
            for (int j : positionsOfB) {
                ancestor = findCommonAncestor(i, j);
                if (ancestor != -1) {
                    length = childCountA.get(ancestor) + childCountB.get(ancestor);
                    addOrReplaceAncestorLength(lengthsMap, ancestor, length);
                }
            }
        }
        return lengthsMap;
    }

    private void addOrReplaceAncestorLength(HashMap<Integer, Integer> lengthsMap, int ancestor, int length) {
        if (lengthsMap.containsKey(ancestor)) {
            if (length < lengthsMap.get(ancestor)) lengthsMap.replace(ancestor, length);
        } else lengthsMap.put(ancestor, length);
    }

    private ArrayList<Integer> getNounPositions(String noun) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < synsets.size(); ++i) {
            for (String s : synsets.get(i)) if (s.equals(noun)) positions.add(i);
        }
        return positions;
    }

    private int findCommonAncestor(int a, int b) {
        Queue<Integer> queueA = new Queue<>();
        Queue<Integer> queueB = new Queue<>();
        markerA = new HashMap<>();
        markerB = new HashMap<>();
        childCountA = new HashMap<>();
        childCountB = new HashMap<>();

        queueA.enqueue(a);
        queueB.enqueue(b);
        markerA.put(a, true);
        markerB.put(b, true);
        childCountA.put(a, 0);
        childCountB.put(b, 0);

        boolean toA = true;

        int current;
        while (!queueA.isEmpty() || !queueB.isEmpty()) {
            if (toA) {
                if (!queueA.isEmpty()) {
                    current = queueA.dequeue();
                    if (isMarked(current, false)) childCountB.replace(current, 0);
                    if (hypernyms.get(current) != null) {
                        int sy = getMarkedOrEnqueue(b, queueA, current, true);
                        if (sy != -1) return sy;
                    }
                }
                toA = false;
            } else {
                if (!queueB.isEmpty()) {
                    current = queueB.dequeue();
                    if (isMarked(current, false)) childCountB.replace(current, 0);
                    if (hypernyms.get(current) != null) {
                        int sy = getMarkedOrEnqueue(a, queueB, current, false);
                        if (sy != -1) return sy;
                    }
                }
                toA = true;
            }
        }
        return -1;
    }

    private int getMarkedOrEnqueue(int root, Queue<Integer> queue, int current, boolean isA) {
        for (int sy : hypernyms.get(current)) {
            increaseChildCount(sy, current, isA);
            if (isMarked(sy, !isA) && sy != root) {
                return sy;
            } else {
                queue.enqueue(sy);
                if (isA) markerA.put(sy, true);
                else markerB.put(sy, true);
            }
        }
        return -1;
    }

    private boolean isMarked(int sy, boolean isA) {
        if (isA) return markerA.getOrDefault(sy, false);
        return markerB.getOrDefault(sy, false);
    }

    private void increaseChildCount(int parent, int child, boolean isA) {
        if (isA) {
            if (!childCountA.containsKey(parent)) {
                childCountA.put(parent, childCountA.get(child) + 1);
            }
        } else {
            if (!childCountB.containsKey(parent)) {
                childCountB.put(parent, childCountB.get(child) + 1);
            }
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wn = new WordNet(
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src\\" +
                        "\\synsets.txt",
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src" +
                        "\\hypernyms.txt"
        );
        String nounA = "locomotion", nounB = "running";

        StdOut.println(wn.sap(nounA, nounB));
        StdOut.println(wn.distance(nounA, nounB));
    }

}