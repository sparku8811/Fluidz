# R8 rules for Fluidz

# Fix for missing AutoValue
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$Builder

# Fix for Project Reactor / BlockHound (often used in transitive deps)
-dontwarn reactor.blockhound.**
-dontwarn reactor.core.scheduler.ReactorBlockHoundIntegration

# General Compose Multiplatform rules
-keepattributes Signature
-keepattributes AnnotationDefault
-keepattributes *Annotation*
-keep class androidx.compose.runtime.ParcelableSnapshotState$Companion { *; }

# MSAL and Microsoft Graph often need these
-keep class com.microsoft.identity.client.** { *; }
-keep interface com.microsoft.identity.client.** { *; }
-dontwarn com.microsoft.identity.client.**
