package com.smartsched.repository;

import com.smartsched.entity.ScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, Long> {
    List<ScheduleAssignment> findByScheduleId(Long scheduleId);
    List<ScheduleAssignment> findByScheduleIdAndFacultyId(Long scheduleId, Long facultyId);
    List<ScheduleAssignment> findByScheduleIdAndStudentBatchId(Long scheduleId, Long batchId);
    List<ScheduleAssignment> findByScheduleIdAndClassroomId(Long scheduleId, Long classroomId);
}
