package io.javaui.input;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;

import java.util.function.Consumer;

/**
 * High-performance input connection communicating directly with the software keyboard (IME).
 */
public class JavaUIInputConnection extends BaseInputConnection {

    private final SpannableStringBuilder editable;
    private final Consumer<String> textChangeListener;
    private int batchDepth = 0;

    public JavaUIInputConnection(View targetView, String initialText, Consumer<String> textChangeListener) {
        super(targetView, true);
        this.editable = new SpannableStringBuilder(initialText != null ? initialText : "");
        this.textChangeListener = textChangeListener;
        Selection.setSelection(editable, editable.length());
    }

    @Override
    public Editable getEditable() {
        return editable;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        super.commitText(text, newCursorPosition);
        notifyTextChange();
        return true;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        super.deleteSurroundingText(beforeLength, afterLength);
        notifyTextChange();
        return true;
    }

    @Override
    public boolean beginBatchEdit() {
        batchDepth++;
        return super.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        batchDepth--;
        if (batchDepth == 0) {
            notifyTextChange();
        }
        return super.endBatchEdit();
    }

    private void notifyTextChange() {
        if (batchDepth == 0 && textChangeListener != null) {
            textChangeListener.accept(editable.toString());
        }
    }

    public static void configureEditorInfo(EditorInfo outAttrs, String initialText) {
        outAttrs.inputType = EditorInfo.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        outAttrs.initialSelStart = initialText != null ? initialText.length() : 0;
        outAttrs.initialSelEnd = initialText != null ? initialText.length() : 0;
    }
}
