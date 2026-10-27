package com.smartsched.controller;

import com.smartsched.dto.DashboardSummaryDto;
import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.entity.AlgorithmRun;
import com.smartsched.entity.Schedule;
import com.smartsched.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final StudentBatchRepository batchRepository;
    private final ClassroomRepository classroomRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final AlgorithmRunRepository algorithmRunRepository;

    public DashboardController(
            FacultyRepository facultyRepository,
            SubjectRepository subjectRepository,
            StudentBatchRepository batchRepository,
            ClassroomRepository classroomRepository,
            ScheduleAssignmentRepository assignmentRepository,
            ScheduleRepository scheduleRepository,
            AlgorithmRunRepository algorithmRunRepository) {
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.classroomRepository = classroomRepository;
        this.assignmentRepository = assignmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.algorithmRunRepository = algorithmRunRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalFaculty(facultyRepository.count());
        summary.setTotalSubjects(subjectRepository.count());
        summary.setTotalBatches(batchRepository.count());
        summary.setTotalRooms(classroomRepository.count());
        summary.setTotalScheduledClasses(assignmentRepository.count());

        List<Schedule> latestSchedules = scheduleRepository.findAllByOrderByCreatedAtDesc();
        if (!latestSchedules.isEmpty()) {
            Schedule latest = latestSchedules.get(0);
            summary.setCurrentTimetableStatus(latest.getStatus());
            summary.setHardViolations(latest.getHardViolations());
            summary.setSoftPenalty(latest.getSoftPenalty());
            summary.setScheduleQualityPercent(latest.getFitnessScore());
        } else {
            summary.setCurrentTimetableStatus("NOT_GENERATED");
            summary.setHardViolations(0);
            summary.setSoftPenalty(0.0);
            summary.setScheduleQualityPercent(0.0);
        }

        List<AlgorithmRun> recentRuns = algorithmRunRepository.findTop10ByOrderByCreatedAtDesc();
        List<ScheduleResultDto> runDtos = new ArrayList<>();
        for (AlgorithmRun r : recentRuns) {
            ScheduleResultDto dto = new ScheduleResultDto();
            dto.setName(r.getRunName());
            dto.setAlgorithmName(r.getAlgorithmName());
            dto.setStatus(r.getStatus());
            dto.setHardViolations(r.getHardViolations());
            dto.setSoftPenalty(r.getSoftPenalty());
            dto.setFitnessScore(r.getFitnessScore());
            dto.setExecutionTimeMs(r.getExecutionTimeMs());
            dto.setExploredStates(r.getExploredStates());
            dto.setBacktrackingCount(r.getBacktrackingCount());
            dto.setIterationsOrGenerations(r.getIterationsOrGenerations());
            runDtos.add(dto);
        }
        summary.setRecentRuns(runDtos);

        return ResponseEntity.ok(summary);
    }
}
