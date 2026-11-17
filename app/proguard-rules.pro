# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.ddayforge.widget.**$$serializer { *; }
-keepclassmembers class com.ddayforge.widget.** {
    *** Companion;
}
-keepclasseswithmembers class com.ddayforge.widget.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers enum com.ddayforge.widget.** { *; }
