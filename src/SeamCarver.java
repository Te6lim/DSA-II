import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

import java.awt.Color;


public class SeamCarver {

    private Picture mPicture;
    private Pixel[][] pixelGraph;

    private int mWidth;

    private int[] verticalSeam = new int[0];
    private int[] horizontalSeam = new int[0];

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        verifyObject(picture);
        mPicture = new Picture(picture);
        mWidth = mPicture.width();
        pixelGraph = getEnergyMatrixFrom(picture);
    }

    private Pixel[][] getEnergyMatrixFrom(Picture pic) {
        Pixel[][] matrix = new Pixel[pic.height()][pic.width()];

        Pixel pixel;

        for (int v = 0; v < pic.height() * pic.width(); ++v) {
            Color color = pic.get(col(v), row(v));
            pixel = matrix[row(v)][col(v)] = new Pixel(v);
            pixel.red = color.getRed();
            pixel.green = color.getGreen();
            pixel.blue = color.getBlue();
        }

        for (int v = 0; v < pic.height() * pic.width(); ++v) {
            calculateEnergy(matrix[row(v)][col(v)], matrix);
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
        return pixelGraph[y][x].energy;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {

        if (horizontalSeam.length == 0) {
            int[] seamHorizontalCoordinates = new int[0];
            double totalEnergy = -1.0;

            Stack<Integer> seam = new Stack<>();

            for (int s = 0; s < mPicture.height(); s += 2) {
                Pixel source = pixelGraph[row(s * mPicture.width())][col(s * mPicture.width())];

                seam = findSeam(source, seam, false);

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
            horizontalSeam = seamHorizontalCoordinates;
            return seamHorizontalCoordinates;
        } else return horizontalSeam.clone();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        if (verticalSeam.length == 0) {
            int[] seamVerticalCoordinates = new int[0];
            double totalEnergy = -1.0;

            Stack<Integer> seam = new Stack<>();

            for (int s = 0; s < mPicture.width(); s += 2) {

                seam = findSeam(pixelGraph[row(s)][col(s)], seam, true);

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
            verticalSeam = seamVerticalCoordinates;
            return seamVerticalCoordinates;
        } else return verticalSeam.clone();
    }

    private Stack<Integer> findSeam(Pixel source, Stack<Integer> seam, boolean isVertical) {

         Queue<Pixel> queue = new Queue<>();

         queue.enqueue(source);
         source.setDistToSource(source.energy, isVertical);
         source.parent = -1;

         Pixel smallestEnd = null;

         while (!queue.isEmpty()) {
             Pixel v = queue.dequeue();
             if (getVertices(v, mPicture, isVertical).isEmpty()) {
                 if (smallestEnd == null) smallestEnd = v;
                 else if (v.getDistToSource(isVertical) < smallestEnd.getDistToSource(isVertical)) smallestEnd = v;
             } else insertNeighbours(v, queue, isVertical);
         }

        if (smallestEnd != null) {
            seam = new Stack<>();
            for (int x = smallestEnd.position; x != -1; x = pixelGraph[row(x)][col(x)].parent) {
                seam.push(x);
            }
        }
         return seam;
    }

    private void insertNeighbours(Pixel v, Queue<Pixel> pQ, boolean isVertical) {
        Bag<Integer> vertices = getVertices(v, mPicture, isVertical);
        for (int n : vertices) {
            //StdOut.print(n + ", ");
            Pixel nthPixel = pixelGraph[row(n)][col(n)];
            if (nthPixel.getDistToSource(isVertical) > v.getDistToSource(isVertical) + nthPixel.energy) {
                pQ.enqueue(nthPixel);
                nthPixel.parent = v.position;
                nthPixel.setDistToSource(v.getDistToSource(isVertical) + nthPixel.energy, isVertical);
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
                    if (foundCrack) pic.set(c, r - 1, mPicture.get(c, r));
                    else pic.set(c, r, mPicture.get(c, r));
                } else foundCrack = true;
            }
        }

        mPicture = pic;
        pixelGraph = getEnergyMatrixFrom(pic);
        verticalSeam = new int[0];
        horizontalSeam = new int[0];
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
                    if (foundCrack) pic.set(c - 1, r, mPicture.get(c, r));
                    else pic.set(c, r, mPicture.get(c, r));
                } else foundCrack = true;
            }
        }

        mPicture = pic;
        pixelGraph = getEnergyMatrixFrom(pic);
        verticalSeam = new int[0];
        horizontalSeam = new int[0];
    }

    private void calculateEnergy(Pixel pixel, Pixel[][] graph) {
        if (pixel.energy == 0f) {
            if (col(pixel.position) == 0 || col(pixel.position) == graph[0].length - 1
                    || row(pixel.position) == 0 || row(pixel.position) == graph.length - 1) {
                pixel.energy = 1000.0;
                return;
            }
            double deltaXSquared = getDeltaXSquared(pixel, graph);
            double deltaYSquared = getDeltaYSquared(pixel, graph);
            pixel.energy = Math.sqrt(deltaXSquared + deltaYSquared);
        }
    }

    private double getDeltaXSquared(Pixel pixel, Pixel[][] graph) {
        int col = col(pixel.position);
        int row = row(pixel.position);

        int red = graph[row][col + 1].red - graph[row][col - 1].red;
        int green = graph[row][col + 1].green - graph[row][col - 1].green;
        int blue = graph[row][col + 1].blue - graph[row][col - 1].blue;

        return Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2);
    }

    private double getDeltaYSquared(Pixel pixel, Pixel[][] graph) {
        int col = col(pixel.position);
        int row = row(pixel.position);

        int red = graph[row + 1][col].red - graph[row - 1][col].red;
        int green = graph[row + 1][col].green - graph[row - 1][col].green;
        int blue = graph[row + 1][col].blue - graph[row - 1][col].blue;

        return Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2);
    }

    private Bag<Integer> getHorizontalVertices(Pixel pixel, Picture pic) {
        Bag<Integer> vertices = new Bag<>();
        if (col(pixel.position) + 1 < pic.width() && row(pixel.position) + 1 < pic.height()) {
            vertices.add(pixel.position + (pic.width() + 1));
        }
        if (col(pixel.position) + 1 < pic.width()) {
            vertices.add(pixel.position + 1);
        }
        if (row(pixel.position) - 1 >= 0 && col(pixel.position) + 1 < pic.width()) {
            vertices.add((pixel.position - pic.width()) + 1);
        }
        return vertices;
    }

    private Bag<Integer> getVerticalVertices(Pixel pixel, Picture pic) {
        Bag<Integer> vertices = new Bag<>();
        if (col(pixel.position) - 1 >= 0 && row(pixel.position) + 1 < pic.height()) {
            vertices.add(pixel.position + (pic.width() - 1));
        }

        if (row(pixel.position) + 1 < pic.height()) {
            vertices.add(pixel.position + pic.width());
        }
        if (col(pixel.position) + 1 < pic.width() && row(pixel.position) + 1 < pic.height()) {
            vertices.add(pixel.position + pic.width() + 1);
        }
        return vertices;
    }

    private Bag<Integer> getVertices(Pixel pixel, Picture pic, boolean isVertical) {
        if (isVertical) return getVerticalVertices(pixel, pic);
        else return getHorizontalVertices(pixel, pic);
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

        public int position;

        double verticalDistToSource = Double.POSITIVE_INFINITY;
        double horizontalDistToSource = Double.POSITIVE_INFINITY;

        public int red;
        public int green;
        public int blue;

        public double energy;

        public int parent = -1;

        public Pixel(int p) {
            position = p;
        }

        public double getDistToSource(boolean isVertical) {
            if (isVertical) return verticalDistToSource;
            else return horizontalDistToSource;
        }

        public void setDistToSource(double value, boolean isVertical) {
            if (isVertical) verticalDistToSource = value;
            else horizontalDistToSource = value;
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
