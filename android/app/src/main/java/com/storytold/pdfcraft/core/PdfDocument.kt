package com.storytold.pdfcraft.core

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-level Kotlin wrapper managing the native PDF document handle and memory lifecycle.
 */
class PdfDocument private constructor(
    private var handle: Long,
    val name: String,
    val initialBytes: ByteArray
) : AutoCloseable {

    val pageCount: Int
        get() {
            checkNotClosed()
            return PdfCraftBridge.nativeGetPageCount(handle)
        }

    fun getPageDimensions(pageIndex: Int): Pair<Float, Float> {
        checkNotClosed()
        val dims = PdfCraftBridge.nativeGetPageDimensions(handle, pageIndex)
            ?: floatArrayOf(612f, 792f)
        return Pair(dims[0], dims[1])
    }

    suspend fun renderPage(pageIndex: Int, scale: Float = 1.5f, nightMode: Boolean = false): Bitmap? =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.renderPageToBitmap(handle, pageIndex, scale, nightMode)
        }

    suspend fun renderThumbnail(pageIndex: Int, maxDimension: Int = 320): Bitmap? =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.renderThumbnailToBitmap(handle, pageIndex, maxDimension)
        }

    suspend fun rotatePages(pageIndices: IntArray, degrees: Int): Boolean =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeRotatePages(handle, pageIndices, degrees)
        }

    suspend fun deletePages(pageIndices: IntArray): Boolean =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeDeletePages(handle, pageIndices)
        }

    suspend fun movePage(fromIndex: Int, toIndex: Int): Boolean =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeMovePage(handle, fromIndex, toIndex)
        }

    suspend fun extractPages(pageIndices: IntArray): ByteArray? =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeExtractPages(handle, pageIndices)
        }

    suspend fun save(): ByteArray? =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeSaveDocument(handle)
        }

    suspend fun encrypt(userPassword: String, ownerPassword: String = userPassword): ByteArray? =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeEncryptDocument(handle, userPassword, ownerPassword)
        }

    suspend fun getPageText(pageIndex: Int): String =
        withContext(Dispatchers.Default) {
            checkNotClosed()
            PdfCraftBridge.nativeGetPageText(handle, pageIndex) ?: ""
        }

    override fun close() {
        if (handle != 0L) {
            PdfCraftBridge.nativeCloseDocument(handle)
            handle = 0L
        }
    }

    private fun checkNotClosed() {
        if (handle == 0L) {
            throw IllegalStateException("PdfDocument has already been closed")
        }
    }

    companion object {
        suspend fun open(bytes: ByteArray, name: String = "document.pdf", password: String? = null): PdfDocument? =
            withContext(Dispatchers.Default) {
                val h = PdfCraftBridge.nativeOpenDocument(bytes, password)
                if (h != 0L) {
                    PdfDocument(h, name, bytes)
                } else {
                    null
                }
            }

        suspend fun merge(documents: List<ByteArray>): ByteArray? =
            withContext(Dispatchers.Default) {
                PdfCraftBridge.nativeMergeDocuments(documents.toTypedArray())
            }
    }
}
