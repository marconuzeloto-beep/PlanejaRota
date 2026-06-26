package com.logicore.shared.domain.valueobject;

import com.logicore.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class TimeWindowTest {

    @Test
    void validWindowIsCreated() {
        TimeWindow window = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0));
        assertThat(window.openTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(window.closeTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void openEqualToCloseThrows() {
        assertThatThrownBy(() -> new TimeWindow(LocalTime.of(10, 0), LocalTime.of(10, 0)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void openAfterCloseThrows() {
        assertThatThrownBy(() -> new TimeWindow(LocalTime.of(18, 0), LocalTime.of(8, 0)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void containsTimeInsideWindow() {
        TimeWindow window = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0));
        assertThat(window.contains(LocalTime.of(12, 0))).isTrue();
        assertThat(window.contains(LocalTime.of(8, 0))).isTrue();
        assertThat(window.contains(LocalTime.of(18, 0))).isTrue();
    }

    @Test
    void containsTimeOutsideWindow() {
        TimeWindow window = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0));
        assertThat(window.contains(LocalTime.of(7, 59))).isFalse();
        assertThat(window.contains(LocalTime.of(18, 1))).isFalse();
    }

    @Test
    void overlapsReturnsTrueWhenWindowsOverlap() {
        TimeWindow a = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(12, 0));
        TimeWindow b = new TimeWindow(LocalTime.of(10, 0), LocalTime.of(14, 0));
        assertThat(a.overlaps(b)).isTrue();
        assertThat(b.overlaps(a)).isTrue();
    }

    @Test
    void overlapsReturnsFalseWhenWindowsAdjacent() {
        TimeWindow a = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(10, 0));
        TimeWindow b = new TimeWindow(LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertThat(a.overlaps(b)).isFalse();
    }
}
