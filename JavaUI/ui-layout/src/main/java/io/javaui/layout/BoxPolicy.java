package io.javaui.layout;

import java.util.List;

/**
 * Box layout policy (stacks children with alignment).
 */
public final class BoxPolicy implements LayoutPolicy {

    private final Alignment alignment;

    public BoxPolicy(Alignment alignment) {
        this.alignment = alignment != null ? alignment : Alignment.CENTER;
    }

    public static final BoxPolicy DEFAULT = new BoxPolicy(Alignment.TOP_START);

    @Override
    public LayoutResult measure(LayoutContext context, List<LayoutNode> children, Constraints constraints) {
        int maxWidth = 0;
        int maxHeight = 0;

        Constraints childConstraints = new Constraints(0, constraints.maxWidth(), 0, constraints.maxHeight());

        for (LayoutNode child : children) {
            child.measure(childConstraints, context);
            maxWidth = Math.max(maxWidth, child.getWidth());
            maxHeight = Math.max(maxHeight, child.getHeight());
        }

        int finalWidth = constraints.constrainWidth(maxWidth);
        int finalHeight = constraints.constrainHeight(maxHeight);

        for (LayoutNode child : children) {
            int cx = alignment.alignHorizontal(child.getWidth(), finalWidth);
            int cy = alignment.alignVertical(child.getHeight(), finalHeight);
            child.place(cx, cy);
        }

        return new LayoutResult(finalWidth, finalHeight);
    }
}
