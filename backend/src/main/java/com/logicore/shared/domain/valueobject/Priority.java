package com.logicore.shared.domain.valueobject;

public record Priority(int value) {
    public Priority {
        if (value < 1 || value > 5)
            throw new IllegalArgumentException("Prioridade deve estar entre 1 e 5, recebido: " + value);
    }

    public static Priority low()     { return new Priority(1); }
    public static Priority normal()  { return new Priority(3); }
    public static Priority critical(){ return new Priority(5); }

    public boolean isHigherThan(Priority other) { return this.value > other.value; }
    public boolean isCritical() { return value == 5; }
}
