# Getting Started with JavaUI in Android Studio

Follow this step-by-step guide to create and run JavaUI apps in **Android Studio Hedgehog / Iguana / Jellyfish (2023.x - 2024.x+)** using pure **Java 17**.

---

## 1. Project Prerequisites

- **Android Studio**: Android Studio Hedgehog (2023.1.1) or newer
- **JDK**: Java 17 or Java 21 (Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK: 17+)
- **Android SDK**: `compileSdk 34`, `minSdk 29` (required for public `RenderNode` hardware acceleration)
- **Language**: Java (no Kotlin required)

---

## 2. Gradle Configuration

### Step A: Root `settings.gradle`
Ensure `mavenCentral()` and `google()` are in your plugin management:

```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MyJavaUIApp"
include ':app'
```

---

### Step B: App `app/build.gradle`
Apply the `io.javaui` Gradle plugin:

```groovy
plugins {
    id 'com.android.application'
    id 'io.javaui' // Automatically configures JavaUI dependencies & annotation processors
}

android {
    namespace 'com.example.myjavauiapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.myjavauiapp"
        minSdk 29      // Required for hardware RenderNode display lists
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

> **Manual Dependency Alternative (without the plugin):**
> If you prefer explicit Maven coordinates:
> ```groovy
> dependencies {
>     implementation 'io.javaui:ui-annotations:1.0.0'
>     implementation 'io.javaui:ui-state:1.0.0'
>     implementation 'io.javaui:ui-runtime:1.0.0'
>     implementation 'io.javaui:ui-layout:1.0.0'
>     implementation 'io.javaui:ui-renderer:1.0.0'
>     implementation 'io.javaui:ui-input:1.0.0'
>     implementation 'io.javaui:ui-foundation:1.0.0'
>     implementation 'io.javaui:ui-material:1.0.0'
>     implementation 'io.javaui:ui-navigation:1.0.0'
>     annotationProcessor 'io.javaui:ui-compiler:1.0.0'
> }
> ```

---

## 3. Creating Your First Screen (`CounterScreen.java`)

Create `app/src/main/java/com/example/myjavauiapp/CounterScreen.java`:

```java
package com.example.myjavauiapp;

import io.javaui.annotation.Preview;
import io.javaui.annotation.UIComponent;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.state.IntState;

// Static import for all core declarative UI elements
import static io.javaui.foundation.UIFoundation.*;
import static io.javaui.material.UIMaterial.*;

public final class CounterScreen {

    @UIComponent
    public static UI Counter(Scope s) {
        // Unboxed primitive state (0 memory boxing allocations)
        IntState count = s.intState(0);

        return Column(
            Modifier.fillMaxSize().padding(24),

            TopBar(Modifier.DEFAULT, "JavaUI Counter", null, null),

            Spacer(Modifier.size(32)),

            Card(
                Modifier.fillMaxWidth().padding(16),
                
                // Fine-grained binding: ONLY this text node updates when count changes.
                // The Counter(...) method NEVER re-executes!
                Text(() -> "Count: " + count.get()),

                Spacer(Modifier.size(16)),

                Button("Increment", () -> count.update(c -> c + 1))
            )
        );
    }

    // Android Studio Layoutlib Preview
    @Preview(name = "Counter Screen Preview")
    public static UI PreviewCounter(Scope s) {
        return Counter(s);
    }
}
```

---

## 4. Hosting in `MainActivity.java`

JavaUI trees live inside a single host view (`JavaUIHostView`). You can set it as your Activity's content view directly:

```java
package com.example.myjavauiapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.javaui.renderer.JavaUIHostView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the host view and bind your root component
        JavaUIHostView hostView = new JavaUIHostView(this);
        hostView.setContent(CounterScreen::Counter);

        setContentView(hostView);
    }
}
```

---

## 5. Integrating with Existing XML Layouts

If you are migrating an existing app, place `JavaUIHostView` directly into any XML layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- Existing Toolbar / Legacy View -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Legacy XML Header" />

    <!-- Declarative JavaUI Host View -->
    <io.javaui.renderer.JavaUIHostView
        android:id="@+id/javaui_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

In your Activity or Fragment:

```java
JavaUIHostView container = findViewById(R.id.javaui_container);
container.setContent(CounterScreen::Counter);
```

---

## 6. Integrating with Fragments (`JavaUIFragment`)

To use JavaUI inside AndroidX Navigation graphs or ViewPager2:

```java
public class MyFragment extends io.javaui.interop.JavaUIFragment {
    @Override
    protected UI createUI(Scope scope) {
        return CounterScreen.Counter(scope);
    }
}
```

---

## 7. Using the Android Studio Preview & Plugin

1. Install the **JavaUI Developer Tools** plugin from disk or IDE plugins:
   `Settings` → `Plugins` → `Install Plugin from Disk...` → select `ui-android-studio-plugin-1.0.0.jar`.
2. Open any file with a `@Preview` method.
3. Click the **Split** or **Design** tab on the top-right of the editor.
4. The `@Preview` function renders live inside Android Studio using layoutlib without deploying to a device or emulator.
