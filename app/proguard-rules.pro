# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Room entities
-keep class com.app.quizgen.data.local.entity.** { *; }
-keep class com.app.quizgen.data.local.relation.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
