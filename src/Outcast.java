
public class Outcast {

    private final WordNet wn;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        wn = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns){
        int[] distances = new int[nouns.length];
        int largestNounIndex = 0;
        for (int i = 0; i < nouns.length; ++i) {
            for (int j = i + 1; j < nouns.length; ++j) {
                distances[i] += wn.distance(nouns[i], nouns[j]);
            }
            if (distances[i] > distances[largestNounIndex]) largestNounIndex = i;
        }

        return nouns[largestNounIndex];
    }

    // see test client below
    public static void main(String[] args) {
        /*WordNet wn = new WordNet(
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src\\" +
                        "\\synsets.txt",
                "C:\\Users\\ADMIN\\IdeaProjects\\DSA II\\src" +
                        "\\hypernyms.txt"
        );

        ArrayList<String> nouns = new ArrayList<>();

        for (String n : wn.nouns()) nouns.add(n);

        Outcast oc = new Outcast(wn);
        StdOut.println(nouns.size());
        StdOut.println(oc.outcast(nouns.toArray(new String[0])));*/
    }


}