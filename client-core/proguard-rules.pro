# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see:
#   http://developer.android.com/guide/developing/tools/proguard.html
# and: 
#   https://developer.android.com/build/shrink-code#native-crash-support

# More info for debugging
-verbose

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
# We could use this to hide the original source file name.
-renamesourcefileattribute SourceFile

#
-addconfigurationdebugging

## The DB Schema: Room massively use reflection
-keep class org.sinou.pydia.client.core.db.**

# Necessary third parties
# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz
# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep class javax.net.ssl.**

# For preferences see https://medium.com/androiddevelopers/all-about-preferences-datastore-cc7995679334
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Custom signer used by AWS SDK for transfers when the remote is Cells
-keep class org.sinou.pydia.client.core.transfer.CellsSigner { *; }

## Gson specific classes
-dontwarn sun.misc.**
# Prevent stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Application classes that will be serialized/deserialized over Gson
-keep class org.sinou.pydia.sdk.api.ServerURL { <fields>; }
-keep class org.sinou.pydia.sdk.transport.ServerURLImpl { *; }
-keep class org.sinou.pydia.sdk.transport.StateID { <fields>; }
# OAuth credential flow
-keep class org.sinou.pydia.sdk.transport.auth.jwt.Header  { *; }
-keep class org.sinou.pydia.sdk.transport.auth.jwt.Claims  { *; }

# Extra rules added while debuging, might still be refined.
# For GSon see: https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#troubleshooting-gson-gson
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
## This is also necessary as swagger generated code massively use Gson
-keep class org.sinou.pydia.openapi.** { *; }

# Fix crash on release builds when trying to intantiate complex DB objects that use a converter
-keep class org.sinou.pydia.client.core.db.CellsConverters { *; }
-keep class java.util.Properties

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
## End of proguard configuration for Gson
