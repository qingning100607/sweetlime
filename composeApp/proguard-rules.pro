-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.qingning.sweetlime.ui.nav.**$$serializer { *; }
-keepclassmembers class com.qingning.sweetlime.ui.nav.** {
    *** Companion;
    *** INSTANCE;
}

-dontwarn org.jetbrains.annotations.**
