package com.planejarota.domain.valueobject;

import com.planejarota.domain.exception.InvalidTimeWindowException;

import java.time.LocalTime;

/**
 * Representa a janela de tempo aceitável para uma entrega.
 *
 * Restrição de negócio: cliente só aceita entrega entre openTime e closeTime.
 * O algoritmo de otimização deve respeitar esta restrição como constraint hard.
 */
public record TimeWindow(LocalTime openTime, LocalTime closeTime) {

    public TimeWindow {
        if (openTime == null || closeTime == null) {
            throw new InvalidTimeWindowException("Horários de abertura e fechamento são obrigatórios");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new InvalidTimeWindowException(
                "Horário de abertura deve ser anterior ao fechamento: " + openTime + " >= " + closeTime
            );
        }
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    public boolean overlaps(TimeWindow other) {
        return this.openTime.isBefore(other.closeTime) && other.openTime.isBefore(this.closeTime);
    }

    public long durationInMinutes() {
        return openTime.until(closeTime, java.time.temporal.ChronoUnit.MINUTES);
    }
}
