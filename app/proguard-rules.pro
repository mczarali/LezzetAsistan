# R8 / ProGuard Obfuscation Rules for Lezzet Asistanı

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,allowobfuscation class com.lezzetasistani.app.model.** { *; }
-keep,allowobfuscation class com.lezzetasistani.app.update.** { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
