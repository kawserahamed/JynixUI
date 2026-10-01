package io.javaui.layout;

/**
 * Immutable layout box constraints (minWidth, maxWidth, minHeight, maxHeight in pixels).
 * INFINITY is represented as Integer.MAX_VALUE.
 */
public record Constraints(int minWidth, int maxWidth, int minHeight, int maxHeight) {

    public static final int INFINITY = Integer.MAX_VALUE;

    public Constraints {
        if (minWidth < 0 || minHeight < 0) {
            throw new IllegalArgumentException("Constraints dimensions cannot be negative");
        }
        if (minWidth > maxWidth) {
            throw new IllegalArgumentException("minWidth (" + minWidth + ") > maxWidth (" + maxWidth + ")");
        }
        if (minHeight > maxHeight) {
            throw new IllegalArgumentException("minHeight (" + minHeight + ") > maxHeight (" + maxHeight + ")");
        }
    }

    public static Constraints fixed(int width, int height) {
        return new Constraints(width, width, height, height);
    }

    public static Constraints loose(int maxWidth, int maxHeight) {
        return new Constraints(0, maxWidth, 0, maxHeight);
    }

    public static Constraints unbounded() {
        return new Constraints(0, INFINITY, 0, INFINITY);
    }

    public boolean hasBoundedWidth() {
        return maxWidth != INFINITY;
    }

    public boolean hasBoundedHeight() {
        return maxHeight != INFINITY;
    }

    public boolean isTight() {
        return minWidth == maxWidth && minHeight == maxHeight;
    }

    public int constrainWidth(int width) {
        return Math.max(minWidth, Math.min(maxWidth, width));
    }

    public int constrainHeight(int height) {
        return Math.max(minHeight, Math.min(maxHeight, height));
    }
}
