import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Queue;

import java.util.HashMap;

public class SAP {

    private final Digraph digraph;

    private HashMap<Integer, Integer> childCounterV;
    private HashMap<Integer, Integer> childCounterW;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = new Digraph(G.V());
        for (int e = 0; e < G.V(); ++e) {
            if (G.outdegree(e) > 0) {
                for (int n : G.adj(e)) { digraph.addEdge(e, n); }
            }
        }
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        int ancestor = ancestor(v, w);
        if (ancestor != -1) return childCounterV.get(ancestor) + childCounterW.get(ancestor);
        else return -1;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        Queue<Integer> queueV = new Queue<>();
        HashMap<Integer, Boolean> markerV = new HashMap<>();
        childCounterV = new HashMap<>();
        markAndEnqueue(queueV, markerV, v);
        childCounterV.put(v, 0);

        Queue<Integer> queueW = new Queue<>();
        HashMap<Integer, Boolean> markerW = new HashMap<>();
        childCounterW = new HashMap<>();
        markAndEnqueue(queueW, markerW, w);
        childCounterW.put(w, 0);

        if (v == w) return v;

        int smallestCommonAncestor = -1;

        boolean toA = true;

        int current;
        while (!queueV.isEmpty() || !queueW.isEmpty()) {
            if (toA && !queueV.isEmpty()) {
                current = queueV.dequeue();
                if (digraph.outdegree(current) > 0) {
                    for (int sy : digraph.adj(current)) {
                        increaseChildCount(childCounterV, sy, current);
                        if (!isMarked(markerV, sy)) markAndEnqueue(queueV, markerV, sy);
                        if (isMarked(markerW, sy)) {
                            smallestCommonAncestor = getSmallestChildCountParent(smallestCommonAncestor, sy);
                        }
                    }
                }
                toA = queueW.isEmpty();
            } else {
                current = queueW.dequeue();
                if (digraph.outdegree(current) > 0) {
                    for (int sy : digraph.adj(current)) {
                        increaseChildCount(childCounterW, sy, current);
                        if (!isMarked(markerW, sy)) markAndEnqueue(queueW, markerW, sy);
                        if (isMarked(markerV, sy)) {
                            smallestCommonAncestor = getSmallestChildCountParent(smallestCommonAncestor, sy);
                        }
                    }
                }
                toA = !queueV.isEmpty();
            }
        }
        return smallestCommonAncestor;
    }

    private int getSmallestChildCountParent(int smallestCommonAncestor, int sy) {
        if (smallestCommonAncestor == -1) smallestCommonAncestor = sy;
        else {
            if (childCounterV.get(sy) + childCounterW.get(sy) <
                    childCounterV.get(smallestCommonAncestor) + childCounterW.get(smallestCommonAncestor)) {
                smallestCommonAncestor = sy;
            }
        }
        return smallestCommonAncestor;
    }

    private void markAndEnqueue(Queue<Integer> queue, HashMap<Integer, Boolean> marker, int position) {
        queue.enqueue(position);
        marker.put(position, true);
    }

    private boolean isMarked(HashMap<Integer, Boolean> marker, int sy) {
        return marker.getOrDefault(sy, false);
    }

    private void increaseChildCount(HashMap<Integer, Integer> childCounter, int parent, int child) {
        childCounter.putIfAbsent(parent, childCounter.get(child) + 1);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        validate(v, w);
        HashMap<Integer, Integer> lengthMap = createMapOfShortestLengthAncestors(v, w);

        return getShortestLength(lengthMap);
    }

    private void validate(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
        for (Integer i : v) {
            if (i == null) throw new IllegalArgumentException();
        }

        for (Integer i : w) {
            if (i == null) throw new IllegalArgumentException();
        }
    }

    private int getShortestLength(HashMap<Integer, Integer> lengthMap) {
        int shortestLength = -1;
        for (int p : lengthMap.values()) {
            if (shortestLength != -1) {
                if (p < shortestLength) shortestLength = p;
            } else shortestLength = p;
        }
        return shortestLength;
    }

    private HashMap<Integer, Integer> createMapOfShortestLengthAncestors(Iterable<Integer> v, Iterable<Integer> w) {
        HashMap<Integer, Integer> lengthMap = new HashMap<>();
        int ancestor;
        int length;
        for (int j : v) {
            for (int k : w) {
                ancestor = ancestor(j, k);
                if (ancestor != -1) {
                    length = childCounterV.get(ancestor) + childCounterW.get(ancestor);
                    addShortestAncestralLengthToMap(lengthMap, length, ancestor);
                }
            }
        }
        return lengthMap;
    }

    private void addShortestAncestralLengthToMap(HashMap<Integer, Integer> lengthMap, int length, int ancestor) {
        if (lengthMap.containsKey(ancestor)) {
            if (length < lengthMap.get(ancestor)) lengthMap.replace(ancestor, length);
        } else lengthMap.put(ancestor, length);
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        validate(v, w);
        HashMap<Integer, Integer> lengthMap = createMapOfShortestLengthAncestors(v, w);
        int shortestLength = getShortestLength(lengthMap);
        return getShortestLengthAncestor(lengthMap, shortestLength);
    }

    private int getShortestLengthAncestor(HashMap<Integer, Integer> lengthMap, int shortestLength) {
        for (int k : lengthMap.keySet()) {
            if (lengthMap.get(k) == shortestLength) return k;
        }
        return -1;
    }

    // do unit testing of this class
    public static void main(String[] args) {}

}
