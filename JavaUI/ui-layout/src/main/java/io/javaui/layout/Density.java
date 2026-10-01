package io.javaui.layout;

/**
 * Screen density context for converting dp (density-independent pixels) and sp to raw pixels.
 */
public interface Density {

    float getDensity();
    float getFontScale();

    default int dpToPx(float dp) {
        return Math.round(dp * getDensity());
    }

    default float pxToDp(int px) {
        return px / getDensity();
    }

    default int spToPx(float sp) {
        return Math.round(sp * getFontScale() * getDensity());
    }

    static Density of(float density, float fontScale) {
        return new Density() {
            @Override
            public float getDensity() {
                return density;
            }

            @Override
            public float getFontScale() {
                return fontScale;
            }
        };
    }

    static Density standard() {
        return of(2.0f, 1.0f);
    }
}
