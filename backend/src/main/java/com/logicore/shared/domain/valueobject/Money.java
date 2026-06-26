package com.logicore.shared.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) throw new IllegalArgumentException("Valor monetário é obrigatório");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor monetário não pode ser negativo: " + amount);
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Moeda é obrigatória");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase();
    }

    public static Money brl(BigDecimal amount) { return new Money(amount, "BRL"); }
    public static Money brl(double amount) { return new Money(BigDecimal.valueOf(amount), "BRL"); }
    public static Money zero() { return new Money(BigDecimal.ZERO, "BRL"); }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(double factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Moedas incompatíveis: " + this.currency + " vs " + other.currency);
    }
}
