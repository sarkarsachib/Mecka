# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/lib/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Keep all agent classes
-keep class ai.mecka.agents.** { *; }

# Keep IPC message classes
-keep class ai.mecka.runtime.** { *; }

# Keep security classes
-keep class ai.mecka.security.** { *; }

# Keep hardware interface classes
-keep class ai.mecka.hardware.** { *; }

# Keep all public methods in services
-keepclassmembers class ai.mecka.services.** {
    public *;
}

# Keep all enum classes
-keep class * extends java.lang.Enum

# Keep all Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep all coroutine classes
-keep class kotlinx.coroutines.** { *; }

# Keep all Timber logging
-keep class timber.log.Timber { *; }

# Keep all R classes
-keep class **.R$* { *; }

# Keep all BuildConfig classes
-keep class **.BuildConfig { *; }

# Keep all parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}