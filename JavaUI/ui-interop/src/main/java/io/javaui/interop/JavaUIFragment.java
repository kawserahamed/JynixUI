package io.javaui.interop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.javaui.renderer.JavaUIHostView;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;

import java.util.function.Function;

/**
 * Base Fragment allowing enterprise applications to adopt JavaUI screen-by-screen
 * inside existing Fragment / Navigation architectures.
 */
public abstract class JavaUIFragment extends Fragment {

    private JavaUIHostView hostView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        hostView = new JavaUIHostView(requireContext());
        hostView.setContent(this::createUI);
        return hostView;
    }

    /**
     * Declares the UI content for this Fragment.
     */
    protected abstract UI createUI(Scope scope);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hostView = null;
    }
}
