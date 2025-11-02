package org.craftamethyst.tritium.util;

/**
 * Coordinates language switching optimization between mixins.
 */
public class LanguageLoadOptimizer {
    
    private static volatile boolean isLanguageChanging = false;
    
    public static void setLanguageChanging(boolean changing) {
        isLanguageChanging = changing;
    }
    
    public static boolean isLanguageChanging() {
        return isLanguageChanging;
    }
    
    public static void reset() {
        isLanguageChanging = false;
    }
}
