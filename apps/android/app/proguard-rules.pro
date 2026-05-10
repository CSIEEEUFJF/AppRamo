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

# Firestore cria estes modelos por reflexão ao usar toObject/data class.
-keep class com.ramoieeeufjf.appRamo.pages.ChapterEvent { *; }
-keep class com.ramoieeeufjf.appRamo.pages.ChapterTask { *; }
-keep class com.ramoieeeufjf.appRamo.pages.UserProfile { *; }

# Dependência opcional referenciada por bibliotecas transitivas.
-dontwarn org.slf4j.impl.StaticLoggerBinder
