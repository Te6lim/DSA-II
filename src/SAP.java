import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Queue;

import java.util.HashMap;

public class SAP {

    private final Digraph digraph;

    private HashMap<Integer, Boolean> marker;
    private HashMap<Integer, Integer> childCount;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        digraph = G;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) { return 0; }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        Queue<Integer> queueV = new Queue<>();
        Queue<Integer> queueW = new Queue<>();
        marker = new HashMap<>();
        childCount = new HashMap<>();

        queueV.enqueue(v);
        queueW.enqueue(w);

        marker.put(v, true);
        marker.put(w, true);
        childCount.put(v, 0);
        childCount.put(w, 0);

        boolean toV = true;

        int current;
        while (!queueV.isEmpty() || !queueW.isEmpty()) {
            if (toV) {
                if (!queueV.isEmpty()) {
                    current = queueV.dequeue();
                    if (digraph.outdegree(current) > 0) {
                        int x = getMarkedOrEnqueueNeighboursOfCurrent(w, queueV, current);
                        if (x != -1) return x;
                    }
                }
                toV = false;
            } else {
                if (!queueW.isEmpty()) {
                    current = queueW.dequeue();
                    if (digraph.outdegree(current) > 0) {
                        int x = getMarkedOrEnqueueNeighboursOfCurrent(v, queueW, current);
                        if (x != -1) return x;
                    }
                }
                toV = true;
            }
        }


        return -1;
    }

    private int getMarkedOrEnqueueNeighboursOfCurrent(int w, Queue<Integer> queueV, int current) {
        for (int x : digraph.adj(current)) {
            increaseChildCount(current, x);
            if (isMarked(x) && x != w) return x;
            else {
                queueV.enqueue(x);
                marker.put(x, true);
            }
        }
        return -1;
    }

    private boolean isMarked(int x) {
        return marker.getOrDefault(x, false);
    }

    private void increaseChildCount(int current, int x) {
        if (childCount.containsKey(x)) {
            childCount.replace(x, childCount.get(x) + childCount.get(current) + 1);
        } else childCount.put(x, childCount.get(current) + 1);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) { return 0; }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) { return 0; }

    // do unit testing of this class
    public static void main(String[] args) {}

}
