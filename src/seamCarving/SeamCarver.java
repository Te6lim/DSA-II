package seamCarving;

import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.awt.*;
import java.util.*;

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
            matrix[x(v)][y(v)] = new Pixel(v);
            addVerticalEdges(v,matrix);
            addHorizontalEdges(v, matrix);
            matrix[x(v)][y(v)].energy = calculateEnergyOf(v);
        }

        return matrix;
    }

    private double calculateEnergyOf(int v) {
        if (x(v) == 0 || x(v) == mPicture.width() - 1 || y(v) == 0 || y(v) == mPicture.height() - 1)
            return 1000;
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
                if (x - 1 >= 0 && x + 1 < mPicture.width())
                    return mPicture.get(x + 1, y).getRed() - mPicture.get(x - 1, y).getRed();
                if (x - 1 < 0) return mPicture.get(x + 1, y).getRed();
                else return mPicture.get(x - 1, y).getRed();
            }

            case GREEN : {
                if (x - 1 >= 0 && x + 1 < mPicture.width())
                    return mPicture.get(x + 1, y).getGreen() - mPicture.get(x - 1, y).getGreen();
                if (x - 1 < 0) return mPicture.get(x + 1, y).getGreen();
                else return mPicture.get(x - 1, y).getGreen();
            }

            case BLUE : {
                if (x - 1 >= 0 && x + 1 < mPicture.width())
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
                if (y - 1 >= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getRed() - mPicture.get(x, y - 1).getRed();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getRed();
                else return mPicture.get(x, y - 1).getRed();
            }

            case GREEN : {
                if (y - 1 >= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getGreen() - mPicture.get(x, y - 1).getGreen();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getGreen();
                else return mPicture.get(x, y - 1).getGreen();
            }

            case BLUE : {
                if (y - 1 >= 0 && y + 1 < mPicture.height())
                    return mPicture.get(x, y + 1).getBlue() - mPicture.get(x, y - 1).getBlue();
                if (y - 1 < 0) return mPicture.get(x, y + 1).getBlue();
                else return mPicture.get(x, y - 1).getBlue();
            }

            default: throw new IllegalArgumentException();
        }
    }

    private void addVerticalEdges(int v, Pixel[][] matrix) {
        int x = x(v);
        int y = y(v);

        if (x - 1 >= 0 && y + 1 < mPicture.height()) matrix[x][y].addVerticalEdgeTo(v + (mPicture.width() - 1));
        if (y + 1 < mPicture.height()) matrix[x][y].addVerticalEdgeTo(v + mPicture.width());
        if (x + 1 < mPicture.width() && y + 1 < mPicture.height()) matrix[x][y].addVerticalEdgeTo(v + mPicture.width() + 1);
        StdOut.println("Parent= " + v);
        for (int p : matrix[x][y].verticalEdges.keySet()) {
            StdOut.print(" " + p);
        }
        StdOut.println("\n");
    }

    private void addHorizontalEdges(int v, Pixel[][] matrix) {
        int x = x(v);
        int y = y(v);

        if (x + 1 < mPicture.width() && y + 1 < mPicture.height()) matrix[x][y].addHorizontalEdgeTo(v + (mPicture.width() + 1));
        if (x + 1 < mPicture.width()) matrix[x][y].addHorizontalEdgeTo(v + 1);
        if (y - 1 >= mPicture.height() && x + 1 < mPicture.width()) matrix[x][y].addHorizontalEdgeTo(v - (mPicture.width() - 1));
        StdOut.println("Parent= " + v);
        for (int p : matrix[x][y].horizontalEdges.keySet()) {
            StdOut.print(" " + p);
        }
        StdOut.println("\n");
    }

    private int x(int v) {
        return v % picture().width();
    }

    private int y(int v) {
        return v / picture().width();
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
        return energyMatrix[x][y].energy;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int[] seam = new int[0];
        double totalEnergy = -1.0;

        for (int s = 0; s < mPicture.height(); ++s) {
            ArrayList<Integer> edgeTo = new ArrayList<>();
            HashMap<Integer, Boolean> visited = new HashMap<>();

            MinPQ<Pixel> pQ = new MinPQ<>();
            pQ.insert(energyMatrix[x(mPicture.width() * s)][y(mPicture.width() * s)]);
            visited.put(s, false);

            Pixel parent = null;
            double tempTotalEnergy = 0.0f;

            tempTotalEnergy = getTotalEnergyOfSeam(edgeTo, visited, pQ, false);

            for (int i = 0; i < edgeTo.size(); ++i) {
                if (i + 1 < edgeTo.size()) StdOut.print(edgeTo.get(i) + "->");
                else StdOut.print(edgeTo.get(i));
            }
            StdOut.println(" total energy= " + tempTotalEnergy);

            if (totalEnergy < 0) {
                totalEnergy = tempTotalEnergy;
                seam = extractVerticalCoordinatesFrom(edgeTo);
            } else {
                if (tempTotalEnergy < totalEnergy) {
                    totalEnergy = tempTotalEnergy;
                    seam = extractVerticalCoordinatesFrom(edgeTo);
                }
            }
        }
        return seam;
    }

    private double getTotalEnergyOfSeam(ArrayList<Integer> edgeTo, HashMap<Integer, Boolean> visited, MinPQ<Pixel> pQ, boolean b) {
        Pixel parent = null;
        double totalEnergy = 0.0;
        while (!pQ.isEmpty()) {
            Pixel p = pQ.delMin();
            if (!visited.getOrDefault(p.position, false)) {
                visited.replace(p.position, true);
                if (parent != null) {
                    markChildrenAsVisited(visited, parent, b);
                }
                edgeTo.add(p.position);
                totalEnergy += energyMatrix[x(p.position)][y(p.position)].energy;
                parent = p;
                addVerticalChildrenToPQ(pQ, visited, p, b);
            }
        }
        return totalEnergy;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int[] seam = new int[0];
        double totalEnergy = -1.0;

        for (int s = 0; s < mPicture.width(); ++s) {
            ArrayList<Integer> edgeTo = new ArrayList<>();
            HashMap<Integer, Boolean> visited = new HashMap<>();

            MinPQ<Pixel> pQ = new MinPQ<>();
            pQ.insert(energyMatrix[x(s)][y(s)]);
            visited.put(s, false);

            Pixel parent = null;
            double tempTotalEnergy = 0.0f;

            tempTotalEnergy = getTotalEnergyOfSeam(edgeTo, visited, pQ, true);

            for (int i = 0; i < edgeTo.size(); ++i) {
                if (i + 1 < edgeTo.size()) StdOut.print(edgeTo.get(i) + "->");
                else StdOut.print(edgeTo.get(i));
            }
            StdOut.println(" total energy= " + tempTotalEnergy);

            if (totalEnergy < 0) {
                totalEnergy = tempTotalEnergy;
                seam = extractVerticalCoordinatesFrom(edgeTo);
            } else {
                if (tempTotalEnergy < totalEnergy) {
                    totalEnergy = tempTotalEnergy;
                    seam = extractVerticalCoordinatesFrom(edgeTo);
                }
            }
        }
        return seam;
    }

    private int[] extractVerticalCoordinatesFrom(ArrayList<Integer> edgeTo) {
        int[] yCoordinates = new int[edgeTo.size()];
        for (int x = 0; x < edgeTo.size(); ++x) {
            yCoordinates[x] = edgeTo.get(x);
        }
        return yCoordinates;
    }

    private void addVerticalChildrenToPQ(MinPQ<Pixel> pQ, HashMap<Integer, Boolean> visited, Pixel p, boolean vertical) {
        if (vertical) {
            for (int i : p.verticalEdges.keySet()) {
                Pixel pixel = energyMatrix[x(i)][y(i)];
                pQ.insert(pixel);
                visited.put(i, false);
            }
        } else {
            for (int i : p.horizontalEdges.keySet()) {
                Pixel pixel = energyMatrix[x(i)][y(i)];
                pQ.insert(pixel);
                visited.put(i, false);
            }
        }
    }

    private void markChildrenAsVisited(HashMap<Integer, Boolean> visited, Pixel parent, boolean vertical) {
        if (vertical) {
            for (int e : parent.verticalEdges.keySet()) {
                visited.replace(e, true);
            }
        } else {
            for (int e : parent.horizontalEdges.keySet()) {
                visited.replace(e, true);
            }
        }
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

        HashMap<Integer, Integer> verticalEdges = new HashMap<>();
        HashMap<Integer, Integer> horizontalEdges = new HashMap<>();

        private final int position;

        private Pixel(int p) { position = p; }

        double energy;

        private void addVerticalEdgeTo(int q) {
            if (verticalEdges.get(q) == null) verticalEdges.put(q, position);
            else verticalEdges.replace(q, position);
        }

        private void addHorizontalEdgeTo(int q) {
            if (verticalEdges.get(q) == null) horizontalEdges.put(q, position);
            else horizontalEdges.replace(q, position);
        }

        @Override
        public int compareTo(Pixel p) {
            return Double.compare(energy, p.energy);
        }
    }

    // unit testing (optional)
    public static void main(String[] args) {

        int width = 5; int height = 5;

        Picture p = new Picture(width, height);

        Random rand = new Random();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int r = rand.nextInt(256);
                int g = rand.nextInt(256);
                int b = rand.nextInt(256);
                p.set(x, y, new Color(r, g, b));
            }
        }

        SeamCarver carver = new SeamCarver(p);

        for (int x = 0; x < p.width(); ++x) {
            for (int y = 0; y < p.height(); ++y) {
                StdOut.print("[" + carver.energyMatrix[x][y].energy + "]");
            }
            StdOut.println();
        }

        int[] seam = carver.findVerticalSeam();

        StdOut.println("seam size = " + seam.length);

        for (int i = 0; i < seam.length; ++i) {
            if (i + 1 < seam.length) StdOut.print(seam[i] + "->");
            else StdOut.println(seam[i]);
        }

    }
}
