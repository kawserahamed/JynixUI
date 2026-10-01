package io.javaui.layout;

/**
 * Alignment in 2D space (-1.0 to 1.0 or pixel offsets).
 */
public record Alignment(float horizontal, float vertical) {

    public static final Alignment TOP_START = new Alignment(-1.0f, -1.0f);
    public static final Alignment TOP_CENTER = new Alignment(0.0f, -1.0f);
    public static final Alignment TOP_END = new Alignment(1.0f, -1.0f);

    public static final Alignment CENTER_START = new Alignment(-1.0f, 0.0f);
    public static final Alignment CENTER = new Alignment(0.0f, 0.0f);
    public static final Alignment CENTER_END = new Alignment(1.0f, 0.0f);

    public static final Alignment BOTTOM_START = new Alignment(-1.0f, 1.0f);
    public static final Alignment BOTTOM_CENTER = new Alignment(0.0f, 1.0f);
    public static final Alignment BOTTOM_END = new Alignment(1.0f, 1.0f);

    public int alignHorizontal(int size, int space) {
        float center = (space - size) / 2.0f;
        return Math.round(center * (1.0f + horizontal));
    }

    public int alignVertical(int size, int space) {
        float center = (space - size) / 2.0f;
        return Math.round(center * (1.0f + vertical));
    }
}
