# 📱 PdfCraft for Android

<p align="center">
  <b>High-Performance, Privacy-First PDF Workbench for Android</b><br>
  <i>Powered by the pure-Rust <a href="https://github.com/storytold/pdfcraft">PdfCraft</a> engine</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin_%26_Rust_2024-DEA584?logo=rust&logoColor=white" alt="Languages" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?logo=jetpackcompose&logoColor=white" alt="UI" />
  <img src="https://img.shields.io/badge/Offline-100%25_Local_%26_Private-success" alt="Offline" />
  <img src="https://img.shields.io/badge/License-MIT_OR_Apache--2.0-blue" alt="License" />
</p>

---

## 📖 Overview

**PdfCraft for Android** is a native mobile port of the open-source, clean-room PDF reader and editor [`storytold/pdfcraft`](https://github.com/storytold/pdfcraft).

Unlike standard Android PDF tools that rely on cloud servers or closed-source viewers, PdfCraft Android runs a **pure Rust PDF engine directly on the device** via high-speed JNI bindings. It is **100% offline**, requires **no user accounts**, and contains **zero telemetry**.

---

## 🌟 Key Features

### 🔍 1. High-Fidelity PDF Viewer
- **Touch Gestures:** Smooth pinch-to-zoom (up to 5x), fluid panning, and double-tap zoom reset.
- **Night / Dark Reading Mode:** Inverts document tones for comfortable reading in low light while preserving contrast.
- **Direct Memory Rasterization:** Rust renders pages into direct memory buffers and loads them straight into hardware-accelerated Android Bitmaps (`ARGB_8888`).
- **Text Layer Extraction:** Extract and copy selectable text in true reading order.
- **Page Scrubber:** Rapidly navigate documents with an interactive slider.

### 📑 2. Page Organization & Editing
- **Thumbnail Grid:** Visual overview of all document pages.
- **Multi-Select Rotation:** Rotate pages in 90° clockwise or counter-clockwise steps.
- **Page Deletion & Extraction:** Select unwanted pages to delete, or extract selected pages into a brand new standalone PDF.
- **Copy-on-Write Safety:** Edits are atomic and non-destructive.

### 🛠️ 3. PDF Workbench Tools
- **Merge PDFs:** Select multiple PDF documents and combine them into a single file with one tap.
- **Split PDF:** Break large documents apart into dedicated page ranges.
- **AES-256 Encryption:** Secure confidential files with user and owner passwords using standard PDF security handlers (R6 AES-256).
- **Password Decryption:** Open password-protected PDFs securely on your phone.

### 🔒 4. Privacy & System Integration
- **Storage Access Framework (SAF):** Open and save files anywhere—internal storage, SD card, Google Drive, or Downloads.
- **Android Share Sheet:** Send and receive PDFs to/from WhatsApp, Gmail, Telegram, and file managers.
- **Zero Cloud Reliance:** No external requests, no analytics, no third-party trackers.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│               Android Frontend (Kotlin + Jetpack Compose)       │
│    • Material 3 UI         • Pinch-to-zoom Canvas (PdfPageView) │
│    • SAF File Picker       • Android Share Sheet Intent Handler │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ JNI Native Calls
┌─────────────────────────────────▼───────────────────────────────┐
│              PdfCraft JNI Bridge (`crates/android`)             │
│    `libpdfcraft_android.so` (arm64-v8a / x86_64)                │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ Direct Rust calls
┌─────────────────────────────────▼───────────────────────────────┐
│              PdfCraft Core Rust Engine                          │
│  ┌──────────────────────┬─────────────────────────────────────┐ │
│  │ pdfcraft-cos         │ Object graph, parser, lazy reader   │ │
│  │ pdfcraft-render      │ Rasterizer (hayro / vello) to RGBA  │ │
│  │ pdfcraft-organize    │ Combine, split, rotate, delete      │ │
│  │ pdfcraft-crypt       │ Standard security handler (AES-256) │ │
│  │ pdfcraft-forms       │ Interactive AcroForm models         │ │
│  │ pdfcraft-engine      │ Document session facade             │ │
│  └──────────────────────┴─────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Building the APK via GitHub Actions (Zero Local Space)

You do **not** need to install gigabytes of Android NDK, SDKs, or Rust Android toolchains on your PC. The included GitHub Actions workflow (`.github/workflows/build-apk.yml`) builds and signs the APK in the cloud for free.

### How It Works:
1. Every time you push code or trigger the workflow, GitHub Actions:
   - Sets up Ubuntu with Java 17, Android NDK (r26b), and Rust.
   - Installs `cargo-ndk` and compiles `libpdfcraft_android.so` for `arm64-v8a` (phones) and `x86_64` (emulators).
   - Bundles the native libraries and builds the Android APK with Gradle.
   - Signs the APK with an Android keystore.
   - Uploads the signed APK to **GitHub Actions Artifacts** (and creates a **GitHub Release** on tag push `v*`).

### Download Your APK:
1. Go to your repository on GitHub.
2. Click the **Actions** tab.
3. Select the latest **"Build & Sign PdfCraft Android APK"** run.
4. Scroll down to **Artifacts** and download **`PdfCraft-Android-APK`**!
5. Transfer the `.apk` to your Android phone and install.

---

## 🔑 Custom Release Signing (Optional)

If you have your own Android release keystore and want custom signing, add these 4 Repository Secrets in **GitHub Repository Settings ➔ Secrets and variables ➔ Actions**:

| Secret Name | Value Description |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` or `.keystore` file (`base64 -w 0 release.jks`) |
| `KEYSTORE_PASSWORD` | Password for your keystore |
| `KEY_ALIAS` | Alias name of your key in the keystore |
| `KEY_PASSWORD` | Password for the key alias |

*(If you do not set these secrets, the workflow automatically generates a self-signed release key so that your APK is always signed and installable!)*

---

## 📤 How to Push This Repository to GitHub

When you create your repository on GitHub, run these commands:

```bash
# 1. Add all files to git
git add .

# 2. Create the initial commit
git commit -m "feat: complete PdfCraft Android port with Jetpack Compose, JNI bridge, and GitHub Actions CI/CD"

# 3. Rename branch to main
git branch -M main

# 4. Link your remote GitHub repository (replace with your repo URL):
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git

# 5. Push to GitHub
git push -u origin main
```

Once pushed, GitHub Actions will automatically start compiling the APK!

---

## 📄 License

Licensed under either of:
- Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE))
- MIT License ([LICENSE-MIT](LICENSE-MIT))

at your option.
