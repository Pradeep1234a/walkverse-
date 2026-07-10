# LiquidCalc - Premium Android Glassmorphic Calculator

LiquidCalc is a premium Android calculator application inspired by the futuristic **iOS 26 Liquid Glass** design language. It combines physical depth, light refraction, and fluid touch physics to deliver a stunning glassmorphic UI, suspended above an animated mesh gradient.

---

## 🎨 Visual Design Language & Aesthetics

- **Layered Glass Hierarchy**: Controls appear suspended above the animated background with soft shadows (`shadow-2xl`) and inner edge highlights for depth.
- **Glassmorphic Materials**: Surfaces feature translucent glass stylings using linear reflections (`bg-white/10` and `border border-white/20`).
- **Animated Mesh Gradients**: The background features continuous flowing nodes of royal violet, cyan, and deep indigo.
- **Dynamic Glow Lighting**: Equals and active operator keys feature a luminous colored bloom that reflects onto the glass buttons.
- **Micro-Interactions**: Buttons dynamically compress slightly (down to `scale-95`) on touch to mimic physical key presses.

---

## ⚙️ Features

- **Standard Layout**: A classic 4-column layout for everyday math.
- **Scientific Mode**: Seamlessly morphs to a 6-column grid adding functions like `sin`, `cos`, `tan`, `ln`, `log`, `sqrt`, parenthesis, and constants ($\pi$, $e$).
- **Calculations History**: Swipeable drawer showing past calculations. Tap a calculation to load it back into the formula bar.
- **Deg/Rad Angle Systems**: Tap the toggle in the top bar to calculate angles in degrees or radians.
- **Adaptive Preset Themes**:
  - **Nebula**: Deep Purple & Cyan highlights.
  - **Aurora**: Vibrant Teal & Green gradients.
  - **Sunset**: Warm Pink & Amber tones.

---

## 🏗️ Architecture

- **Clean Architecture & MVVM**: Strictly separates UI layers from state logic.
- **Recursive Descent Math Parser**: A pure Kotlin mathematical evaluator parsing expressions offline with full support for operator precedence and parenthesis nesting.
- **Jetpack Compose**: Native declarative layout rendering ensuring fluid animations at 120Hz.

---

## 🛠️ Build and Installation

### Requirements
- **JDK**: Java 17
- **Gradle**: 9.1.0+
- **Android SDK**: API level 26 (Android 8.0 Oreo) up to API level 36 (Android 15+)

### Local Compilation
To compile a debug APK on Windows, double-click or run:
```cmd
run_gradle.bat assembleDebug
```
The compiled APK will be output to:
`app/build/outputs/apk/debug/app-debug.apk`
