package io.javaui.input;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import io.javaui.runtime.UINode;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes a virtual hierarchy of accessibility nodes to Android's accessibility service (TalkBack).
 */
public class JavaUIAccessibilityNodeProvider extends AccessibilityNodeProvider {

    private final View hostView;
    private final UINode rootNode;
    private final Map<Integer, UINode> idToNodeMap = new IdentityHashMap<>();
    private final Map<UINode, Integer> nodeToIdMap = new IdentityHashMap<>();
    private int nextVirtualId = 1;
    private int focusedVirtualId = View.NO_ID;

    public JavaUIAccessibilityNodeProvider(View hostView, UINode rootNode) {
        this.hostView = hostView;
        this.rootNode = rootNode;
        buildVirtualMap(rootNode);
    }

    private void buildVirtualMap(UINode node) {
        int id = nextVirtualId++;
        idToNodeMap.put(id, node);
        nodeToIdMap.put(node, id);
        for (UINode child : node.getChildren()) {
            buildVirtualMap(child);
        }
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
        if (virtualViewId == View.NO_ID) {
            // Root host node
            AccessibilityNodeInfo rootInfo = AccessibilityNodeInfo.obtain(hostView);
            hostView.onInitializeAccessibilityNodeInfo(rootInfo);
            for (UINode child : rootNode.getChildren()) {
                Integer childId = nodeToIdMap.get(child);
                if (childId != null) {
                    rootInfo.addChild(hostView, childId);
                }
            }
            return rootInfo;
        }

        UINode node = idToNodeMap.get(virtualViewId);
        if (node == null) return null;

        AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(hostView, virtualViewId);
        info.setPackageName(hostView.getContext().getPackageName());
        info.setClassName(node.getAccessibilityRole() != null ? node.getAccessibilityRole() : "android.view.View");
        info.setSource(hostView, virtualViewId);

        String desc = node.getContentDescription();
        if (desc == null && !node.getTextContent().isEmpty()) {
            desc = node.getTextContent();
        }
        info.setContentDescription(desc);
        info.setText(node.getTextContent());

        info.setVisibleToUser(true);
        info.setEnabled(true);
        info.setClickable(node.getClickListener() != null);
        info.setFocusable(true);

        if (focusedVirtualId == virtualViewId) {
            info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        } else {
            info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        }

        if (node.getClickListener() != null) {
            info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        // Compute screen bounds
        Rect bounds = computeNodeBounds(node);
        info.setBoundsInParent(bounds);

        int[] locationOnScreen = new int[2];
        hostView.getLocationOnScreen(locationOnScreen);
        bounds.offset(locationOnScreen[0], locationOnScreen[1]);
        info.setBoundsInScreen(bounds);

        // Add children
        for (UINode child : node.getChildren()) {
            Integer cId = nodeToIdMap.get(child);
            if (cId != null) {
                info.addChild(hostView, cId);
            }
        }

        return info;
    }

    private Rect computeNodeBounds(UINode target) {
        int x = 0, y = 0;
        UINode curr = target;
        while (curr != null) {
            x += curr.getLayoutNode().getX();
            y += curr.getLayoutNode().getY();
            curr = curr.getParent();
        }
        return new Rect(x, y, x + target.getLayoutNode().getWidth(), y + target.getLayoutNode().getHeight());
    }

    @Override
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        if (virtualViewId == View.NO_ID) {
            return hostView.performAccessibilityAction(action, arguments);
        }

        UINode node = idToNodeMap.get(virtualViewId);
        if (node == null) return false;

        if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) {
            focusedVirtualId = virtualViewId;
            sendAccessibilityEvent(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
            return true;
        } else if (action == AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
            if (focusedVirtualId == virtualViewId) {
                focusedVirtualId = View.NO_ID;
                sendAccessibilityEvent(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                return true;
            }
        } else if (action == AccessibilityNodeInfo.ACTION_CLICK) {
            if (node.getClickListener() != null) {
                node.getClickListener().run();
                sendAccessibilityEvent(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                return true;
            }
        }
        return false;
    }

    private void sendAccessibilityEvent(int virtualViewId, int eventType) {
        if (hostView.getParent() == null) return;
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.setPackageName(hostView.getContext().getPackageName());
        event.setSource(hostView, virtualViewId);
        hostView.getParent().requestSendAccessibilityEvent(hostView, event);
    }
}
