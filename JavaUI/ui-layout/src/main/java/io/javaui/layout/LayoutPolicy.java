package io.javaui.layout;

import java.util.List;

/**
 * Functional measure and layout policy for container nodes (Column, Row, Box, Stack).
 */
@FunctionalInterface
public interface LayoutPolicy {
    LayoutResult measure(LayoutContext context, List<LayoutNode> children, Constraints constraints);

    record LayoutResult(int width, int height) {}
}
