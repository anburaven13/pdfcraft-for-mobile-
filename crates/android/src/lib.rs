//! JNI Bridge for PdfCraft Android
//! Exposes PdfCraft's pure-Rust engine, renderer, page organizer, and crypt to Android.

use std::sync::{Arc, Mutex};
use jni::JNIEnv;
use jni::objects::{JByteArray, JClass, JIntArray, JObject, JObjectArray, JString};
use jni::sys::{jboolean, jbyteArray, jfloat, jfloatArray, jint, jintArray, jlong, jobjectArray, jsize, jstring};

use pdfcraft_cos::{Document as CosDocument, SaveOptions, write_full};
use pdfcraft_render::{PageRenderer, RenderConfig, RenderRequest, RequestKind};

pub struct NativePdfDocument {
    pub bytes: Arc<Vec<u8>>,
    pub doc: CosDocument,
    pub renderer: Mutex<PageRenderer>,
    pub password: Option<String>,
}

// Helper to convert raw handle to NativePdfDocument
unsafe fn get_doc<'a>(handle: jlong) -> Option<&'a mut NativePdfDocument> {
    if handle == 0 {
        None
    } else {
        unsafe { Some(&mut *(handle as *mut NativePdfDocument)) }
    }
}

/// Open a PDF document from raw bytes with optional password.
/// Returns a non-zero handle (pointer) on success, or 0 on failure.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeOpenDocument(
    mut env: JNIEnv,
    _class: JClass,
    bytes_arr: JByteArray,
    password_str: JString,
) -> jlong {
    let bytes_vec = match env.convert_byte_array(&bytes_arr) {
        Ok(v) => v,
        Err(_) => return 0,
    };
    let arc_bytes = Arc::new(bytes_vec);

    let password = if !password_str.is_null() {
        match env.get_string(&password_str) {
            Ok(s) => Some(String::from(s)),
            Err(_) => None,
        }
    } else {
        None
    };

    let cos_doc = match CosDocument::open_with_password(arc_bytes.clone(), password.as_deref()) {
        Ok(d) => d,
        Err(_) => return 0,
    };

    let mut config = RenderConfig::default();
    if let Some(ref pwd) = password {
        config.password = Some(Arc::from(pwd.as_str()));
    }
    let renderer = PageRenderer::new(arc_bytes.clone(), config);

    let native = Box::new(NativePdfDocument {
        bytes: arc_bytes,
        doc: cos_doc,
        renderer: Mutex::new(renderer),
        password,
    });

    Box::into_raw(native) as jlong
}

/// Close and destroy the native document handle.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeCloseDocument(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) {
    if handle != 0 {
        unsafe {
            let _ = Box::from_raw(handle as *mut NativePdfDocument);
        }
    }
}

/// Return total number of pages in the document.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeGetPageCount(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jint {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return 0,
    };
    if let Ok(r) = native.renderer.lock() {
        r.page_count() as jint
    } else {
        pdfcraft_organize::pages(&native.doc).map(|p| p.len()).unwrap_or(0) as jint
    }
}

/// Return page dimensions in PDF points [width, height].
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeGetPageDimensions(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
) -> jfloatArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let boxes = match pdfcraft_organize::page_boxes(&native.doc) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    let idx = page_index as usize;
    if idx >= boxes.len() {
        return std::ptr::null_mut();
    }

    let crop = boxes[idx][1];
    let width = (crop[2] - crop[0]).abs() as f32;
    let height = (crop[3] - crop[1]).abs() as f32;

    let result = [width, height];
    let j_arr = match env.new_float_array(2) {
        Ok(a) => a,
        Err(_) => return std::ptr::null_mut(),
    };
    if env.set_float_array_region(&j_arr, 0, &result).is_err() {
        return std::ptr::null_mut();
    }
    j_arr.into_raw()
}

