package io.javaui.renderer;

import android.text.PrecomputedText;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache for StaticLayout and PrecomputedText parameters to eliminate layout churn on text measurements.
 */
public final class TextLayoutCache {

    private static final int MAX_CACHE_SIZE = 256;

    private final LinkedHashMap<CacheKey, StaticLayout> layoutCache =
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, StaticLayout> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };

    public record CacheKey(CharSequence text, int width, float textSize, int color) {}

    public synchronized StaticLayout get(CacheKey key) {
        return layoutCache.get(key);
    }

    public synchronized void put(CacheKey key, StaticLayout layout) {
        layoutCache.put(key, layout);
    }

    public synchronized void clear() {
        layoutCache.clear();
    }
}
