package io.javaui.foundation;

import io.javaui.layout.Constraints;
import io.javaui.layout.Density;
import io.javaui.layout.LayoutContext;
import io.javaui.layout.LayoutNode;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Virtualized vertical list.
 * Recycles nodes based on viewport bounds. 10,000 items allocate only ~20-30 active nodes in the tree.
 */
public final class LazyColumn {

    public interface ItemProvider<T> {
        int getCount();
        T getItem(int index);
        Object getKey(int index);
        int getContentType(int index);
    }

    public static <T> UI LazyColumn(
            Modifier modifier,
            int itemCount,
            Function<Integer, Object> keyProvider,
            BiFunction<Scope, Integer, UI> itemFactory
    ) {
        return scope -> {
            UINode container = new UINode("LazyColumn", null);
            container.setModifier(modifier);

            // Virtualized manager
            VirtualizedListManager manager = new VirtualizedListManager(itemCount, itemFactory, scope, container);
            container.setLayoutPolicy((context, children, constraints) -> manager.measureAndLayout(context, constraints));

            return container;
        };
    }

    public static class VirtualizedListManager {
        private final int itemCount;
        private final BiFunction<Scope, Integer, UI> itemFactory;
        private final Scope parentScope;
        private final UINode container;

        // Viewport scroll offset in pixels
        private int scrollOffsetY = 0;
        private final int estimatedItemHeightPx = 120; // 60dp default estimate

        // Recycling pools indexed by contentType
        private final Map<Integer, List<UINode>> recyclePool = new HashMap<>();
        private final Map<Integer, UINode> activeItemNodes = new HashMap<>();

        public VirtualizedListManager(int itemCount, BiFunction<Scope, Integer, UI> itemFactory, Scope parentScope, UINode container) {
            this.itemCount = itemCount;
            this.itemFactory = itemFactory;
            this.parentScope = parentScope;
            this.container = container;
        }

        public io.javaui.layout.LayoutPolicy.LayoutResult measureAndLayout(LayoutContext context, Constraints constraints) {
            int viewportHeight = constraints.hasBoundedHeight() ? constraints.maxHeight() : 1200;
            int totalListHeight = itemCount * estimatedItemHeightPx;

            int firstVisibleIndex = Math.max(0, scrollOffsetY / estimatedItemHeightPx);
            int lastVisibleIndex = Math.min(itemCount - 1, (scrollOffsetY + viewportHeight) / estimatedItemHeightPx + 1);

            // Detach and recycle nodes that fell out of viewport
            List<Integer> outOfViewIndices = new ArrayList<>();
            for (Map.Entry<Integer, UINode> entry : activeItemNodes.entrySet()) {
                int index = entry.getKey();
                if (index < firstVisibleIndex || index > lastVisibleIndex) {
                    outOfViewIndices.add(index);
                    recycleNode(entry.getValue());
                    container.removeChild(entry.getValue());
                }
            }
            for (int idx : outOfViewIndices) {
                activeItemNodes.remove(idx);
            }

            // Materialize or reuse nodes for visible window
            for (int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
                if (!activeItemNodes.containsKey(i)) {
                    UINode node = obtainNode(i);
                    activeItemNodes.put(i, node);
                    container.addChild(node);
                }
                UINode activeNode = activeItemNodes.get(i);
                int itemTop = (i * estimatedItemHeightPx) - scrollOffsetY;
                activeNode.getLayoutNode().measure(Constraints.loose(constraints.maxWidth(), Constraints.INFINITY), context);
                activeNode.getLayoutNode().place(0, itemTop);
            }

            int finalWidth = constraints.constrainWidth(constraints.maxWidth());
            int finalHeight = constraints.constrainHeight(viewportHeight);
            return new io.javaui.layout.LayoutPolicy.LayoutResult(finalWidth, finalHeight);
        }

        private UINode obtainNode(int index) {
            List<UINode> pool = recyclePool.computeIfAbsent(0, k -> new ArrayList<>());
            if (!pool.isEmpty()) {
                return pool.remove(pool.size() - 1);
            }
            // Create new node instance for this slot
            UI itemUI = itemFactory.apply(parentScope, index);
            return itemUI.materialize(parentScope);
        }

        private void recycleNode(UINode node) {
            List<UINode> pool = recyclePool.computeIfAbsent(0, k -> new ArrayList<>());
            pool.add(node);
        }

        public int getActiveNodeCount() {
            return activeItemNodes.size();
        }

        public void setScrollOffsetY(int offset) {
            this.scrollOffsetY = offset;
            container.getLayoutNode().markLayoutDirty();
        }
    }
}
