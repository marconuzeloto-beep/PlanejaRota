package com.logicore.shared.domain.valueobject;

import com.logicore.shared.domain.exception.BusinessRuleException;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public record TimeWindow(LocalTime openTime, LocalTime closeTime) {

    public TimeWindow {
        if (openTime == null || closeTime == null)
            throw new BusinessRuleException("Horários da janela de tempo são obrigatórios");
        if (!openTime.isBefore(closeTime))
            throw new BusinessRuleException("Horário de abertura deve ser anterior ao fechamento: "
                    + openTime + " >= " + closeTime);
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    public boolean overlaps(TimeWindow other) {
        return openTime.isBefore(other.closeTime) && other.openTime.isBefore(closeTime);
    }

    public long durationMinutes() {
        return openTime.until(closeTime, ChronoUnit.MINUTES);
    }
}
