package com.logicore.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void brlFactoryCreatesBRLMoney() {
        Money m = Money.brl(new BigDecimal("150.50"));
        assertThat(m.currency()).isEqualTo("BRL");
        assertThat(m.amount()).isEqualByComparingTo("150.50");
    }

    @Test
    void brlDoubleFactory() {
        Money m = Money.brl(99.9);
        assertThat(m.currency()).isEqualTo("BRL");
        assertThat(m.amount()).isEqualByComparingTo("99.90");
    }

    @Test
    void zeroReturnsZeroBRL() {
        Money m = Money.zero();
        assertThat(m.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(m.currency()).isEqualTo("BRL");
    }

    @Test
    void addCombinesAmounts() {
        Money a = Money.brl(100.0);
        Money b = Money.brl(50.0);
        assertThat(a.add(b).amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void negativeAmountThrows() {
        assertThatThrownBy(() -> Money.brl(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullAmountThrows() {
        assertThatThrownBy(() -> new Money(null, "BRL"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
