package io.javaui.layout;

import java.util.List;

/**
 * Vertical linear layout policy.
 */
public final class ColumnPolicy implements LayoutPolicy {

    public static final ColumnPolicy INSTANCE = new ColumnPolicy();

    @Override
    public LayoutResult measure(LayoutContext context, List<LayoutNode> children, Constraints constraints) {
        int totalHeight = 0;
        int maxWidth = 0;

        Constraints childConstraints = new Constraints(0, constraints.maxWidth(), 0, Constraints.INFINITY);

        for (LayoutNode child : children) {
            child.measure(childConstraints, context);
            child.place(0, totalHeight);
            totalHeight += child.getHeight();
            maxWidth = Math.max(maxWidth, child.getWidth());
        }

        int finalWidth = constraints.constrainWidth(maxWidth);
        int finalHeight = constraints.constrainHeight(totalHeight);
        return new LayoutResult(finalWidth, finalHeight);
    }
}
