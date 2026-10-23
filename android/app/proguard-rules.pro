# Proguard rules for PdfCraft
-keepclasseswithmembers class com.storytold.pdfcraft.core.PdfCraftBridge {
    native <methods>;
}
-keep class com.storytold.pdfcraft.core.** { *; }
