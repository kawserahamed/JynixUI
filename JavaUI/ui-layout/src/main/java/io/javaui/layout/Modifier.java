package io.javaui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable modifier chain for styling, sizing, constraints, and semantics.
 */
public final class Modifier {

    public static final Modifier DEFAULT = new Modifier(Collections.emptyList());

    public sealed interface Element permits
            PaddingElement,
            SizeElement,
            FillElement,
            BackgroundElement,
            ClickableElement,
            ClipElement,
            AlphaElement,
            SemanticsElement {}

    public record PaddingElement(float startDp, float topDp, float endDp, float bottomDp) implements Element {}
    public record SizeElement(float widthDp, float heightDp) implements Element {}
    public record FillElement(boolean fillWidth, boolean fillHeight, float fraction) implements Element {}
    public record BackgroundElement(int color, float cornerRadiusDp) implements Element {}
    public record ClickableElement(Runnable onClick, String contentDescription) implements Element {}
    public record ClipElement(float cornerRadiusDp) implements Element {}
    public record AlphaElement(float alpha) implements Element {}
    public record SemanticsElement(String role, String contentDescription) implements Element {}

    private final List<Element> elements;

    private Modifier(List<Element> elements) {
        this.elements = elements;
    }

    public List<Element> getElements() {
        return elements;
    }

    public Modifier then(Element element) {
        List<Element> newList = new ArrayList<>(this.elements.size() + 1);
        newList.addAll(this.elements);
        newList.add(element);
        return new Modifier(Collections.unmodifiableList(newList));
    }

    public static Modifier padding(float allDp) {
        return DEFAULT.then(new PaddingElement(allDp, allDp, allDp, allDp));
    }

    public static Modifier padding(float horizontalDp, float verticalDp) {
        return DEFAULT.then(new PaddingElement(horizontalDp, verticalDp, horizontalDp, verticalDp));
    }

    public static Modifier padding(float startDp, float topDp, float endDp, float bottomDp) {
        return DEFAULT.then(new PaddingElement(startDp, topDp, endDp, bottomDp));
    }

    public Modifier paddingThen(float allDp) {
        return then(new PaddingElement(allDp, allDp, allDp, allDp));
    }

    public static Modifier fillMaxWidth() {
        return DEFAULT.then(new FillElement(true, false, 1.0f));
    }

    public static Modifier fillMaxHeight() {
        return DEFAULT.then(new FillElement(false, true, 1.0f));
    }

    public static Modifier fillMaxSize() {
        return DEFAULT.then(new FillElement(true, true, 1.0f));
    }

    public Modifier fillMaxWidthThen() {
        return then(new FillElement(true, false, 1.0f));
    }

    public Modifier fillMaxSizeThen() {
        return then(new FillElement(true, true, 1.0f));
    }

    public static Modifier size(float widthDp, float heightDp) {
        return DEFAULT.then(new SizeElement(widthDp, heightDp));
    }

    public static Modifier size(float sizeDp) {
        return DEFAULT.then(new SizeElement(sizeDp, sizeDp));
    }

    public Modifier sizeThen(float sizeDp) {
        return then(new SizeElement(sizeDp, sizeDp));
    }

    public static Modifier background(int color) {
        return DEFAULT.then(new BackgroundElement(color, 0f));
    }

    public static Modifier background(int color, float cornerRadiusDp) {
        return DEFAULT.then(new BackgroundElement(color, cornerRadiusDp));
    }

    public Modifier backgroundThen(int color) {
        return then(new BackgroundElement(color, 0f));
    }

    public Modifier backgroundThen(int color, float cornerRadiusDp) {
        return then(new BackgroundElement(color, cornerRadiusDp));
    }

    public static Modifier clickable(Runnable onClick) {
        return DEFAULT.then(new ClickableElement(onClick, null));
    }

    public Modifier clickableThen(Runnable onClick) {
        return then(new ClickableElement(onClick, null));
    }

    public static Modifier alpha(float alpha) {
        return DEFAULT.then(new AlphaElement(alpha));
    }

    public Modifier alphaThen(float alpha) {
        return then(new AlphaElement(alpha));
    }

    public static Modifier semantics(String role, String contentDescription) {
        return DEFAULT.then(new SemanticsElement(role, contentDescription));
    }
}
