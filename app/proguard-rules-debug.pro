# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\phil\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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


#-optimizationpasses 5
#-allowaccessmodification
#-overloadaggressively
#-repackageclasses ''
#-dontskipnonpubliclibraryclasses

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

#jbullet (lwjgl)
-dontwarn cz.advel.stack.instrument.**
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
#nothing

#jogl-all-android.jar
-keep class com.jogamp.nativewindow.egl.EGLGraphicsDevice { *; }
-keep class com.jogamp.opengl.egl.** { *; }

-keep class jogamp.graph.font.typecast.TypecastFontConstructor { *; }
-keep class jogamp.graph.curve.opengl.shader.** { *; }

-keep class jogamp.newt.driver.** { *; }
-keep class jogamp.opengl.** { *; }

#my code
-keep class nif.** { *; }

