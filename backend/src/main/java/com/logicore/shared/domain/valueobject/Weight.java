package com.logicore.shared.domain.valueobject;

public record Weight(double kilograms) {
    public Weight {
        if (kilograms < 0)
            throw new IllegalArgumentException("Peso não pode ser negativo: " + kilograms);
    }

    public static Weight zero() { return new Weight(0); }

    public Weight add(Weight other) { return new Weight(this.kilograms + other.kilograms); }

    public boolean exceeds(Weight limit) { return this.kilograms > limit.kilograms; }

    public double percentOf(Weight total) {
        if (total.kilograms == 0) return 0;
        return (this.kilograms / total.kilograms) * 100.0;
    }
}
