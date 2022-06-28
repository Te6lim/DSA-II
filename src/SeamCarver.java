import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

import java.awt.Color;
import java.util.Arrays;


public class SeamCarver {

    private enum RGB {
        RED, GREEN, BLUE
    }

    private Picture mPicture;
    private Vertex[][] pixelGraph;

    private int mWidth;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        verifyObject(picture);
        mPicture = new Picture(picture);
        mWidth = mPicture.width();
        pixelGraph = getEnergyMatrixFrom(picture);
    }

    private Vertex[][] getEnergyMatrixFrom(Picture pic) {
        Vertex[][] matrix = new Vertex[pic.height()][pic.width()];

        for (int v = 0; v < pic.height() * pic.width(); ++v) {
            matrix[row(v)][col(v)] = new Vertex(v);
            addVerticalEdges(v, matrix);
            addHorizontalEdges(v, matrix);
        }

        return matrix;
    }

    private void addVerticalEdges(int v, Vertex[][] matrix) {
        int col = col(v);
        int row = row(v);


        if (col - 1 >= 0 && row + 1 < mPicture.height()) {
            int p = v + (mPicture.width() - 1);
            matrix[row][col].verticalVertices.add(p);
        }

        if (row + 1 < mPicture.height()) {
            int p = v + mPicture.width();
            matrix[row][col].verticalVertices.add(p);
        }
        if (col + 1 < mPicture.width() && row + 1 < mPicture.height()) {
            int p = v + mPicture.width() + 1;
            matrix[row][col].verticalVertices.add(p);
        }
    }

    private void addHorizontalEdges(int v, Vertex[][] matrix) {
        int col = col(v);
        int row = row(v);

        if (col + 1 < mPicture.width() && row + 1 < mPicture.height()) {
            int p = v + (mPicture.width() + 1);
            matrix[row][col].horizontalVertices.add(p);
        }
        if (col + 1 < mPicture.width()) {
            int p = v + 1;
            matrix[row][col].horizontalVertices.add(p);
        }
        if (row - 1 >= 0 && col + 1 < mPicture.width()) {
            int p = (v - mPicture.width()) + 1;
            matrix[row][col].horizontalVertices.add(p);
        }
    }

    private int col(int v) {
        return v % mWidth;
    }

    private int row(int v) {
        return v / mWidth;
    }

    // current picture
    public Picture picture() {
        return new Picture(mPicture);
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
        return pixelGraph[y][x].energy;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        double[] distTo = new double[mPicture.width() * mPicture.height()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);

        int[] seamHorizontalCoordinates = new int[0];
        double totalEnergy = -1.0;

        Stack<Integer> seam = new Stack<>();

        for (int s = 0; s < mPicture.height(); ++s) {
            Vertex source = pixelGraph[row(s * mPicture.width())][col(s * mPicture.width())];

            seam = findSeam(source, distTo, seam, false);

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
        double[] distTo = new double[mPicture.width() * mPicture.height()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);

        int[] seamVerticalCoordinates = new int[0];
        double totalEnergy = -1.0;

        Stack<Integer> seam = new Stack<>();

        for (int s = 0; s < mPicture.width(); ++s) {

            seam = findSeam(pixelGraph[row(s)][col(s)], distTo, seam, true);

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

    private Stack<Integer> findSeam(Vertex source, double[] distTo, Stack<Integer> seam, boolean isVertical) {


         Queue<Vertex> queue = new Queue<>();

         queue.enqueue(source);
         distTo[source.position] = source.energy;
         source.parent = null;

         Vertex smallestEnd = null;

         while (!queue.isEmpty()) {
             Vertex v = queue.dequeue();
             if (v.getVertices(isVertical).isEmpty()) {
                 if (smallestEnd == null) smallestEnd = v;
                 else if (distTo[v.position] < distTo[smallestEnd.position]) smallestEnd = v;
             } else insertNeighbours(v, queue, distTo, isVertical);
         }

        if (smallestEnd != null)
            seam = new Stack<>();
         for (Vertex x = smallestEnd; x != null; x = x.parent) {
             seam.push(x.position);
         }
         return seam;
    }

    private void insertNeighbours(Vertex v, Queue<Vertex> pQ, double[] distTo, boolean isVertical) {
        if (isVertical) {
            for (int n : v.verticalVertices) {
                Vertex nthVertex = pixelGraph[row(n)][col(n)];
                if (distTo[n] > distTo[v.position] + nthVertex.energy) {
                    pQ.enqueue(nthVertex);
                    nthVertex.parent = v;
                    distTo[n] = distTo[v.position] + nthVertex.energy;
                }
            }
        } else {
            for (int n : v.horizontalVertices) {
                Vertex nthVertex = pixelGraph[row(n)][col(n)];
                if (distTo[n] > distTo[v.position] + nthVertex.energy) {
                    pQ.enqueue(nthVertex);
                    nthVertex.parent = v;
                    distTo[n] = distTo[v.position] + nthVertex.energy;
                }
            }
        }
    }

    private double totalEnergyOf(Stack<Integer> seam) {
        double total = 0.0;
        for (int v : seam) total += pixelGraph[row(v)][col(v)].energy;
        return total;
    }

    private int[] extractCoordinatesFrom(Stack<Integer> vertices, boolean vertical) {
        int[] coordinates = new int[vertices.size()];
        if (vertical) {
            int x = 0;
            for (int v : vertices) {
                coordinates[x++] = col(v);
            }
        } else {
            int x = 0;
            for (int v : vertices) {
                coordinates[x++] = row(v);
            }
        }
        return coordinates;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        verifyObject(seam);
        verifyHorizontalSeam(seam);
        verifyPictureDimension(mPicture.height());

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
        mWidth = mPicture.width();
        pixelGraph = getEnergyMatrixFrom(pic);
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        verifyObject(seam);
        verifyVerticalSeam(seam);
        verifyPictureDimension(mPicture.width());
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
        this.mWidth = pic.width();
        pixelGraph = getEnergyMatrixFrom(pic);
    }

    private void verifyPictureDimension(int dimension) {
        if (dimension <= 1) throw new IllegalArgumentException();
    }

    private void verifyObject(Object object) {
        if (object == null) throw new IllegalArgumentException();
    }

    private void verifyEachHorizontal(int[] seam) {
        int prev = -1;
        for (int node : seam) {
            if (node < 0 || node >= mPicture.height()) throw new IllegalArgumentException();
            if (prev == -1) prev = node;
            else {
                if (node == prev + 1 || node == prev || node == prev - 1) prev = node;
                else throw new IllegalArgumentException();
            }
        }
    }

    private void verifyEachVertical(int[] seam) {
        int prev = -1;
        for (int node : seam) {
            if (node < 0 || node >= mPicture.width()) throw new IllegalArgumentException();
            if (prev == -1) prev = node;
            else {
                if (node == prev + 1 || node == prev || node == prev - 1) prev = node;
                else throw new IllegalArgumentException();
            }

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
        if (x < 0 || x >= mPicture.width()) throw new IllegalArgumentException();
    }

    private void verifyRow(int y) {
        if (y < 0 || y >= mPicture.height()) throw new IllegalArgumentException();
    }

    private class Vertex {

        public final int position;
        public Bag<Integer> verticalVertices = new Bag<>();
        public Bag<Integer> horizontalVertices = new Bag<>();

        public Vertex parent = null;
        public double energy;

        public Vertex(int p) {
            position = p;
            energy = calculateEnergyOf(p);
        }

        public Bag<Integer> getVertices(boolean isVertical) {
            if (isVertical) return verticalVertices;
            else return horizontalVertices;
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
                StdOut.print("[" + carver.pixelGraph[r][c].energy + "]");
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

        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();

        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();
    }
}
