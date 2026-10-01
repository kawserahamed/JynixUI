package io.javaui.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;
import com.intellij.psi.PsiMethod;
import org.jetbrains.uast.UCallExpression;

import java.util.Collections;
import java.util.List;

/**
 * Lint detector preventing instantiation of raw State instances outside of a UIComponent Scope.
 */
public class StateOutsideScopeDetector extends Detector implements SourceCodeScanner {

    public static final Issue ISSUE = Issue.create(
            "StateOutsideScope",
            "State created without Scope ownership",
            "Creating reactive state directly with new IntState(...) inside a UIComponent circumvents"
                    + " lifecycle memory. Use s.intState(...) instead.",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(StateOutsideScopeDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Override
    public List<String> getApplicableConstructorTypes() {
        return Collections.singletonList("io.javaui.state.IntState");
    }

    @Override
    public void visitConstructor(JavaContext context, UCallExpression node, PsiMethod constructor) {
        // Report issue if constructed directly without Scope wrapper
        context.report(ISSUE, node, context.getLocation(node), "Do not instantiate IntState directly; use s.intState(...)");
    }
}
