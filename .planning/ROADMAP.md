# ROADMAP.md — PdfCraft Android Implementation Roadmap

## Phases & Execution Plan

### Phase 1: Workspace & Core Rust Setup
- [x] Analyze upstream repository `storytold/pdfcraft` crates and dependencies.
- [x] Initialize Git repository and project structure.
- [x] Configure workspace `Cargo.toml` and integrate core crates.
- [ ] Create `crates/android` JNI crate bridging Kotlin to `pdfcraft-engine`, `pdfcraft-render`, `pdfcraft-organize`, `pdfcraft-forms`, and `pdfcraft-crypt`.

### Phase 2: Android Project Architecture & Native Bridge
- [ ] Initialize Android project with Gradle 8+, Android Gradle Plugin, and Kotlin.
- [ ] Configure `app/build.gradle.kts` with Jetpack Compose (BOM), Material 3, Navigation, Coroutines.
- [ ] Implement `PdfCraftNative` JNI wrapper class in Kotlin with strict lifecycle and error handling.
- [ ] Implement `PdfDocumentManager` repository managing native document instances.

### Phase 3: Mobile UI & Feature Implementation
- [ ] **Home & Document Picker:** Modern Material 3 dashboard with Recent Files, Open from Storage (SAF), and quick tool launchers.
- [ ] **Viewer Screen:** High-fidelity gesture-based canvas (pinch zoom, pan, double tap), scrubber, night mode toggle, and page navigator.
- [ ] **Organize Screen:** Grid thumbnail view with drag-and-drop reordering, multi-select rotation, deletion, and extraction.
- [ ] **Tools Suite:**
  - Merge PDFs dialog & workflow.
  - Split PDF dialog & workflow.
  - Password Protect / Encrypt dialog (AES-256).
  - Password Unlock dialog with retry handling.
- [ ] **Form Filling & Signatures:** Interactive field editing and digital/ink signature drawing pad.

### Phase 4: CI/CD Pipeline & GitHub Actions APK Builder
- [ ] Create `.github/workflows/build-apk.yml`.
- [ ] Multi-arch Rust compilation (`cargo-ndk` for `aarch64-linux-android` and `x86_64-linux-android`).
- [ ] Gradle build with release keystore signing via GitHub Secrets (and automatic fallback for self-signing).
- [ ] Automatic GitHub Actions Artifact upload and GitHub Release creation on tag push.

### Phase 5: Documentation & Git Remote Push Preparation
- [ ] Create comprehensive `README.md` with features, architecture diagram, GitHub Actions setup instructions, and mobile screenshots guide.
- [ ] Add `.gitignore` for Android, Gradle, and Rust.
- [ ] Commit all code cleanly and prepare for pushing to user's GitHub repository.
