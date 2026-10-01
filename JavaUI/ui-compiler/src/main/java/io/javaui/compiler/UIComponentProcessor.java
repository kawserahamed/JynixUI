package io.javaui.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.javaui.annotation.UIComponent;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Annotation processor scanning @UIComponent declarations and generating companion
 * metadata classes with pre-computed stable integer hash keys.
 */
@SupportedAnnotationTypes("io.javaui.annotation.UIComponent")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class UIComponentProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(UIComponent.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                generateMetadataForMethod((ExecutableElement) element);
            }
        }
        return true;
    }

    private void generateMetadataForMethod(ExecutableElement method) {
        TypeElement enclosingClass = (TypeElement) method.getEnclosingElement();
        String packageName = processingEnv.getElementUtils().getPackageOf(enclosingClass).getQualifiedName().toString();
        String methodName = method.getSimpleName().toString();
        String metadataClassName = enclosingClass.getSimpleName() + "_" + methodName + "_Metadata";

        int stableKey = (enclosingClass.getQualifiedName() + "#" + methodName).hashCode();

        TypeSpec metadataType = TypeSpec.classBuilder(metadataClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec.builder(int.class, "STABLE_KEY", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$L", stableKey)
                        .build())
                .addField(FieldSpec.builder(String.class, "COMPONENT_NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", methodName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getKey")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(int.class)
                        .addStatement("return STABLE_KEY")
                        .build())
                .build();

        try {
            JavaFile.builder(packageName, metadataType)
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write metadata: " + e.getMessage());
        }
    }
}
