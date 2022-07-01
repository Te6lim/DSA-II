import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

import java.awt.Color;
import java.util.Arrays;


public class SeamCarver {

    private static final double EDGE_ENERGY = 1000.0;
    private Picture mPicture;

    private int mWidth;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        verifyObject(picture);
        mPicture = new Picture(picture);
        mWidth = mPicture.width();
    }

    private Pixel[][] getEnergyMatrixFrom(Picture pic) {
        Pixel[][] matrix = new Pixel[pic.height()][pic.width()];

        Pixel pixel;
        Color color;

        for (int v = 0; v < pic.height() * pic.width(); ++v) {
            color = pic.get(col(v), row(v));
            pixel = new Pixel(v);
            pixel.setVerticalVertices(pic.width(), pic.height());
            pixel.setHorizontalVertices(pic.width(), pic.height());
            pixel.red = color.getRed();
            pixel.green = color.getGreen();
            pixel.blue = color.getBlue();
            matrix[row(v)][col(v)] = pixel;
        }

        for (int v = 0; v < pic.height() * pic.width(); ++v) {
            matrix[row(v)][col(v)].calculateEnergy(matrix);
        }

        return matrix;
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
        int position = y * mPicture.width() + x;
        return calculateEnergy(position, mPicture);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int[] seamHorizontalCoordinates = new int[0];
        double totalEnergy = -1.0;

        Pixel[][] pixelGraph = getEnergyMatrixFrom(mPicture);

        Stack<Integer> seam = new Stack<>();
        double[] distTo = new double[mPicture.height() * mPicture.width()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);

        for (int s = 0; s < mPicture.height(); s += 2) {
            Pixel source = pixelGraph[row(s * mPicture.width())][col(s * mPicture.width())];

            seam = findSeam(pixelGraph, source, seam, distTo, false);

            double tempTotalEnergy = totalEnergyOf(pixelGraph, seam);

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

        Pixel[][] pixelGraph = getEnergyMatrixFrom(mPicture);

        Stack<Integer> seam = new Stack<>();
        double[] distTo = new double[mPicture.height() * mPicture.width()];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);

        for (int s = 0; s < mPicture.width(); s++) {

            seam = findSeam(pixelGraph, pixelGraph[row(s)][col(s)], seam, distTo, true);

            double tempTotalEnergy = totalEnergyOf(pixelGraph, seam);

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

    private Stack<Integer> findSeam(Pixel[][] pixelGraph, Pixel source, Stack<Integer> seam, double[] distTo, boolean isVertical) {

         Queue<Pixel> queue = new Queue<>();

         queue.enqueue(source);
         distTo[source.position] = source.energy;
         source.parent = -1;

         Pixel smallestEnd = null;

         while (!queue.isEmpty()) {
             Pixel v = queue.dequeue();
             if (v.getVertices(isVertical).isEmpty()) {
                 if (smallestEnd == null) smallestEnd = v;
                 else if (distTo[v.position] < distTo[smallestEnd.position]) smallestEnd = v;
             } else insertNeighbours(pixelGraph, v, queue, distTo, isVertical);
         }

        if (smallestEnd != null) {
            seam = new Stack<>();
            for (int x = smallestEnd.position; x != -1; x = pixelGraph[row(x)][col(x)].parent) {
                seam.push(x);
            }
        }
         return seam;
    }

    private void insertNeighbours(Pixel[][] pixelGraph, Pixel v, Queue<Pixel> pQ, double[] distTo, boolean isVertical) {
        for (int n : v.getVertices(isVertical)) {
            Pixel nthPixel = pixelGraph[row(n)][col(n)];
            if (distTo[n] > distTo[v.position] + nthPixel.energy) {
                pQ.enqueue(nthPixel);
                nthPixel.parent = v.position;
                distTo[n] = distTo[v.position] + nthPixel.energy;
            }
        }
    }

    private double totalEnergyOf(Pixel[][] pixelGraph, Stack<Integer> seam) {
        double total = 0.0;
        for (int v : seam) total += pixelGraph[row(v)][col(v)].energy;
        return total;
    }

    private int[] extractCoordinatesFrom(Stack<Integer> vertices, boolean vertical) {
        int[] coordinates = new int[vertices.size()];
        int x = 0;
        if (vertical) {
            for (int v : vertices) {
                coordinates[x++] = col(v);
            }
        } else {
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
        mWidth = newWidth;

        for (int c = 0; c < mPicture.width(); ++c) {
            boolean foundCrack = false;
            for (int r = 0; r < mPicture.height(); ++r) {
                if (r != seam[c]) {
                    if (foundCrack) {
                        pic.set(c, r - 1, mPicture.get(c, r));
                    }
                    else {
                        pic.set(c, r, mPicture.get(c, r));
                    }
                } else foundCrack = true;
            }
        }

        mPicture = pic;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        verifyObject(seam);
        verifyVerticalSeam(seam);
        verifyPictureDimension(mPicture.width());
        int width = mPicture.width() - 1;
        int height = mPicture.height();

        Picture pic = new Picture(width, height);
        mWidth = width;

        for (int r = 0; r < mPicture.height(); ++r) {
            boolean foundCrack = false;
            for (int c = 0; c < mPicture.width(); ++c) {
                if (c != seam[r]) {
                    if (foundCrack) {
                        pic.set(c - 1, r, mPicture.get(c, r));
                    }
                    else {
                        pic.set(c, r, mPicture.get(c, r));
                    }
                } else foundCrack = true;
            }
        }

        mPicture = pic;
    }

    private double calculateEnergy(int position, Picture pic) {
        if (col(position) == 0 || col(position) == pic.width() - 1
                || row(position) == 0 || row(position) == pic.height() - 1) {
            return EDGE_ENERGY;
        }
        double deltaXSquared = getDeltaXSquared(position, pic);
        double deltaYSquared = getDeltaYSquared(position, pic);
        return Math.sqrt(deltaXSquared + deltaYSquared);
    }

    private double getDeltaXSquared(int position, Picture pic) {
        int col = col(position);
        int row = row(position);

        int red = pic.get(col + 1, row).getRed() - pic.get(col - 1, row).getRed();
        int green = pic.get(col + 1, row).getGreen() - pic.get(col - 1, row).getGreen();
        int blue = pic.get(col + 1, row).getBlue() - pic.get(col - 1, row).getBlue();

        return Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2);
    }

    private double getDeltaYSquared(int position, Picture pic) {
        int col = col(position);
        int row = row(position);

        int red = pic.get(col, row + 1).getRed() - pic.get(col, row - 1).getRed();
        int green = pic.get(col, row + 1).getGreen() - pic.get(col, row - 1).getGreen();
        int blue = pic.get(col, row + 1).getBlue() - pic.get(col, row - 1).getBlue();

        return Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2);
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

    private class Pixel {

        public Bag<Integer> verticalLinks = new Bag<>();
        public Bag<Integer> horizontalLinks = new Bag<>();

        public int position;

        public int red;
        public int green;
        public int blue;

        public double energy;

        public int parent = -1;

        public Pixel(int p) {
            position = p;
        }

        public void setHorizontalVertices(int width, int height) {
            Bag<Integer> vertices = new Bag<>();
            if (col(position) + 1 < width && row(position) + 1 < height) {
                vertices.add(position + (width + 1));
            }
            if (col(position) + 1 < width) {
                vertices.add(position + 1);
            }
            if (row(position) - 1 >= 0 && col(position) + 1 < width) {
                vertices.add((position - width) + 1);
            }

            horizontalLinks = vertices;
        }

        public void setVerticalVertices(int width, int height) {
            Bag<Integer> vertices = new Bag<>();
            if (col(position) - 1 >= 0 && row(position) + 1 < height) {
                vertices.add(position + (width - 1));
            }

            if (row(position) + 1 < height) {
                vertices.add(position + width);
            }
            if (col(position) + 1 < width && row(position) + 1 < height) {
                vertices.add(position + width + 1);
            }
            verticalLinks = vertices;
        }

        public Bag<Integer> getVertices(boolean isVertical) {
            if (isVertical) {
                return verticalLinks;
            }
            else {
                return horizontalLinks;
            }
        }

        public void calculateEnergy(Pixel[][] graph) {
            if (col(position) == 0 || col(position) == graph[0].length - 1
                    || row(position) == 0 || row(position) == graph.length - 1) {
                energy = EDGE_ENERGY;
                return;
            }
            double deltaXSquared = getDeltaXSquared(graph);
            double deltaYSquared = getDeltaYSquared(graph);
            energy = Math.sqrt(deltaXSquared + deltaYSquared);
        }

        private double getDeltaXSquared(Pixel[][] graph) {
            int col = col(position);
            int row = row(position);
            int r = graph[row][col + 1].red - graph[row][col - 1].red;
            int g = graph[row][col + 1].green - graph[row][col - 1].green;
            int b = graph[row][col + 1].blue - graph[row][col - 1].blue;

            return Math.pow(r, 2) + Math.pow(g, 2) + Math.pow(b, 2);
        }

        private double getDeltaYSquared(Pixel[][] graph) {
            int col = col(position);
            int row = row(position);

            int r = graph[row + 1][col].red - graph[row - 1][col].red;
            int g = graph[row + 1][col].green - graph[row - 1][col].green;
            int b = graph[row + 1][col].blue - graph[row - 1][col].blue;

            return Math.pow(r, 2) + Math.pow(g, 2) + Math.pow(b, 2);
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

        /* for (int r = 0; r < p.height(); ++r) {
            for (int c = 0; c < p.width(); ++c) {
                StdOut.print("[" + carver.pixelGraph[r][c].energy + "]");
            }
            StdOut.println();
        }*/

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

        int[] seam = carver.findVerticalSeam();
        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());

        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();

        /* for (int r = 0; r < carver.mPicture.height(); ++r) {
            for (int c = 0; c < carver.mPicture.width(); ++c) {
                StdOut.print("[" + carver.pixelGraph[r][c].energy + "]");
            }
            StdOut.println();
        }*/


        seam = carver.findVerticalSeam();
        StdOut.println("seam size: " + seam.length);
        for (int i = 0; i < seam.length; ++i) {
            if (i + 1 < seam.length) StdOut.print(seam[i] + "->");
            else StdOut.println(seam[i]);
        }
        carver.removeHorizontalSeam(carver.findHorizontalSeam());
        carver.removeVerticalSeam(carver.findVerticalSeam());
        StdOut.println("Width: " + carver.width());
        StdOut.println("Height: " + carver.height());
        StdOut.println();
    }
}
