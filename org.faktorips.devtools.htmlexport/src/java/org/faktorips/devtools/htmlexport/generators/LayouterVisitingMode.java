package org.faktorips.devtools.htmlexport.generators;

public enum LayouterVisitingMode {
    INIT, FINALIZE, COMPLETE;
    
    public static boolean isInitiating(LayouterVisitingMode mode) {
        if (mode == FINALIZE) return false;
        return true;
    }

    public static boolean isFinalizing(LayouterVisitingMode mode) {
        if (mode == INIT) return false;
        return true;
    }
}
