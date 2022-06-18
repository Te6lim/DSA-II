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

    private Picture mPicture;
    private Pixel[][] energyMatrix;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        validateObject(picture);
        mPicture = new Picture(picture);
        energyMatrix = getEnergyMatrixFrom(picture);
    }

    private Pixel[][] getEnergyMatrixFrom(Picture picture) {

        Pixel[][] matrix = new Pixel[picture.width()][picture.height()];

        for (int v = 0; v < picture.height() * picture.width(); ++v) {
            matrix[row(v)][col(v)] = new Pixel(v);
            addVerticalEdges(v,matrix);
            addHorizontalEdges(v, matrix);
            matrix[row(v)][col(v)].energy = calculateEnergyOf(v);
        }

        return matrix;
    }

    private double calculateEnergyOf(int v) {
        if (col(v) == 0 || col(v) == mPicture.width() - 1 || row(v) == 0 || row(v) == mPicture.height() - 1)
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
        int col = col(v);
        int row = row(v);

        switch (rgb) {
            case RED : {
                if (col - 1 >= 0 && col + 1 < mPicture.width())
                    return mPicture.get(row, col + 1).getRed() - mPicture.get(row, col - 1).getRed();
                if (col - 1 < 0) return mPicture.get(row, col + 1).getRed();
                else return mPicture.get(row, col - 1).getRed();
            }

            case GREEN : {
                if (col - 1 >= 0 && col + 1 < mPicture.width())
                    return mPicture.get(row, col + 1).getGreen() - mPicture.get(row, col - 1).getGreen();
                if (col - 1 < 0) return mPicture.get(row, col + 1).getGreen();
                else return mPicture.get(row, col - 1).getGreen();
            }

            case BLUE : {
                if (col - 1 >= 0 && col + 1 < mPicture.width())
                    return mPicture.get(row, col + 1).getBlue() - mPicture.get(row, col - 1).getBlue();
                if (col - 1 < 0) return mPicture.get(row, col + 1).getBlue();
                else return mPicture.get(row, col - 1).getBlue();
            }
            default: throw new IllegalArgumentException();
        }
    }

    private int getYColorDiff(int v, RGB rgb) {
        int col = col(v);
        int row = row(v);

        switch (rgb) {
            case RED : {
                if (row - 1 >= 0 && row + 1 < mPicture.height())
                    return mPicture.get(row + 1, col).getRed() - mPicture.get(row - 1, col).getRed();
                if (row - 1 < 0) return mPicture.get(row + 1, col).getRed();
                else return mPicture.get(row - 1, col).getRed();
            }

            case GREEN : {
                if (row - 1 >= 0 && row + 1 < mPicture.height())
                    return mPicture.get(row + 1, col).getGreen() - mPicture.get(row - 1, col).getGreen();
                if (row - 1 < 0) return mPicture.get(row + 1, col).getGreen();
                else return mPicture.get(row - 1, col).getGreen();
            }

            case BLUE : {
                if (row - 1 >= 0 && row + 1 < mPicture.height())
                    return mPicture.get(row + 1, col).getBlue() - mPicture.get(row - 1, col).getBlue();
                if (row - 1 < 0) return mPicture.get(row + 1, col).getBlue();
                else return mPicture.get(row - 1, col).getBlue();
            }

            default: throw new IllegalArgumentException();
        }
    }

    private void addVerticalEdges(int v, Pixel[][] matrix) {
        int col = col(v);
        int row = row(v);

        if (col - 1 >= 0 && row + 1 < mPicture.height()) matrix[row][col].addVerticalEdgeTo(v + (mPicture.width() - 1));
        if (row + 1 < mPicture.height()) matrix[row][col].addVerticalEdgeTo(v + mPicture.width());
        if (col + 1 < mPicture.width() && row + 1 < mPicture.height()) matrix[row][col].addVerticalEdgeTo(v + mPicture.width() + 1);
        /*StdOut.println("Parent= " + v);
        for (int p : matrix[x][y].verticalEdges.keySet()) {
            StdOut.print(" " + p);
        }
        StdOut.println("\n");*/
    }

    private void addHorizontalEdges(int v, Pixel[][] matrix) {
        int col = col(v);
        int row = row(v);

        if (col + 1 < mPicture.width() && row + 1 < mPicture.height())
            matrix[row][col].addHorizontalEdgeTo(v + (mPicture.width() + 1));
        if (col + 1 < mPicture.width()) matrix[row][col].addHorizontalEdgeTo(v + 1);
        if (row - 1 >= 0 && col + 1 < mPicture.width())
            matrix[row][col].addHorizontalEdgeTo((v - mPicture.width()) + 1);
        /*StdOut.println("Parent= " + v);
        for (int p : matrix[row][col].horizontalEdges.keySet()) {
            StdOut.print(" " + p);
        }
        StdOut.println("\n");*/
    }

    private int col(int v) {
        return v % mPicture.width();
    }

    private int row(int v) {
        return v / mPicture.width();
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
        int[] seamHorizontalCoordinates = new int[0];
        double totalEnergy = -1.0;

        for (int s = 0; s < mPicture.height(); ++s) {
            Pixel source = energyMatrix[row(s * mPicture.width())][col(s * mPicture.width())];

            ArrayList<Integer> seam = findSeam(source, false);

            double tempTotalEnergy = totalEnergyOf(seam);

            /*for (int i = 0; i < seam.size(); ++i) {
                if (i + 1 < seam.size()) StdOut.print(seam.get(i) + "->");
                else StdOut.print(seam.get(i));
            }
            StdOut.println(" total energy= " + tempTotalEnergy);*/

            if (totalEnergy < 0) {
                totalEnergy = tempTotalEnergy;
                seamHorizontalCoordinates = extractCoordinatesFrom(seam, false);
            } else {
                if (tempTotalEnergy < totalEnergy) {
                    totalEnergy = tempTotalEnergy;
                    seamHorizontalCoordinates = extractCoordinatesFrom(seam, false);
                }
            }
        }
        return seamHorizontalCoordinates;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int[] seamVerticalCoordinates = new int[0];
        double totalEnergy = -1.0;

        for (int s = 0; s < mPicture.width(); ++s) {

            Pixel source = energyMatrix[row(s)][col(s)];

            ArrayList<Integer> seam = findSeam(source, true);

            double tempTotalEnergy = totalEnergyOf(seam);

            /*for (int i = 0; i < seam.size(); ++i) {
                if (i + 1 < seam.size()) StdOut.print(seam.get(i) + "->");
                else StdOut.print(seam.get(i));
            }
            StdOut.println(" total energy= " + tempTotalEnergy);*/

            if (totalEnergy < 0) {
                totalEnergy = tempTotalEnergy;
                seamVerticalCoordinates = extractCoordinatesFrom(seam, true);
            } else {
                if (tempTotalEnergy < totalEnergy) {
                    totalEnergy = tempTotalEnergy;
                    seamVerticalCoordinates = extractCoordinatesFrom(seam, true);
                }
            }
        }
        StdOut.println();
        return seamVerticalCoordinates;
    }

    private ArrayList<Integer> findSeam(Pixel source, boolean isVertical) {
        HashMap<Integer, Boolean> visited = new HashMap<>();
        ArrayList<Integer> pixels = new ArrayList<>();

        MinPQ<Pixel> pQ = new MinPQ<>();
        pQ.insert(source);
        visited.put(source.position, false);
        Pixel parent = null;

        while (!pQ.isEmpty()) {
            Pixel p = pQ.delMin();
            if (!visited.getOrDefault(p.position, false)) {
                visited.replace(p.position, true);
                if (parent != null) {
                    markChildrenAsVisited(visited, parent, isVertical);
                }
                pixels.add(p.position);
                parent = p;
                addChildrenToPQ(pQ, visited, p, isVertical);
            }
        }
        return pixels;
    }

    private double totalEnergyOf(ArrayList<Integer> seam) {
        double total = 0.0;
        for (int v : seam) total += energyMatrix[row(v)][col(v)].energy;
        return total;
    }

    private int[] extractCoordinatesFrom(ArrayList<Integer> vertices, boolean vertical) {
        int[] coordinates = new int[vertices.size()];
        if (vertical) {
            for (int x = 0; x < vertices.size(); ++x) {
                coordinates[x] = col(vertices.get(x));
            }
            return coordinates;
        } else {
            for (int x = 0; x < vertices.size(); ++x) {
                coordinates[x] = row(vertices.get(x));
            }
            return coordinates;
        }
    }

    private void addChildrenToPQ(MinPQ<Pixel> pQ, HashMap<Integer, Boolean> visited, Pixel p, boolean vertical) {
        if (vertical) {
            for (int i : p.verticalEdges.keySet()) {
                Pixel pixel = energyMatrix[row(i)][col(i)];
                pQ.insert(pixel);
                visited.put(i, false);
            }
        } else {
            for (int i : p.horizontalEdges.keySet()) {
                Pixel pixel = energyMatrix[row(i)][col(i)];
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

        int newWidth = mPicture.width() - 1;
        int newHeight = mPicture.height();

        Picture pic = new Picture(newWidth, newHeight);

        for (int c = 0; c < mPicture.width(); ++c) {
            for (int r = 0; r < mPicture.height(); ++r) {
                if (r != seam[c]) pic.set(r, c, mPicture.get(r, c));
            }
        }
        mPicture = pic;
        energyMatrix = getEnergyMatrixFrom(pic);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateObject(seam);
        verifyVerticalSeam(seam);
        validatePictureDimension(mPicture.height());
        int newWidth = mPicture.width() - 1;
        int newHeight = mPicture.height();

        Picture pic = new Picture(newWidth, newHeight);

        for (int r = 0; r < mPicture.height(); ++r) {
            for (int c = 0; c < mPicture.width(); ++c) {
                if (c != seam[r]) pic.set(r, c, mPicture.get(r, c));
            }
        }
        mPicture = pic;
        energyMatrix = getEnergyMatrixFrom(pic);
    }

    private void validatePictureDimension(int dimension) {
        if (dimension <= 1) throw new IllegalArgumentException();
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
            if (horizontalEdges.get(q) == null) horizontalEdges.put(q, position);
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

        int[] verticalSeam = carver.findVerticalSeam();
        int[] horizontalSeam = carver.findHorizontalSeam();

        StdOut.print("Vertical seam: ");
        for (int i = 0; i < verticalSeam.length; ++i) {
            if (i + 1 < verticalSeam.length) StdOut.print(verticalSeam[i] + "->");
            else StdOut.println(verticalSeam[i]);
        }

        StdOut.println();

        StdOut.print("Horizontal seam: ");
        for (int i = 0; i < horizontalSeam.length; ++i) {
            if (i + 1 < horizontalSeam.length) StdOut.print(horizontalSeam[i] + "->");
            else StdOut.println(horizontalSeam[i]);
        }

    }
}
