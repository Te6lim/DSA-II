import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Queue;

import java.util.HashMap;

public class SAP {

    private final Digraph digraph;

    private HashMap<Integer, Boolean> markerV;
    private HashMap<Integer, Boolean> markerW;
    private HashMap<Integer, Integer> childCountV;
    private HashMap<Integer, Integer> childCountW;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = G;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        int ancestor = ancestor(v, w);
        return childCountV.get(ancestor) + childCountW.get(ancestor);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        Queue<Integer> queueV = new Queue<>();
        Queue<Integer> queueW = new Queue<>();
        markerV = new HashMap<>();
        markerW = new HashMap<>();
        childCountV = new HashMap<>();
        childCountW = new HashMap<>();

        queueV.enqueue(v);
        queueW.enqueue(w);

        markerV.put(v, true);
        markerW.put(w, true);
        childCountV.put(v, 0);
        childCountW.put(w, 0);

        boolean toV = true;

        int current;
        while (!queueV.isEmpty() || !queueW.isEmpty()) {
            if (toV) {
                if (!queueV.isEmpty()) {
                    current = queueV.dequeue();
                    int x = getMarkedOrEnqueueNeighboursOfCurrent(w, queueV, current, true);
                    if (x != -1) return x;
                }
                toV = false;
            } else {
                if (!queueW.isEmpty()) {
                    current = queueW.dequeue();
                    int x = getMarkedOrEnqueueNeighboursOfCurrent(v, queueW, current, false);
                    if (x != -1) return x;
                }
                toV = true;
            }
        }


        return -1;
    }

    private int getMarkedOrEnqueueNeighboursOfCurrent(int root, Queue<Integer> queue, int current, boolean isV) {
        if (digraph.outdegree(current) > 0) {
            for (int x : digraph.adj(current)) {
                increaseChildCount(x, current, isV);
                if (isMarked(x, isV) && x != root) return x;
                else {
                    queue.enqueue(x);
                    if (isV) markerV.put(x, true);
                    else markerW.put(x, true);
                    if (x == root) resetAllChildCount(isV);
                }
            }
        }
        return -1;
    }

    private void resetAllChildCount(boolean isV) {
        if (isV) {
            for (int c : childCountW.keySet()) {
                childCountW.replace(c, 0);
            }
        } else {
            for (int c : childCountV.keySet()) {
                childCountV.replace(c, 0);
            }
        }
    }

    private boolean isMarked(int x, boolean isV) {
        if (isV) return markerV.getOrDefault(x, false);
        return markerW.getOrDefault(x, false);
    }

    private void increaseChildCount(int parent, int child, boolean isV) {
        if (isV) {
            if (!childCountV.containsKey(parent)) childCountV.put(parent, childCountV.get(child) + 1);
        } else {
            if (!childCountW.containsKey(parent)) childCountW.put(parent, childCountV.get(child) + 1);
        }
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        HashMap<Integer, Integer> lengthMap = createMapOfShortestLengthAncestors(v, w);

        return getShortestLength(lengthMap);
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
                    length = childCountV.get(ancestor) + childCountW.get(ancestor);
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
