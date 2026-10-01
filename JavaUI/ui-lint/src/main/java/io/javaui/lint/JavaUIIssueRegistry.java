package io.javaui.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.Vendor;
import com.android.tools.lint.detector.api.CURRENT_API;
import com.android.tools.lint.detector.api.Issue;

import java.util.Collections;
import java.util.List;

public class JavaUIIssueRegistry extends IssueRegistry {

    @Override
    public List<Issue> getIssues() {
        return Collections.singletonList(StateOutsideScopeDetector.ISSUE);
    }

    @Override
    public int getApi() {
        return CURRENT_API;
    }

    @Override
    public Vendor getVendor() {
        return new Vendor("JavaUI Project", "io.javaui", "https://github.com/javaui/javaui");
    }
}
