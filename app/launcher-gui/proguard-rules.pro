### Warning suppression rules

# Logback
-dontwarn jakarta.servlet.**
-dontwarn org.jline.jansi.**
-dontwarn jakarta.mail.**
-dontwarn org.tukaani.xz.**

# Metro/Circuit
-dontwarn com.slack.circuit.codegen.annotations.**
-dontwarn software.amazon.lastmile.kotlin.inject.anvil.**
-dontwarn dagger.hilt.**

# Cryptography
-dontwarn org.bouncycastle.**

# OSHI
-dontwarn oshi.jna.platform.mac.**

# Filekit
-dontwarn io.github.vinceglb.filekit.dialogs.compose.util.ImageBitmapExt_nonAndroidKt*

### Keep rules

# Filekit
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keep class org.freedesktop.dbus.** { *; }
-keep class io.github.vinceglb.filekit.dialogs.platform.xdg.** { *; }
-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations

# Coil
-keep class coil3.util.DecoderServiceLoaderTarget { *; }
-keep class coil3.util.FetcherServiceLoaderTarget { *; }
-keep class coil3.util.ServiceLoaderComponentRegistry { *; }
-keep class * implements coil3.util.DecoderServiceLoaderTarget { *; }
-keep class * implements coil3.util.FetcherServiceLoaderTarget { *; }
# Okio for Coil cache
-keep,allowshrinking class okio.Okio { *; }

# Compose
-keep,allowshrinking class androidx.** { *; }

# Logback
-keep class ch.qos.logback.** { *; }

# Ktor
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }

# Cryptography
-keep class * implements dev.whyoleg.cryptography.CryptographyProviderContainer { *; }

# Nucleus updater (kotlinx-serialization DTOs for the GitHub/yml feeds)
-keep class dev.nucleusframework.updater.** { *; }
