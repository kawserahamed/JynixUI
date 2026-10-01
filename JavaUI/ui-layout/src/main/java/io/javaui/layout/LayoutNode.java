package io.javaui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure JVM layout node. Computes positions and sizes without Android SDK dependencies.
 */
public class LayoutNode {

    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private boolean layoutDirty = true;
    private Constraints lastConstraints = null;

    private Modifier modifier = Modifier.DEFAULT;
    private final List<LayoutNode> children = new ArrayList<>();
    private LayoutPolicy layoutPolicy;

    public LayoutNode(LayoutPolicy layoutPolicy) {
        this.layoutPolicy = layoutPolicy;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier != null ? modifier : Modifier.DEFAULT;
        markLayoutDirty();
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setLayoutPolicy(LayoutPolicy policy) {
        this.layoutPolicy = policy;
        markLayoutDirty();
    }

    public void addChild(LayoutNode child) {
        children.add(child);
        markLayoutDirty();
    }

    public void removeChild(LayoutNode child) {
        children.remove(child);
        markLayoutDirty();
    }

    public void clearChildren() {
        children.clear();
        markLayoutDirty();
    }

    public List<LayoutNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void markLayoutDirty() {
        this.layoutDirty = true;
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Resolves modifier constraints, executes layout policy, and computes bounds.
     */
    public void measure(Constraints incoming, LayoutContext context) {
        if (!layoutDirty && incoming.equals(lastConstraints)) {
            return; // Cache hit: zero work, zero allocation
        }
        this.lastConstraints = incoming;

        // Apply modifier constraints (size, fill, padding)
        Constraints effective = incoming;
        float padStart = 0, padTop = 0, padEnd = 0, padBottom = 0;

        for (Modifier.Element elem : modifier.getElements()) {
            if (elem instanceof Modifier.SizeElement se) {
                int w = context.dpToPx(se.widthDp());
                int h = context.dpToPx(se.heightDp());
                effective = new Constraints(
                        effective.constrainWidth(w),
                        effective.constrainWidth(w),
                        effective.constrainHeight(h),
                        effective.constrainHeight(h)
                );
            } else if (elem instanceof Modifier.FillElement fe) {
                int minW = fe.fillWidth() && effective.hasBoundedWidth()
                        ? Math.round(effective.maxWidth() * fe.fraction()) : effective.minWidth();
                int minH = fe.fillHeight() && effective.hasBoundedHeight()
                        ? Math.round(effective.maxHeight() * fe.fraction()) : effective.minHeight();
                effective = new Constraints(minW, effective.maxWidth(), minH, effective.maxHeight());
            } else if (elem instanceof Modifier.PaddingElement pe) {
                padStart += pe.startDp();
                padTop += pe.topDp();
                padEnd += pe.endDp();
                padBottom += pe.bottomDp();
            }
        }

        int hPadPx = context.dpToPx(padStart + padEnd);
        int vPadPx = context.dpToPx(padTop + padBottom);

        Constraints innerConstraints = new Constraints(
                Math.max(0, effective.minWidth() - hPadPx),
                effective.hasBoundedWidth() ? Math.max(0, effective.maxWidth() - hPadPx) : Constraints.INFINITY,
                Math.max(0, effective.minHeight() - vPadPx),
                effective.hasBoundedHeight() ? Math.max(0, effective.maxHeight() - vPadPx) : Constraints.INFINITY
        );

        if (layoutPolicy != null) {
            LayoutPolicy.LayoutResult result = layoutPolicy.measure(context, children, innerConstraints);
            this.width = effective.constrainWidth(result.width() + hPadPx);
            this.height = effective.constrainHeight(result.height() + vPadPx);
        } else {
            this.width = effective.minWidth();
            this.height = effective.minHeight();
        }

        this.layoutDirty = false;
    }

    public void place(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
