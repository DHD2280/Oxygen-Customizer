# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
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

-printusage release/usage.txt