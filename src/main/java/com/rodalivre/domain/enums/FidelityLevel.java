package com.rodalivre.domain.enums;

public enum FidelityLevel {
    BRONZE(0, 0.0, "Bronze"),
    SILVER(500, 0.05, "Prata"),
    GOLD(1000, 0.10, "Ouro"),
    DIAMOND(2000, 0.15, "Diamante");

    private final int minPoints;
    private final double discountRate;
    private final String displayName;

    FidelityLevel(int minPoints, double discountRate, String displayName) {
        this.minPoints = minPoints;
        this.discountRate = discountRate;
        this.displayName = displayName;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FidelityLevel fromPoints(int points) {
        if (points >= DIAMOND.minPoints) return DIAMOND;
        if (points >= GOLD.minPoints) return GOLD;
        if (points >= SILVER.minPoints) return SILVER;
        return BRONZE;
    }
}
