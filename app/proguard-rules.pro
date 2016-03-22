# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/android/way/tools/sdk/tools/proguard/proguard-android.txt
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
-keepattributes LineNumberTable,SourceFile

-keep class com.bugtags.library.** {*;}
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient.**
-dontwarn com.bugtags.library.vender.**
-dontwarn com.bugtags.library.**
-dontwarn com.alexvasilkov.android.**

#-dontwarn com.github.hiteshsondhi88.libffmpeg.**
#-keep com.github.hiteshsondhi88.libffmpeg.**{*;}
#-keep android.support.**{*;}
#-keep com.way.captain.widget.**{*;}
#-keep com.getbase.floatingactionbutton.**{*;}
#-keep com.alexvasilkov.**{*;}
