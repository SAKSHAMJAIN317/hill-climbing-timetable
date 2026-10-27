package com.smartsched.repository;

import com.smartsched.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByScheduleTypeOrderByCreatedAtDesc(String scheduleType);
    List<Schedule> findAllByOrderByCreatedAtDesc();
}
