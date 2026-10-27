package com.smartsched.repository;

import com.smartsched.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByIsBreakFalseOrderByPeriodNumberAsc();
    List<TimeSlot> findAllByOrderByPeriodNumberAsc();
    Optional<TimeSlot> findByDayOfWeekAndPeriodNumber(String dayOfWeek, int periodNumber);
}
