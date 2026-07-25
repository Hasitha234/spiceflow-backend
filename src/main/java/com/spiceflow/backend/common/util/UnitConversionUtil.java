package com.spiceflow.backend.common.util;

public final class UnitConversionUtil {

    private UnitConversionUtil() {
        // Prevent instantiation
    }

    public static int toEachItems(int quantity, String unitType) {
        switch (unitType != null ? unitType.toUpperCase(java.util.Locale.ROOT) : "EA") {
            case "DZ":
                return quantity * 12;
            case "MC":
                return quantity * 1000;
            default:
                return quantity; // EA, EACH, etc.
        }
    }
}
