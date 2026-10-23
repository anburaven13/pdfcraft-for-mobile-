# PROJECT.md — PdfCraft for Android

## 1. Overview
- **Project Name:** PdfCraft Android (Mobile Port)
- **Upstream Repository:** [storytold/pdfcraft](https://github.com/storytold/pdfcraft)
- **Primary Goal:** Port the privacy-first, pure-Rust PDF workbench `pdfcraft` to Android as a high-performance native app built with **Jetpack Compose (Kotlin)** and a high-speed **Rust JNI bridge (`libpdfcraft_android.so`)**.
- **Build Infrastructure:** Cloud-first compilation via **GitHub Actions** (`.github/workflows/build-apk.yml`) to generate signed APKs without requiring gigabytes of local toolchains on developer machines.

## 2. Core Pillars
1. **100% Offline & Private:** Zero analytics, zero cloud reliance, zero external telemetry. All PDF operations, cryptographic handlers, and page rendering happen strictly on the local device.
2. **Native Mobile UX:** Material Design 3 interface customized for touch targets, gesture navigation (pinch-to-zoom, pan, swipe thumbnails), system document pickers (Storage Access Framework), and Android Share Sheet.
3. **Pure Rust Performance:** Unaltered PDF object model, tolerant parser, incremental copy-on-write saver, and high-fidelity rendering pipeline compiling straight to `aarch64-linux-android` / `x86_64-linux-android`.
4. **Zero-Overhead Cloud CI/CD:** Complete GitHub Actions workflow capable of running matrix builds, compiling native libraries via `cargo-ndk`, assembling APKs, signing with release keystores, and publishing installable artifacts.

## 3. Technology Stack
- **Mobile Frontend:** Kotlin, Jetpack Compose, Material 3, AndroidX Lifecycle, Coroutines, StateFlow, Android SAF.
- **Native Bridge:** Rust (`pdfcraft-android` cdylib), JNI (Java Native Interface), `bytemuck` / direct ByteBuffer transfer for zero-copy bitmap rendering.
- **Engine Core:** `pdfcraft-engine`, `pdfcraft-render`, `pdfcraft-organize`, `pdfcraft-cos`, `pdfcraft-crypt`, `pdfcraft-forms`, `pdfcraft-annot`.
- **CI/CD & DevOps:** GitHub Actions, Ubuntu runner, Rust stable + Android NDK r26+, OpenJDK 17, Gradle 8+, Android Gradle Plugin (AGP).
