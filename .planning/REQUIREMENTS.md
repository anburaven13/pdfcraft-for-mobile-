# REQUIREMENTS.md — PdfCraft Android Requirements

## Feature Requirements (Derived from upstream storytold/pdfcraft)

### 1. Document Viewing & Rendering (REQ-VIEW)
- **REQ-VIEW-01:** Open PDFs from device storage, Downloads, Google Drive via Android Storage Access Framework (SAF).
- **REQ-VIEW-02:** High-fidelity rasterization using `pdfcraft-render` directly to Android `Bitmap` (ARGB_8888).
- **REQ-VIEW-03:** Smooth pinch-to-zoom, pan, double-tap zoom, and continuous scroll.
- **REQ-VIEW-04:** Night / dark reading mode (color inversion / paper tone adjustment).
- **REQ-VIEW-05:** Page indicator and quick scrub bar.

### 2. Page Organization & Manipulation (REQ-ORG)
- **REQ-ORG-01:** Grid view displaying real-time rendered page thumbnails.
- **REQ-ORG-02:** Drag-and-drop page reordering with instant visual feedback.
- **REQ-ORG-03:** Page rotation in 90-degree steps (CW / CCW / 180°).
- **REQ-ORG-04:** Delete selected page(s) and extract selected pages to a new PDF.
- **REQ-ORG-05:** Insert blank page or pages from another document.

### 3. PDF Operations & Utilities (REQ-TOOL)
- **REQ-TOOL-01:** **Merge:** Combine multiple PDF documents into a single document.
- **REQ-TOOL-02:** **Split:** Divide a document by page ranges, single pages, or bookmarks.
- **REQ-TOOL-03:** **Password Protection & Encryption:** Secure documents with user and owner passwords using AES-256 via `pdfcraft-crypt`.
- **REQ-TOOL-04:** **Password Decryption:** Unlock and read password-protected documents seamlessly.

### 4. Forms & Annotations (REQ-FORM)
- **REQ-FORM-01:** Detect and display interactive AcroForm fields (Text inputs, Checkboxes, Dropdowns).
- **REQ-FORM-02:** Fill form fields and regenerate appearances through `pdfcraft-forms`.
- **REQ-FORM-03:** Freehand drawing / ink signature overlay and placement.

### 5. Native JNI Bridge (REQ-JNI)
- **REQ-JNI-01:** Memory-safe Rust cdylib (`libpdfcraft_android.so`) exposing JNI interfaces for document lifecycle (`open`, `close`, `save`, `getPageCount`, `getPageDimensions`).
- **REQ-JNI-02:** High-speed `renderPageToBitmap` passing direct byte buffer without copying.
- **REQ-JNI-03:** Thread-safe execution using background coroutines (never block Android main thread).

### 6. Cloud CI/CD & Delivery (REQ-CICD)
- **REQ-CICD-01:** GitHub Actions workflow (`build-apk.yml`) building native Rust libraries (`cargo-ndk`) for `arm64-v8a` and `x86_64`.
- **REQ-CICD-02:** Automatic building of debug APK and signed release APK.
- **REQ-CICD-03:** Support for GitHub Repository Secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) for production signing.
- **REQ-CICD-04:** Automatic release generation and artifact upload for direct phone installation.
