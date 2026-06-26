package com.planejarota.domain.valueobject;

/**
 * Peso de carga em quilogramas. Garante que o valor nunca seja negativo.
 * Uso de record evita a necessidade de equals/hashCode manual.
 */
public record Weight(double kilograms) {

    public Weight {
        if (kilograms < 0) throw new IllegalArgumentException("Peso não pode ser negativo: " + kilograms);
    }

    public static Weight zero() {
        return new Weight(0);
    }

    public Weight add(Weight other) {
        return new Weight(this.kilograms + other.kilograms);
    }

    public boolean exceeds(Weight limit) {
        return this.kilograms > limit.kilograms;
    }
}
