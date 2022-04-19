import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Queue;

import java.util.HashMap;

public class SAP {

    private final Digraph digraph;

    private HashMap<Integer, Boolean> markerV;
    private HashMap<Integer, Boolean> markerW;
    private HashMap<Integer, Integer> childCounterV;
    private HashMap<Integer, Integer> childCounterW;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = G;
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
        Queue<Integer> queueW = new Queue<>();
        markerV = new HashMap<>();
        markerW = new HashMap<>();
        childCounterV = new HashMap<>();
        childCounterW = new HashMap<>();

        int ancestor = connection(v, w);
        if (ancestor != -1) return ancestor;

        if (v == w) {
            childCounterV.put(v, 0);
            childCounterW.put(w, 0);
            return v;
        }

        queueV.enqueue(v);
        markerV.put(v, true);
        childCounterV.put(v, 0);

        queueW.enqueue(w);
        markerW.put(w, true);
        childCounterW.put(w, 0);

        boolean isV = true;

        int current;
        while (!queueV.isEmpty() && !queueW.isEmpty()) {
            if (isV) {
                if (!queueV.isEmpty()) {
                    current = queueV.dequeue();
                    int x = getMarkedOrEnqueueNeighboursOfCurrent(queueV, markerV, childCounterV, current);
                    if (x != -1) return x;
                }
                isV = false;
            } else {
                if (!queueW.isEmpty()) {
                    current = queueW.dequeue();
                    int x = getMarkedOrEnqueueNeighboursOfCurrent(queueW, markerW, childCounterW, current);
                    if (x != -1) return x;
                }
                isV = true;
            }
        }
        return -1;
    }

    private int getMarkedOrEnqueueNeighboursOfCurrent(
            Queue<Integer> queue, HashMap<Integer, Boolean> marker, HashMap<Integer, Integer> childCounter, int current
    ) {
        if (digraph.outdegree(current) > 0) {
            for (int x : digraph.adj(current)) {
                increaseChildCount(childCounter, x, current);
                marker.put(x, true);
                if (isMarked(x)) return x;
                else {
                    queue.enqueue(x);
                }
            }
        }
        return -1;
    }

    private int connection(int v, int w) {
        if (digraph.outdegree(v) != 0) {
            for (int h : digraph.adj(v)) {
                if (h == w) {
                    childCounterV.put(h, 0);
                    childCounterW.put(h, 1);
                    return h;
                }
            }
        }

        if (digraph.outdegree(w) != 0) {
            for (int h : digraph.adj(w)) {
                if (h == v) {
                    childCounterV.put(h, 1);
                    childCounterW.put(h, 0);
                    return h;
                }
            }
        }
        return -1;
    }

    private boolean isMarked(int x) {
        return markerV.getOrDefault(x, false) && markerW.getOrDefault(x, false);
    }

    private void increaseChildCount(HashMap<Integer, Integer> childCounter, int parent, int child) {
        if (!childCounter.containsKey(parent)) childCounter.put(parent, childCounter.get(child) + 1);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        validate(v, w);
        HashMap<Integer, Integer> lengthMap = createMapOfShortestLengthAncestors(v, w);

        return getShortestLength(lengthMap);
    }

    private void validate(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
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
