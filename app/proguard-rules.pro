# Project-specific R8 rules. The libraries in use (Hilt, Navigation, Parcelize,
# DataStore) ship their own consumer rules, and nothing here uses reflection, so little is
# needed.

# Keep line numbers so stack traces from Play vitals can be mapped back with the uploaded
# mapping file, but hide the original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Debug and info logging is for development only; release builds drop the calls entirely.
# Warnings and errors stay.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
