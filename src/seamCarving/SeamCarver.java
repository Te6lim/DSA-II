package seamCarving;

import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class SeamCarver {

    private final Picture mPicture;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        validateObject(picture);
        mPicture = buildPicture(picture);
    }

    private Picture buildPicture(Picture picture) {
        Picture p = new Picture(picture.width(), picture.height());
        for (int x = 0; x < picture.width(); ++x) {
            for (int y = 0; y < picture.height(); ++y) {
                p.set(x, y, picture.get(x, y));
            }
        }
        return p;
    }

    // current picture
    public Picture picture() {
        return null;
    }

    // width of current picture
    public int width() {
        return 0;
    }

    // height of current picture
    public int height() {
        return 0;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        verifyXRange(x);
        verifyYRange(y);
        return 0f;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return null;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return null;
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

    private void verifyXRange(int x) {
        if (x < 0 || x > mPicture.width() - 1) throw new IllegalArgumentException();
    }

    private void verifyYRange(int y) {
        if (y < 0 || y > mPicture.height() - 1) throw new IllegalArgumentException();
    }

    private void verifyVerticalSeam(int[] seam) {
        if (seam.length < mPicture.height()) throw new IllegalArgumentException();
        Arrays.sort(seam);
        verifyYRange(seam[0]);
        verifyYRange(seam[seam.length - 1]);
        if (hasBreakage(seam)) throw new IllegalArgumentException();
    }

    private void verifyHorizontalSeam(int[] seam) {
        if (seam.length < picture().width()) throw new IllegalArgumentException();
        Arrays.sort(seam);
        verifyXRange(seam[0]);
        verifyXRange(seam[seam.length - 1]);
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

    // unit testing (optional)
    public static void main(String[] args) {

    }
}
