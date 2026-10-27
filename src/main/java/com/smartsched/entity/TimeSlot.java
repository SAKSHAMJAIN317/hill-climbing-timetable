package com.smartsched.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Day of week is required")
    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY

    @Min(value = 1, message = "Period number must be >= 1")
    @Column(nullable = false)
    private int periodNumber; // 1, 2, 3, 4, 5, 6, 7

    @NotBlank(message = "Start time is required")
    @Column(nullable = false)
    private String startTime; // "09:00"

    @NotBlank(message = "End time is required")
    @Column(nullable = false)
    private String endTime; // "10:00"

    @Column(nullable = false)
    private boolean isBreak = false; // Lunch / recess

    public TimeSlot() {}

    public TimeSlot(String dayOfWeek, int periodNumber, String startTime, String endTime, boolean isBreak) {
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBreak = isBreak;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isBreak() { return isBreak; }
    public void setBreak(boolean aBreak) { isBreak = aBreak; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return periodNumber == timeSlot.periodNumber &&
               Objects.equals(dayOfWeek, timeSlot.dayOfWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, periodNumber);
    }
}
