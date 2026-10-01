package io.javaui.layout;

import java.util.List;

/**
 * Horizontal linear layout policy.
 */
public final class RowPolicy implements LayoutPolicy {

    public static final RowPolicy INSTANCE = new RowPolicy();

    @Override
    public LayoutResult measure(LayoutContext context, List<LayoutNode> children, Constraints constraints) {
        int totalWidth = 0;
        int maxHeight = 0;

        Constraints childConstraints = new Constraints(0, Constraints.INFINITY, 0, constraints.maxHeight());

        for (LayoutNode child : children) {
            child.measure(childConstraints, context);
            child.place(totalWidth, 0);
            totalWidth += child.getWidth();
            maxHeight = Math.max(maxHeight, child.getHeight());
        }

        int finalWidth = constraints.constrainWidth(totalWidth);
        int finalHeight = constraints.constrainHeight(maxHeight);
        return new LayoutResult(finalWidth, finalHeight);
    }
}