/// Render a page directly into an Android DirectByteBuffer.
/// Returns [rendered_width, rendered_height] on success, or empty on failure.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeRenderPage(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
    scale: jfloat,
    buffer_obj: JObject,
    night_mode: jboolean,
) -> jintArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let buf_ptr = match env.get_direct_buffer_address(&buffer_obj) {
        Ok(p) => p,
        Err(_) => return std::ptr::null_mut(),
    };
    let buf_cap = match env.get_direct_buffer_capacity(&buffer_obj) {
        Ok(c) => c,
        Err(_) => return std::ptr::null_mut(),
    };

    let mut renderer = match native.renderer.lock() {
        Ok(r) => r,
        Err(_) => return std::ptr::null_mut(),
    };

    let req = RenderRequest {
        page: page_index as usize,
        kind: RequestKind::Pixels,
        tile: None,
        scale: scale as f32,
        tag: 0,
        preview: false,
    };

    let rendered = renderer.render(req);
    let bytes = rendered.rgba.as_ref();
    if bytes.is_empty() || bytes.len() > buf_cap {
        return std::ptr::null_mut();
    }

    let is_night = night_mode != 0;
    unsafe {
        if is_night {
            let dst = std::slice::from_raw_parts_mut(buf_ptr, bytes.len());
            for i in (0..bytes.len()).step_by(4) {
                if i + 3 < bytes.len() {
                    dst[i] = 255 - bytes[i];
                    dst[i + 1] = 255 - bytes[i + 1];
                    dst[i + 2] = 255 - bytes[i + 2];
                    dst[i + 3] = bytes[i + 3];
                }
            }
        } else {
            std::ptr::copy_nonoverlapping(bytes.as_ptr(), buf_ptr, bytes.len());
        }
    }

    let out = [rendered.width as i32, rendered.height as i32];
    let j_arr = match env.new_int_array(2) {
        Ok(a) => a,
        Err(_) => return std::ptr::null_mut(),
    };
    if env.set_int_array_region(&j_arr, 0, &out).is_err() {
        return std::ptr::null_mut();
    }
    j_arr.into_raw()
}

/// Render a thumbnail for a page, returned as encoded JPEG byte array.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeRenderThumbnail(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
    max_dimension: jint,
) -> jbyteArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let boxes = match pdfcraft_organize::page_boxes(&native.doc) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    let idx = page_index as usize;
    if idx >= boxes.len() {
        return std::ptr::null_mut();
    }

    let crop = boxes[idx][1];
    let width = (crop[2] - crop[0]).abs() as f32;
    let height = (crop[3] - crop[1]).abs() as f32;
    let max_side = width.max(height);
    let scale = (max_dimension as f32) / max_side.max(1.0);

    let mut renderer = match native.renderer.lock() {
        Ok(r) => r,
        Err(_) => return std::ptr::null_mut(),
    };

    let req = RenderRequest {
        page: idx,
        kind: RequestKind::Pixels,
        tile: None,
        scale,
        tag: 0,
        preview: true,
    };

    let rendered = renderer.render(req);
    let bytes = rendered.rgba.as_ref();
    if bytes.is_empty() {
        return std::ptr::null_mut();
    }

    let img_buf = match image::RgbaImage::from_raw(rendered.width, rendered.height, bytes.to_vec()) {
        Some(b) => b,
        None => return std::ptr::null_mut(),
    };

    let mut jpeg_bytes = Vec::new();
    let mut cursor = std::io::Cursor::new(&mut jpeg_bytes);
    if image::DynamicImage::ImageRgba8(img_buf)
        .write_to(&mut cursor, image::ImageFormat::Jpeg)
        .is_err()
    {
        return std::ptr::null_mut();
    }

    match env.byte_array_from_slice(&jpeg_bytes) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Rotate pages by 90, 180, or 270 degrees.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeRotatePages(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_indices: JIntArray,
    degrees: jint,
) -> jboolean {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return 0,
    };

    let len = match env.get_array_length(&page_indices) {
        Ok(l) => l as usize,
        Err(_) => return 0,
    };
    let mut indices_i32 = vec![0i32; len];
    if env.get_int_array_region(&page_indices, 0, &mut indices_i32).is_err() {
        return 0;
    }
    let indices: Vec<usize> = indices_i32.into_iter().map(|i| i as usize).collect();

    if pdfcraft_organize::rotate_pages(&mut native.doc, &indices, degrees as i64).is_ok() {
        1
    } else {
        0
    }
}

/// Delete specified pages.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeDeletePages(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_indices: JIntArray,
) -> jboolean {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return 0,
    };

    let len = match env.get_array_length(&page_indices) {
        Ok(l) => l as usize,
        Err(_) => return 0,
    };
    let mut indices_i32 = vec![0i32; len];
    if env.get_int_array_region(&page_indices, 0, &mut indices_i32).is_err() {
        return 0;
    }
    let indices: Vec<usize> = indices_i32.into_iter().map(|i| i as usize).collect();

    if pdfcraft_organize::delete_pages(&mut native.doc, &indices).is_ok() {
        1
    } else {
        0
    }
}

/// Move a single page from one position to another.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeMovePage(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
    from_index: jint,
    to_index: jint,
) -> jboolean {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return 0,
    };

    if pdfcraft_organize::move_pages(&mut native.doc, &[from_index as usize], to_index as usize).is_ok() {
        1
    } else {
        0
    }
}

