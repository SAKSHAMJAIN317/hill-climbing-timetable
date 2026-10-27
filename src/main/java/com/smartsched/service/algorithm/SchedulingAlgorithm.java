package com.smartsched.service.algorithm;

import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.dto.TimetableGenerationRequest;
import com.smartsched.entity.*;
import java.util.List;

public interface SchedulingAlgorithm {
    String getAlgorithmName();

    ScheduleResultDto generateSchedule(
        TimetableGenerationRequest request,
        List<StudentBatch> batches,
        List<Subject> subjects,
        List<Faculty> faculties,
        List<Classroom> classrooms,
        List<TimeSlot> timeSlots
    );
}
