# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\phil\AppData\Local\Android\sdk/ic_tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# if you get warnings that stop proguard building you can swap all -dontwarn below to this catch all statement
#-ignorewarnings

#http://stackoverflow.com/questions/35321742/android-proguard-most-aggressive-optimizations
-optimizationpasses 5
-allowaccessmodification
-overloadaggressively
-repackageclasses ''
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations **
# oddly removing the lines above seems to make thigns slower?

#-optimizations code/simplification/arithmetic
#-optimizations code/simplification/cast
#-optimizations field/*
#-optimizations class/merging/*

# my code
-dontwarn old.**
-dontwarn tools.swing.**
-dontwarn com.gg.**
-dontwarn tools.bootstrap.**
-dontwarn tools.updater.**
-dontwarn tools.db.**
-dontwarn tools.image.**
-dontwarn tools.zip.**
-dontwarn esmj3dfo4.j3d.cell.J3dCELL
-dontwarn scrollsexplorer.ScrollsExplorerNewt

#sound libs
-dontwarn javazoom.**
-dontwarn org.tritonus.**


#jbullet (lwjgl)
-dontwarn cz.advel.stack.instrument.**
-dontwarn cz.advel.stack.**
-dontwarn net.java.games.**
-dontwarn org.lwjgl.**

#jogl
-dontwarn jogamp.opengl.**
-dontwarn com.jogamp.opengl.**
-dontwarn com.jogamp.common.util.awt.**
-dontwarn com.jogamp.nativewindow.**



#if any class not found issues occured replace all "-keep" lines below with these 2 lines
# -keep class com.jogamp.** { *; }
# -keep class jogamp.** { *; }

#gluegen-rt-android.jar
-keep class jogamp.common.os.android.AndroidUtilsImpl { *; }

#joal-android.jar
-keep class com.jogamp.openal.** { *; }
-keep class jogamp.openal.** { *; }

#jogl-all-android.jar
-keep class com.jogamp.nativewindow.egl.EGLGraphicsDevice { *; }
-keep class com.jogamp.opengl.egl.** { *; }

-keep class jogamp.graph.font.typecast.TypecastFontConstructor { *; }
-keep class jogamp.graph.curve.opengl.shader.** { *; }

-keep class jogamp.newt.driver.** { *; }
-keep class jogamp.opengl.** { *; }

#my code
-keep class nif.** { *; }

#proguard rules base
# The remainder of this file is identical to the non-optimized version
# of the Proguard configuration file (except that the other file has
# flags to turn off optimization).

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontwarn android.support.**

