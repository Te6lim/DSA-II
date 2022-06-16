package seamCarving;

import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Picture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SeamCarver {

    private enum RGB {
        RED, GREEN, BLUE
    }

    private final Picture mPicture;
    private final Pixel[][] energyMatrix;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        validateObject(picture);
        mPicture = new Picture(picture);
        energyMatrix = getEnergyMatrixFrom(picture);
    }

    private Pixel[][] getEnergyMatrixFrom(Picture picture) {

        Pixel[][] matrix = new Pixel[picture.width()][picture.height()];

        for (int v = 0; v < picture.height() * picture.width(); ++v) {
            addDownwardEdges(v,matrix);
            matrix[x(v)][y(v)].energy = calculateEnergyOf(v);
        }

        return matrix;
    }

    private double calculateEnergyOf(int v) {
        int deltaXSquared = getDeltaXSquared(v);
        int deltaYSquared = getDeltaYSquared(v);
        return Math.sqrt((double) deltaXSquared + (double)deltaYSquared);
    }

    private int getDeltaXSquared(int v) {
        int rx = getXColorDiff(v, RGB.RED);
        int gx = getXColorDiff(v, RGB.GREEN);
        int bx = getXColorDiff(v, RGB.BLUE);

        int deltaRXSquared = rx * rx;
        int deltaGXSquared = gx * gx;
        int deltaBXSquared = bx * bx;

        return deltaRXSquared + deltaGXSquared + deltaBXSquared;
    }

    private int getDeltaYSquared(int v) {
        int ry = getYColorDiff(v, RGB.RED);
        int gy = getYColorDiff(v, RGB.GREEN);
        int by = getYColorDiff(v, RGB.BLUE);

        int deltaRYSquared = ry * ry;
        int deltaGYSquared = gy * gy;
        int deltaBYSquared = by * by;

        return deltaRYSquared + deltaGYSquared + deltaBYSquared;
    }

    private int getXColorDiff(int v, RGB rgb) {
        int x = x(v);
        int y = y(v);

        switch (rgb) {
            case RED : {
                if (x - 1 <= 0 && x + 1 < mPicture.width())
                    return mPicture.get(x + 1, y).getRed() - mPicture.get(x - 1, y).getRed();
                if (x - 1 < 0) return mPicture.get(x + 1, y).getRed();
                else return mPicture.get(x - 1, y).getRed();
            }

            case GREEN : {
                if (x - 1 <= 0 && x + 1 < mPicture.width())
                    return mPicture.get(x + 1, y).getGreen() - mPicture.get(x - 1, y).getGreen();
                if (x - 1 < 0) return mPicture.get(x + 1, y).getGreen();
                else return mPicture.get(x - 1, y).getGreen();
            }

            case BLUE : {
                if (x - 1 <= 0 && x + 1 < mPicture.width())
                    return mPicture.get(x + 1, y).getBlue() - mPicture.get(x - 1, y).getBlue();
                if (x - 1 < 0) return mPicture.get(x + 1, y).getBlue();
                else return mPicture.get(x - 1, y).getBlue();
            }
            default: throw new IllegalArgumentException();
        }
    }

    private int getYColorDiff(int v, RGB rgb) {
        int x = x(v);
        int y = y(v);

        switch (rgb) {
            case RED : {
                if (y - 1 <= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getRed() - mPicture.get(x, y - 1).getRed();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getRed();
                else return mPicture.get(x, y - 1).getRed();
            }

            case GREEN : {
                if (y - 1 <= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getGreen() - mPicture.get(x, y - 1).getGreen();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getGreen();
                else return mPicture.get(x, y - 1).getGreen();
            }

            case BLUE : {
                if (y - 1 <= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getBlue() - mPicture.get(x, y - 1).getBlue();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getBlue();
                else return mPicture.get(x, y - 1).getBlue();
            }

            default: throw new IllegalArgumentException();
        }
    }

    private void addDownwardEdges(int v, Pixel[][] matrix) {
        int x = x(v);
        int y = y(v);

        matrix[x][y] = new Pixel(v);

        if (x - 1 >= 0 && y + 1 < mPicture.height()) matrix[x][y].addEdgeTo(v + (mPicture.width() - 1));
        if (y + 1 < mPicture.height()) matrix[x][y].addEdgeTo(v + mPicture.width());
        if (x + 1 < mPicture.width() && y + 1 < mPicture.height()) matrix[x][y].addEdgeTo(v + mPicture.width() + 1);
    }

    private int x(int v) {
        return v / picture().width();
    }

    private int y(int v) {
        return v % picture().width();
    }

    // current picture
    public Picture picture() {
        return mPicture;
    }

    // width of current picture
    public int width() {
        return mPicture.width();
    }

    // height of current picture
    public int height() {
        return mPicture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        verifyRangeOfX(x);
        verifyRangeOfY(y);

        int v = mPicture.width() * x + y;
        return energyMatrix[x][y].energy;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return null;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        ArrayList<Integer> edgeTo = new ArrayList<>();
        HashMap<Integer, Boolean> visited = new HashMap<>();

        MinPQ<Pixel> pQ = new MinPQ<>();
        int s = 1;
        pQ.insert(energyMatrix[x(s)][y(s)]);

        Pixel parent = null;

        while (!pQ.isEmpty()) {
            Pixel p = pQ.delMin();
            if (!visited.get(p.position)) {
                if (parent != null) {
                    for (int e : parent.edges.keySet()) {
                        if (e != parent.position)
                            parent.removeEdge(e);

                        visited.put(e, true);
                    }
                    edgeTo.add(parent.position);
                }
                edgeTo.add(p.position);
                parent = p;
                for (int i : p.edges.keySet()) pQ.insert(energyMatrix[x(i)][y(i)]);
            }
        }

        int[] list = new int[edgeTo.size() + 1];

        for (int x = 0; x < edgeTo.size();++x) {
            list[x] = y(edgeTo.get(x));
        }
        
        return list;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validateObject(seam);
        verifyHorizontalSeam(seam);
        validatePictureDimension(mPicture.width());
    }

    private void validatePictureDimension(int dimension) {
        if (dimension <= 1) throw new IllegalArgumentException();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateObject(seam);
        verifyVerticalSeam(seam);
        validatePictureDimension(mPicture.height());
    }

    private void validateObject(Object object) {
        if (object == null) throw new IllegalArgumentException();
    }

    private void verifyRangeOfX(int x) {
        if (x < 0 || x > mPicture.width() - 1) throw new IllegalArgumentException();
    }

    private void verifyRangeOfY(int y) {
        if (y < 0 || y > mPicture.height() - 1) throw new IllegalArgumentException();
    }

    private void verifyVerticalSeam(int[] seam) {
        if (seam.length < mPicture.height()) throw new IllegalArgumentException();
        Arrays.sort(seam);
        verifyRangeOfY(seam[0]);
        verifyRangeOfY(seam[seam.length - 1]);
        if (hasBreakage(seam)) throw new IllegalArgumentException();
    }

    private void verifyHorizontalSeam(int[] seam) {
        if (seam.length < picture().width()) throw new IllegalArgumentException();
        Arrays.sort(seam);
        verifyRangeOfX(seam[0]);
        verifyRangeOfX(seam[seam.length - 1]);
        if (hasBreakage(seam)) throw new IllegalArgumentException();
    }

    private boolean hasBreakage(int[] seam) {
        int temp = -1;
        for (int i : seam) {
            if (i != temp + 1) return true;
            ++temp;
        }
        return false;
    }

    private class Pixel implements Comparable<Pixel> {

        HashMap<Integer, Integer> edges = new HashMap<>();

        private final int position;

        private Pixel(int p) { position = p; }

        double energy;

        private void addEdgeTo(int q) {
            edges.put(q, position);
        }

        private void removeEdge(int r) {
            edges.remove(r);
        }

        @Override
        public int compareTo(Pixel p) {
            return Double.compare(energy, p.energy);
        }
    }

    // unit testing (optional)
    public static void main(String[] args) {

    }
}