/// Extract specified pages into a new standalone PDF document.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeExtractPages(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_indices: JIntArray,
) -> jbyteArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let len = match env.get_array_length(&page_indices) {
        Ok(l) => l as usize,
        Err(_) => return std::ptr::null_mut(),
    };
    let mut indices_i32 = vec![0i32; len];
    if env.get_int_array_region(&page_indices, 0, &mut indices_i32).is_err() {
        return std::ptr::null_mut();
    }
    let indices: Vec<usize> = indices_i32.into_iter().map(|i| i as usize).collect();

    let extracted_doc = match pdfcraft_organize::extract_pages(&native.doc, &indices) {
        Ok(d) => d,
        Err(_) => return std::ptr::null_mut(),
    };

    let out_bytes = match write_full(&extracted_doc, &SaveOptions::default()) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    match env.byte_array_from_slice(&out_bytes) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Merge multiple PDF files into one combined PDF.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeMergeDocuments(
    mut env: JNIEnv,
    _class: JClass,
    docs_array: jobjectArray,
) -> jbyteArray {
    let jobj_array = unsafe { JObjectArray::from_raw(docs_array) };
    let count = match env.get_array_length(&jobj_array) {
        Ok(c) => c as usize,
        Err(_) => return std::ptr::null_mut(),
    };

    let mut arc_list = Vec::with_capacity(count);
    for i in 0..count {
        let item = match env.get_object_array_element(&jobj_array, i as jsize) {
            Ok(it) => it,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_arr = unsafe { JByteArray::from_raw(item.into_raw()) };
        let vec = match env.convert_byte_array(&b_arr) {
            Ok(v) => v,
            Err(_) => return std::ptr::null_mut(),
        };
        arc_list.push(Arc::new(vec));
    }

    let docs: Vec<CosDocument> = arc_list.iter().filter_map(|b| CosDocument::open(b.clone()).ok()).collect();
    let pairs: Vec<(&str, &CosDocument)> = docs.iter().map(|d| ("doc", d)).collect();
    let combined_doc = match pdfcraft_organize::combine(&pairs) {
        Ok(d) => d,
        Err(_) => return std::ptr::null_mut(),
    };

    let out_bytes = match write_full(&combined_doc, &SaveOptions::default()) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    match env.byte_array_from_slice(&out_bytes) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Save the modified document to full PDF bytes.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeSaveDocument(
    env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jbyteArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let out_bytes = match write_full(&native.doc, &SaveOptions::default()) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    match env.byte_array_from_slice(&out_bytes) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Encrypt document with user and owner passwords using AES-256 standard handler.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeEncryptDocument(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    user_pwd: JString,
    owner_pwd: JString,
) -> jbyteArray {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let user_str = match env.get_string(&user_pwd) {
        Ok(s) => String::from(s),
        Err(_) => return std::ptr::null_mut(),
    };
    let owner_str = match env.get_string(&owner_pwd) {
        Ok(s) => String::from(s),
        Err(_) => return std::ptr::null_mut(),
    };

    let encryption = pdfcraft_crypt::NewEncryption {
        algorithm: pdfcraft_crypt::Algorithm::Aes256,
        user_password: &user_str,
        owner_password: &owner_str,
        permissions: -4,
        encrypt_metadata: true,
        seed: [42u8; 32],
    };

    if native.doc.set_encryption(&encryption).is_err() {
        return std::ptr::null_mut();
    }

    let out_bytes = match write_full(&native.doc, &SaveOptions::default()) {
        Ok(b) => b,
        Err(_) => return std::ptr::null_mut(),
    };

    match env.byte_array_from_slice(&out_bytes) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Extract text from a page in reading order.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_storytold_pdfcraft_core_PdfCraftBridge_nativeGetPageText(
    mut env: JNIEnv,
    _class: JClass,
    handle: jlong,
    page_index: jint,
) -> jstring {
    let native = match unsafe { get_doc(handle) } {
        Some(d) => d,
        None => return std::ptr::null_mut(),
    };

    let mut renderer = match native.renderer.lock() {
        Ok(r) => r,
        Err(_) => return std::ptr::null_mut(),
    };

    let req = RenderRequest {
        page: page_index as usize,
        kind: RequestKind::Text,
        tile: None,
        scale: 1.0,
        tag: 0,
        preview: false,
    };

    let rendered = renderer.render(req);
    let text = match rendered.text {
        Some(t) => t.plain_text(),
        None => String::new(),
    };

    match env.new_string(text) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
