package com.storytold.pdfcraft.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.nio.ByteBuffer

/**
 * JNI Bridge to PdfCraft's pure-Rust engine (libpdfcraft_android.so).
 */
object PdfCraftBridge {
    private const val TAG = "PdfCraftBridge"
    private var isLoaded = false

    init {
        try {
            System.loadLibrary("pdfcraft_android")
            isLoaded = true
            Log.i(TAG, "Successfully loaded native library: libpdfcraft_android.so")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load libpdfcraft_android.so", e)
            isLoaded = false
        }
    }

    fun isNativeReady(): Boolean = isLoaded

    // Native declarations
    external fun nativeOpenDocument(bytes: ByteArray, password: String?): Long
    external fun nativeCloseDocument(handle: Long)
    external fun nativeGetPageCount(handle: Long): Int
    external fun nativeGetPageDimensions(handle: Long, pageIndex: Int): FloatArray?
    external fun nativeRenderPage(
        handle: Long,
        pageIndex: Int,
        scale: Float,
        buffer: ByteBuffer,
        nightMode: Boolean
    ): IntArray?
    external fun nativeRenderThumbnail(handle: Long, pageIndex: Int, maxDimension: Int): ByteArray?
    external fun nativeRotatePages(handle: Long, pageIndices: IntArray, degrees: Int): Boolean
    external fun nativeDeletePages(handle: Long, pageIndices: IntArray): Boolean
    external fun nativeMovePage(handle: Long, fromIndex: Int, toIndex: Int): Boolean
    external fun nativeExtractPages(handle: Long, pageIndices: IntArray): ByteArray?
    external fun nativeMergeDocuments(docsArray: Array<ByteArray>): ByteArray?
    external fun nativeSaveDocument(handle: Long): ByteArray?
    external fun nativeEncryptDocument(
        handle: Long,
        userPassword: String,
        ownerPassword: String
    ): ByteArray?
    external fun nativeGetPageText(handle: Long, pageIndex: Int): String?

    /**
     * High-speed page renderer converting Rust RGBA buffer directly into an Android Bitmap.
     */
    fun renderPageToBitmap(
        handle: Long,
        pageIndex: Int,
        scale: Float,
        nightMode: Boolean = false
    ): Bitmap? {
        val dims = nativeGetPageDimensions(handle, pageIndex) ?: return null
        val targetWidth = (dims[0] * scale).toInt().coerceAtLeast(1)
        val targetHeight = (dims[1] * scale).toInt().coerceAtLeast(1)

        val bufferSize = targetWidth * targetHeight * 4
        val directBuffer = ByteBuffer.allocateDirect(bufferSize)

        val renderedDims = nativeRenderPage(handle, pageIndex, scale, directBuffer, nightMode) ?: return null
        val w = renderedDims[0]
        val h = renderedDims[1]

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        directBuffer.rewind()
        bitmap.copyPixelsFromBuffer(directBuffer)
        return bitmap
    }

    /**
     * Decode thumbnail bytes directly into a Bitmap.
     */
    fun renderThumbnailToBitmap(handle: Long, pageIndex: Int, maxDimension: Int = 300): Bitmap? {
        val jpegBytes = nativeRenderThumbnail(handle, pageIndex, maxDimension) ?: return null
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}
