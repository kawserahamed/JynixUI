package io.javaui.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeProvider;

import io.javaui.layout.Constraints;
import io.javaui.layout.Density;
import io.javaui.layout.LayoutContext;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.Batch;
import io.javaui.state.BatchScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Root host View hosting the entire declarative JavaUI tree.
 * Uses public Android API 29+ RenderNode for isolated subtree recording.
 */
public class JavaUIHostView extends View implements BatchScheduler {

    private final Density density;
    private final LayoutContext layoutContext;
    private final Map<UINode, RenderNode> renderNodeMap = new HashMap<>(64);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Scope rootScope;
    private UINode rootNode;
    private Function<Scope, UI> rootComponent;
    private AccessibilityNodeProvider accessibilityProvider;
    private boolean frameScheduled = false;

    public JavaUIHostView(Context context) {
        this(context, null);
    }

    public JavaUIHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        float f = getResources().getConfiguration().fontScale;
        this.density = Density.of(d, f);
        this.layoutContext = new LayoutContext(this.density);

        Batch.setScheduler(this);
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setContent(Function<Scope, UI> component) {
        this.rootComponent = component;
        rebuildTree();
    }

    private void rebuildTree() {
        if (rootScope != null) {
            rootScope.dispose();
        }
        if (rootNode != null) {
            rootNode.dispose();
            destroyRenderNodes();
        }

        rootScope = new Scope("RootHost", this::requestFrame);
        rootScope.startPass();
        UI ui = rootComponent.apply(rootScope);
        rootNode = ui.materialize(rootScope);

        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int minW = (widthMode == MeasureSpec.EXACTLY) ? widthSize : 0;
        int maxW = (widthMode == MeasureSpec.UNSPECIFIED) ? Constraints.INFINITY : widthSize;
        int minH = (heightMode == MeasureSpec.EXACTLY) ? heightSize : 0;
        int maxH = (heightMode == MeasureSpec.UNSPECIFIED) ? Constraints.INFINITY : heightSize;

        Constraints constraints = new Constraints(minW, maxW, minH, maxH);

        if (rootNode != null) {
            rootNode.getLayoutNode().measure(constraints, layoutContext);
            setMeasuredDimension(rootNode.getLayoutNode().getWidth(), rootNode.getLayoutNode().getHeight());
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (rootNode != null) {
            rootNode.getLayoutNode().place(0, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rootNode == null) return;

        // Traverse tree and draw isolated RenderNodes
        drawNodeHierarchy(canvas, rootNode, 0, 0);
    }

    private void drawNodeHierarchy(Canvas canvas, UINode node, int parentX, int parentY) {
        int absX = parentX + node.getLayoutNode().getX();
        int absY = parentY + node.getLayoutNode().getY();
        int w = node.getLayoutNode().getWidth();
        int h = node.getLayoutNode().getHeight();

        if (w <= 0 || h <= 0) return;

        RenderNode renderNode = renderNodeMap.computeIfAbsent(node, n -> new RenderNode(n.getType()));
        renderNode.setPosition(absX, absY, absX + w, absY + h);

        if (node.isDrawDirty()) {
            recordNodeDrawing(node, renderNode, w, h);
            node.clearDrawDirty();
        }

        if (renderNode.hasDisplayList()) {
            canvas.drawRenderNode(renderNode);
        }

        // Draw children
        for (UINode child : node.getChildren()) {
            drawNodeHierarchy(canvas, child, absX, absY);
        }
    }

    private void recordNodeDrawing(UINode node, RenderNode renderNode, int width, int height) {
        Canvas recordingCanvas = renderNode.beginRecording(width, height);
        try {
            // Draw background modifier if present
            for (Modifier.Element elem : node.getLayoutNode().getModifier().getElements()) {
                if (elem instanceof Modifier.BackgroundElement bg) {
                    paint.setColor(bg.color());
                    if (bg.cornerRadiusDp() > 0) {
                        float radiusPx = density.dpToPx(bg.cornerRadiusDp());
                        recordingCanvas.drawRoundRect(0, 0, width, height, radiusPx, radiusPx, paint);
                    } else {
                        recordingCanvas.drawRect(0, 0, width, height, paint);
                    }
                }
            }

            // Draw text content if present
            if (!node.getTextContent().isEmpty()) {
                paint.setColor(0xFF1E1E1E);
                paint.setTextSize(density.spToPx(16));
                recordingCanvas.drawText(node.getTextContent(), density.dpToPx(8), height / 2f + density.spToPx(6), paint);
            }
        } finally {
            renderNode.endRecording();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && rootNode != null) {
            float x = event.getX();
            float y = event.getY();
            UINode clicked = hitTest(rootNode, 0, 0, (int) x, (int) y);
            if (clicked != null && clicked.getClickListener() != null) {
                clicked.getClickListener().run();
                return true;
            }
        }
        return true;
    }

    private UINode hitTest(UINode node, int pX, int pY, int targetX, int targetY) {
        int absX = pX + node.getLayoutNode().getX();
        int absY = pY + node.getLayoutNode().getY();
        int w = node.getLayoutNode().getWidth();
        int h = node.getLayoutNode().getHeight();

        if (targetX >= absX && targetX <= absX + w && targetY >= absY && targetY <= absY + h) {
            // Check children back to front
            for (int i = node.getChildren().size() - 1; i >= 0; i--) {
                UINode hit = hitTest(node.getChildren().get(i), absX, absY, targetX, targetY);
                if (hit != null) return hit;
            }
            if (node.getClickListener() != null) {
                return node;
            }
        }
        return null;
    }

    public void setAccessibilityProvider(AccessibilityNodeProvider provider) {
        this.accessibilityProvider = provider;
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return accessibilityProvider != null ? accessibilityProvider : super.getAccessibilityNodeProvider();
    }

    private void destroyRenderNodes() {
        for (RenderNode rn : renderNodeMap.values()) {
            rn.discardDisplayList();
        }
        renderNodeMap.clear();
    }

    private void requestFrame() {
        if (!frameScheduled) {
            frameScheduled = true;
            scheduleFrame(() -> {
                frameScheduled = false;
                if (rootNode != null && rootNode.getLayoutNode().isLayoutDirty()) {
                    requestLayout();
                }
                invalidate();
            });
        }
    }

    @Override
    public void scheduleFrame(Runnable task) {
        Choreographer.getInstance().postFrameCallback(frameTimeNanos -> task.run());
    }

    @Override
    public boolean isMainThread() {
        return android.os.Looper.myLooper() == android.os.Looper.getMainLooper();
    }

    public UINode getRootNode() {
        return rootNode;
    }
}
