import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class SeamCarver {

    private enum RGB {
        RED, GREEN, BLUE
    }

    private Picture mPicture;
    private Pixel[][] energyMatrix;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        verifyObject(picture);
        mPicture = new Picture(picture);
        energyMatrix = getEnergyMatrixFrom(picture);
    }

    private Pixel[][] getEnergyMatrixFrom(Picture picture) {

        Pixel[][] matrix = new Pixel[picture.height()][picture.width()];

        for (int v = 0; v < picture.height() * picture.width(); ++v) {
            matrix[row(v)][col(v)] = new Pixel(v);
            matrix[row(v)][col(v)].energy = calculateEnergyOf(v);
        }

        for (Pixel[] row : matrix) {
            for (Pixel p : row) {
                addVerticalEdges(p.position, matrix);
                addHorizontalEdges(p.position, matrix);
            }
        }

        return matrix;
    }

    private double calculateEnergyOf(int v) {
        if (col(v) == 0 || col(v) == mPicture.width() - 1 || row(v) == 0 || row(v) == mPicture.height() - 1)
            return 1000;
        double deltaXSquared = getDeltaXSquared(v);
        double deltaYSquared = getDeltaYSquared(v);
        return Math.sqrt(deltaXSquared + deltaYSquared);
    }

    private double getDeltaXSquared(int v) {
        int rx = getXColorDiff(v, RGB.RED);
        int gx = getXColorDiff(v, RGB.GREEN);
        int bx = getXColorDiff(v, RGB.BLUE);

        double deltaRXSquared = Math.pow(rx, 2);
        double deltaGXSquared = Math.pow(gx, 2);
        double deltaBXSquared = Math.pow(bx, 2);

        return deltaRXSquared + deltaGXSquared + deltaBXSquared;
    }

    private double getDeltaYSquared(int v) {
        int ry = getYColorDiff(v, RGB.RED);
        int gy = getYColorDiff(v, RGB.GREEN);
        int by = getYColorDiff(v, RGB.BLUE);

        double deltaRYSquared = Math.pow(ry, 2);
        double deltaGYSquared = Math.pow(gy, 2);
        double deltaBYSquared = Math.pow(by, 2);

        return deltaRYSquared + deltaGYSquared + deltaBYSquared;
    }

    private int getXColorDiff(int v, RGB rgb) {
        int col = col(v);
        int row = row(v);

        switch (rgb) {
            case RED : return mPicture.get(col + 1, row).getRed() - mPicture.get(col - 1, row).getRed();

            case GREEN : return mPicture.get(col + 1, row).getGreen() - mPicture.get(col - 1, row).getGreen();

            case BLUE : return mPicture.get(col + 1, row).getBlue() - mPicture.get(col - 1, row).getBlue();

            default: throw new IllegalArgumentException();
        }
    }

    private int getYColorDiff(int v, RGB rgb) {
        int col = col(v);
        int row = row(v);

        switch (rgb) {
            case RED :
                    return mPicture.get(col, row + 1).getRed() - mPicture.get(col, row - 1).getRed();

            case GREEN :
                    return mPicture.get(col, row + 1).getGreen() - mPicture.get(col, row - 1).getGreen();

            case BLUE :
                    return mPicture.get(col, row + 1).getBlue() - mPicture.get(col, row - 1).getBlue();

            default: throw new IllegalArgumentException();
        }
    }

    private void addVerticalEdges(int v, Pixel[][] matrix) {
        int col = col(v);
        int row = row(v);

        if (col - 1 >= 0 && row + 1 < mPicture.height()) {
            int p = v + (mPicture.width() - 1);
            matrix[row][col].addVerticalEdgeTo(matrix[row(p)][col(p)]);
        };
        if (row + 1 < mPicture.height()) {
            int p = v + mPicture.width();
            matrix[row][col].addVerticalEdgeTo(matrix[row(p)][col(p)]);
        }
        if (col + 1 < mPicture.width() && row + 1 < mPicture.height()) {
            int p = v + mPicture.width() + 1;
            matrix[row][col].addVerticalEdgeTo(matrix[row(p)][col(p)]);
        }
    }

    private void addHorizontalEdges(int v, Pixel[][] matrix) {
        int col = col(v);
        int row = row(v);

        if (col + 1 < mPicture.width() && row + 1 < mPicture.height()) {
            int p = v + (mPicture.width() + 1);
            matrix[row][col].addHorizontalEdgeTo(matrix[row(p)][col(p)]);
        }
        if (col + 1 < mPicture.width()) {
            int p = v + 1;
            matrix[row][col].addHorizontalEdgeTo(matrix[row(p)][col(p)]);
        }
        if (row - 1 >= 0 && col + 1 < mPicture.width()) {
            int p = (v - mPicture.width()) + 1;
            matrix[row][col].addHorizontalEdgeTo(matrix[row(p)][col(p)]);
        }
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
        verifyColumn(x);
        verifyRow(y);
        return energyMatrix[y][x].energy;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int[] seamHorizontalCoordinates = new int[0];
        double totalEnergy = -1.0;

        ArrayList<Integer> seam = new ArrayList<>();

        for (int s = 0; s < mPicture.height(); ++s) {
            Pixel source = energyMatrix[row(s * mPicture.width())][col(s * mPicture.width())];

            seam = findSeam(source, false);

            double tempTotalEnergy = totalEnergyOf(seam);

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

        ArrayList<Integer> seam = new ArrayList<>();

        for (int s = 0; s < mPicture.width(); ++s) {

            Pixel source = energyMatrix[row(s)][col(s)];

            seam = findSeam(source, true);
            StdOut.print("Source " + s + ": ");
            for (int i : seam) {
                StdOut.print(" " + i);
            }
            StdOut.println();

            double tempTotalEnergy = totalEnergyOf(seam);

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
        return seamVerticalCoordinates;
    }

    private ArrayList<Integer> findSeam(Pixel source, boolean isVertical) {
        ArrayList<Integer> pixels = new ArrayList<>();
        Pixel p = source;

        while (p != null) {
            pixels.add(p.position);
            p.visited = true;
            Pixel smallestEnergyChild = getChildWithSmallestEnergy(p, isVertical);
            if (smallestEnergyChild != null) {
                if (smallestEnergyChild.visited) {
                    if (isVertical) {
                        if (row(smallestEnergyChild.position) + 1 != mPicture.height() - 1)
                            smallestEnergyChild.removeSmallestEdge(true);
                    } else {
                        if (col(smallestEnergyChild.position) + 1 != mPicture.width() - 1)
                            smallestEnergyChild.removeSmallestEdge(false);
                    }
                }
            }
            p = smallestEnergyChild;
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
        } else {
            for (int x = 0; x < vertices.size(); ++x) {
                coordinates[x] = row(vertices.get(x));
            }
        }
        return coordinates;
    }

    private Pixel getChildWithSmallestEnergy(Pixel p, boolean vertical) {
        Pixel smallestEnergyPixel = null;
        if (vertical) {
            if (p.verticalEdges.isEmpty()) return null;
            for (Pixel pixel : p.verticalEdges) {
                if (smallestEnergyPixel == null) smallestEnergyPixel = pixel;
                else {
                    if (pixel.energy < smallestEnergyPixel.energy) smallestEnergyPixel = pixel;
                }
            }
        } else {
            if (p.horizontalEdges.isEmpty()) return null;
            for (Pixel pixel : p.horizontalEdges) {
                if (smallestEnergyPixel == null) smallestEnergyPixel = pixel;
                else {
                    if (pixel.energy < smallestEnergyPixel.energy) smallestEnergyPixel = pixel;
                }
            }
        }
        return smallestEnergyPixel;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        verifyObject(seam);
        verifyHorizontalSeam(seam);
        verifyPictureDimension(mPicture.width());

        int newWidth = mPicture.width();
        int newHeight = mPicture.height() - 1;

        Picture pic = new Picture(newWidth, newHeight);

        for (int c = 0; c < mPicture.width(); ++c) {
            boolean foundCrack = false;
            for (int r = 0; r < mPicture.height(); ++r) {
                if (r != seam[c]) {
                    if (foundCrack) pic.set(c, r - 1, mPicture.get(c, r));
                    else pic.set(c, r, mPicture.get(c, r));
                } else foundCrack = true;
            }
        }
        mPicture = pic;
        energyMatrix = getEnergyMatrixFrom(pic);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        verifyObject(seam);
        verifyVerticalSeam(seam);
        verifyPictureDimension(mPicture.height());
        int width = mPicture.width() - 1;
        int height = mPicture.height();

        Picture pic = new Picture(width, height);

        for (int r = 0; r < mPicture.height(); ++r) {
            boolean foundCrack = false;
            for (int c = 0; c < mPicture.width(); ++c) {
                if (c != seam[r]) {
                    if (foundCrack) {
                        pic.set(c - 1, r, mPicture.get(c, r));
                    } else {
                        pic.set(c, r, mPicture.get(c, r));
                    }
                } else foundCrack = true;
            }
        }
        mPicture = pic;
        energyMatrix = getEnergyMatrixFrom(pic);
    }

    private void verifyPictureDimension(int dimension) {
        if (dimension <= 1) throw new IllegalArgumentException();
    }

    private void verifyObject(Object object) {
        if (object == null) throw new IllegalArgumentException();
    }

    private void verifyEachHorizontal(int[] seam) {
        for (int s : seam) {
            if (s < 0 || s > mPicture.width()) throw new IllegalArgumentException();
        }
    }

    private void verifyEachVertical(int[] seam) {
        for (int s : seam) {
            if (s < 0 || s > mPicture.height()) throw new IllegalArgumentException();
        }
    }

    private void verifyVerticalSeam(int[] seam) {
        if (seam.length < mPicture.height() || seam.length > mPicture.height()) throw new IllegalArgumentException();
        verifyEachVertical(seam);
    }

    private void verifyHorizontalSeam(int[] seam) {
        if (seam.length < mPicture.width() || seam.length > mPicture.width()) throw new IllegalArgumentException();
        verifyEachHorizontal(seam);
    }

    private void verifyColumn(int x) {
        if (x < 0 || x > mPicture.width()) throw new IllegalArgumentException();
    }

    private void verifyRow(int y) {
        if (y < 0 || y > mPicture.height()) throw new IllegalArgumentException();
    }

    private static class Pixel {

        private final ArrayList<Pixel> verticalEdges = new ArrayList<>();
        private final ArrayList<Pixel> horizontalEdges = new ArrayList<>();

        private final int position;

        private Pixel(int p) { position = p; }

        private boolean visited = false;

        double energy;

        public void addVerticalEdgeTo(Pixel q) {
            for (Pixel e : verticalEdges) if (e == q) verticalEdges.remove(q);
            verticalEdges.add(q);
        }

        public void addHorizontalEdgeTo(Pixel q) {
            for (Pixel e : horizontalEdges) if (e == q) horizontalEdges.remove(q);
            horizontalEdges.add(q);
        }

        private void removeSmallestEdge(boolean isVertical) {
            if (isVertical) {
                Pixel smallest = null;
                for (Pixel e : verticalEdges) {
                    if (smallest == null) smallest = e;
                    else {
                        if (e.energy < smallest.energy) smallest = e;
                    }
                }
                if (!verticalEdges.isEmpty()) verticalEdges.remove(smallest);
            } else {
                Pixel smallest = null;
                for (Pixel e : horizontalEdges) {
                    if (smallest == null) smallest = e;
                    else {
                        if (e.energy < smallest.energy) smallest = e;
                    }
                }
                if (!horizontalEdges.isEmpty()) horizontalEdges.remove(smallest);
            }
        }
    }

    // unit testing (optional)
    public static void main(String[] args) {

        int width = 5;
        int height = 5;

        Picture p = new Picture(width, height);

        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                int r = StdRandom.uniform(0, 256);
                int g = StdRandom.uniform(0, 256);
                int b = StdRandom.uniform(0, 256);
                p.set(y, x, new Color(r, g, b));
            }
        }

        SeamCarver carver = new SeamCarver(p);

        for (int r = 0; r < p.height(); ++r) {
            for (int c = 0; c < p.width(); ++c) {
                StdOut.print("[" + carver.energyMatrix[r][c].energy + "]");
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

        StdOut.println();

        //carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();

        //carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();

        StdOut.println("Removed vertical seam: ");
        for (Pixel[] r : carver.energyMatrix) {
            for (Pixel s : r) StdOut.print("[" + s.energy + "]");
            StdOut.println();
        }
    }
}
