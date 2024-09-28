# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
	public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Strip debug log
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Activity and Fragment names
-keep class it.dhd.oxygencustomizer.ui.activities.**
-keep class it.dhd.oxygencustomizer.ui.fragments.**

# Weather
-keepnames class it.dhd.oxygencustomizer.weather.**
-keep class it.dhd.oxygencustomizer.weather.** { *; }

# Xposed
-keep class de.robv.android.xposed.**
-keep class it.dhd.oxygencustomizer.xposed.MainHook
-keepnames class it.dhd.oxygencustomizer.xposed.**
-keepnames class it.dhd.oxygencustomizer.xposed.XPrefs
-keep class it.dhd.oxygencustomizer.xposed.** { *; }

# UI
-keep class it.dhd.oxygencustomizer.ui.** { *; }

# EventBus
-keepattributes *Annotation*
-keepclassmembers,allowoptimization,allowobfuscation class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep,allowoptimization,allowobfuscation enum org.greenrobot.eventbus.ThreadMode { *; }

# If using AsyncExecutord, keep required constructor of default event used.
# Adjust the class name if a custom failure event type is used.
-keepclassmembers,allowoptimization,allowobfuscation class org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Accessed via reflection, avoid renaming or removal
-keep,allowoptimization,allowobfuscation class org.greenrobot.eventbus.android.AndroidComponentsImpl

# Keep the ConstraintLayout Motion class
-keep,allowoptimization,allowobfuscation class androidx.constraintlayout.motion.** { *; }

# Keep Recycler View Stuff
-keep,allowoptimization,allowobfuscation class androidx.recyclerview.widget.** { *; }

# Keep Parcelable Creators
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Obfuscation
-repackageclasses
-allowaccessmodification

# Root Service
-keep class it.dhd.oxygencustomizer.services.* { *; }
-keep class it.dhd.oxygencustomizer.IRootProviderProxy { *; }
-keep class it.dhd.oxygencustomizer.IRootProviderService { *; }

# AIDL Classes
-keep interface **.I* { *; }
-keep class **.I*$Stub { *; }
-keep class **.I*$Stub$Proxy { *; }

# Markdown View
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**
-keep class br.tiagohm.markdownview.** { *; }
-keep class com.vladsch.flexmark.** { *; }
-keep class com.vladsch.flexmark.ext.** { *; }
-keep class com.vladsch.flexmark.util.** { *; }

-printusage release/usage.txt
-printmapping release/mapping.txt