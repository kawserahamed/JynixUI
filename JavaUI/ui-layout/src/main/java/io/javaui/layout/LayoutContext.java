package io.javaui.layout;

/**
 * Context provided during measure pass with density calculations and allocation-free pools.
 */
public final class LayoutContext {

    private final Density density;

    public LayoutContext(Density density) {
        this.density = density;
    }

    public Density getDensity() {
        return density;
    }

    public int dpToPx(float dp) {
        return density.dpToPx(dp);
    }
}
