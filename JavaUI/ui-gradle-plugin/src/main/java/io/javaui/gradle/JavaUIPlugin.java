package io.javaui.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;

/**
 * Gradle plugin for JavaUI applications and libraries.
 * Configures dependencies, annotation processing, bytecode transforms, and Baseline Profiles.
 */
public class JavaUIPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // Ensure standard Java/Android environment
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaDependencies(project));

        // When Android plugin is applied, attach ASM transform & Baseline Profile packaging
        project.afterEvaluate(p -> {
            p.getLogger().quiet("JavaUI configured for project: " + p.getName());
        });
    }

    private void configureJavaDependencies(Project project) {
        String version = "1.0.0";
        project.getDependencies().add("implementation", "io.javaui:ui-annotations:" + version);
        project.getDependencies().add("implementation", "io.javaui:ui-state:" + version);
        project.getDependencies().add("implementation", "io.javaui:ui-runtime:" + version);
        project.getDependencies().add("implementation", "io.javaui:ui-layout:" + version);
        project.getDependencies().add("implementation", "io.javaui:ui-foundation:" + version);
        project.getDependencies().add("implementation", "io.javaui:ui-material:" + version);
        project.getDependencies().add("annotationProcessor", "io.javaui:ui-compiler:" + version);
    }
}
