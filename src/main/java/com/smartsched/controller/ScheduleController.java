package com.smartsched.controller;

import com.smartsched.dto.ManualEditRequest;
import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.dto.TimetableGenerationRequest;
import com.smartsched.entity.Schedule;
import com.smartsched.entity.ScheduleAssignment;
import com.smartsched.repository.ScheduleAssignmentRepository;
import com.smartsched.repository.ScheduleRepository;
import com.smartsched.service.SchedulingService;
import com.smartsched.service.algorithm.SchedulingProblemHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final SchedulingService schedulingService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentRepository assignmentRepository;

    public ScheduleController(
            SchedulingService schedulingService,
            ScheduleRepository scheduleRepository,
            ScheduleAssignmentRepository assignmentRepository) {
        this.schedulingService = schedulingService;
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<ScheduleResultDto> generateSchedule(@RequestBody(required = false) TimetableGenerationRequest request) {
        if (request == null) {
            request = new TimetableGenerationRequest();
        }
        ScheduleResultDto result = schedulingService.generateSchedule(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<Schedule> getAllSchedules(@RequestParam(required = false) String type) {
        if (type != null && !type.trim().isEmpty()) {
            return scheduleRepository.findByScheduleTypeOrderByCreatedAtDesc(type.toUpperCase());
        }
        return scheduleRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResultDto> getScheduleById(@PathVariable Long id) {
        return scheduleRepository.findById(id).map(s -> {
            List<ScheduleAssignment> assignments = assignmentRepository.findByScheduleId(id);
            ScheduleResultDto dto = new ScheduleResultDto();
            dto.setScheduleId(s.getId());
            dto.setName(s.getName());
            dto.setAlgorithmName(s.getAlgorithmUsed());
            dto.setStatus(s.getStatus());
            dto.setHardViolations(s.getHardViolations());
            dto.setSoftPenalty(s.getSoftPenalty());
            dto.setFitnessScore(s.getFitnessScore());
            dto.setExecutionTimeMs(s.getExecutionTimeMs());
            dto.setExploredStates(s.getExploredStates());
            dto.setBacktrackingCount(s.getBacktrackingCount());
            dto.setIterationsOrGenerations(s.getIterationsOrGenerations());
            dto.setDiagnosticNotes(s.getDiagnosticNotes());
            dto.setAssignments(SchedulingProblemHelper.toDtoAssignments(assignments));
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assignments/{assignmentId}")
    public ResponseEntity<ScheduleResultDto> updateAssignment(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @Valid @RequestBody ManualEditRequest request) {
        ScheduleResultDto updated = schedulingService.manualEditAssignment(id, assignmentId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping(value = "/{id}/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(@PathVariable Long id) {
        String csv = schedulingService.exportScheduleCsv(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schedule-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
