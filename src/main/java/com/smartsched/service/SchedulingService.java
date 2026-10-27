package com.smartsched.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsched.dto.*;
import com.smartsched.entity.*;
import com.smartsched.repository.*;
import com.smartsched.service.algorithm.HillClimbingSchedulingAlgorithm;
import com.smartsched.service.algorithm.SchedulingAlgorithm;
import com.smartsched.service.algorithm.SchedulingProblemHelper;
import com.smartsched.validation.ScheduleValidator;
import com.smartsched.validation.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SchedulingService {

    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final StudentBatchRepository batchRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final AlgorithmRunRepository algorithmRunRepository;

    private final HillClimbingSchedulingAlgorithm hillClimbingAlgorithm;
    private final ScheduleValidator scheduleValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SchedulingService(
            FacultyRepository facultyRepository,
            ClassroomRepository classroomRepository,
            SubjectRepository subjectRepository,
            StudentBatchRepository batchRepository,
            TimeSlotRepository timeSlotRepository,
            ScheduleRepository scheduleRepository,
            ScheduleAssignmentRepository assignmentRepository,
            AlgorithmRunRepository algorithmRunRepository,
            HillClimbingSchedulingAlgorithm hillClimbingAlgorithm,
            ScheduleValidator scheduleValidator) {
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
        this.algorithmRunRepository = algorithmRunRepository;
        this.hillClimbingAlgorithm = hillClimbingAlgorithm;
        this.scheduleValidator = scheduleValidator;
    }

    public SchedulingAlgorithm getAlgorithm(String name) {
        return hillClimbingAlgorithm;
    }

    @Transactional
    public ScheduleResultDto generateSchedule(TimetableGenerationRequest request) {
        List<StudentBatch> batches = batchRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Faculty> faculties = facultyRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByPeriodNumberAsc();

        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(
                request, batches, subjects, faculties, classrooms, timeSlots
        );

        // Record run in database for dashboard history
        saveAlgorithmRun(result);

        // If valid, save schedule automatically
        if ("VALID".equalsIgnoreCase(result.getStatus())) {
            String scheduleType = (request != null && request.getScheduleType() != null) ? request.getScheduleType() : "CLASS";
            Schedule schedule = persistSchedule(result, scheduleType);
            result.setScheduleId(schedule.getId());
        }

        return result;
    }

    @Transactional
    public Schedule persistSchedule(ScheduleResultDto result, String scheduleType) {
        Schedule schedule = new Schedule();
        schedule.setName(result.getName() != null ? result.getName() : "Generated Schedule");
        schedule.setScheduleType(scheduleType != null ? scheduleType : "CLASS");
        schedule.setAlgorithmUsed(result.getAlgorithmName());
        schedule.setStatus(result.getStatus());
        schedule.setHardViolations(result.getHardViolations());
        schedule.setSoftPenalty(result.getSoftPenalty());
        schedule.setFitnessScore(result.getFitnessScore());
        schedule.setExecutionTimeMs(result.getExecutionTimeMs());
        schedule.setExploredStates(result.getExploredStates());
        schedule.setBacktrackingCount(result.getBacktrackingCount());
        schedule.setIterationsOrGenerations(result.getIterationsOrGenerations());
        schedule.setDiagnosticNotes(result.getDiagnosticNotes());
        schedule.setCreatedAt(LocalDateTime.now());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        List<ScheduleAssignment> entities = new ArrayList<>();
        for (ScheduleAssignmentDto dto : result.getAssignments()) {
            ScheduleAssignment sa = new ScheduleAssignment();
            sa.setSchedule(savedSchedule);
            sa.setSubject(subjectRepository.findById(dto.getSubjectId()).orElse(null));
            if (dto.getFacultyId() != null) {
                sa.setFaculty(facultyRepository.findById(dto.getFacultyId()).orElse(null));
            }
            sa.setStudentBatch(batchRepository.findById(dto.getBatchId()).orElse(null));
            sa.setClassroom(classroomRepository.findById(dto.getClassroomId()).orElse(null));
            sa.setTimeSlot(timeSlotRepository.findById(dto.getSlotId()).orElse(null));
            sa.setDurationPeriods(dto.getDurationPeriods());
            sa.setExam(dto.isExam());
            entities.add(sa);
        }

        assignmentRepository.saveAll(entities);
        savedSchedule.setAssignments(entities);
        return savedSchedule;
    }

    @Transactional
    public ScheduleResultDto manualEditAssignment(Long scheduleId, Long assignmentId, ManualEditRequest req) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        ScheduleAssignment targetAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        TimeSlot newSlot = timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found: " + req.getTimeSlotId()));

        Classroom newRoom = classroomRepository.findById(req.getClassroomId())
                .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + req.getClassroomId()));

        // Temporarily change to validate
        TimeSlot oldSlot = targetAssignment.getTimeSlot();
        Classroom oldRoom = targetAssignment.getClassroom();

        targetAssignment.setTimeSlot(newSlot);
        targetAssignment.setClassroom(newRoom);

        List<ScheduleAssignment> currentAssignments = assignmentRepository.findByScheduleId(scheduleId);
        List<StudentBatch> batches = batchRepository.findAll();

        ValidationResult valResult = scheduleValidator.validate(currentAssignments, batches, currentAssignments.size());

        if (!valResult.isValid()) {
            // Revert changes
            targetAssignment.setTimeSlot(oldSlot);
            targetAssignment.setClassroom(oldRoom);

            String firstError = valResult.getViolations().isEmpty() ? "Hard constraint violation" :
                    valResult.getViolations().get(0).getDescription();
            throw new IllegalStateException("Cannot update assignment: " + firstError);
        }

        // Save valid change
        assignmentRepository.save(targetAssignment);
        schedule.setHardViolations(valResult.getHardViolationCount());
        schedule.setSoftPenalty(valResult.getSoftPenalty());
        schedule.setFitnessScore(valResult.getFitnessScore());
        schedule.setStatus(valResult.isValid() ? "VALID" : "INVALID");
        scheduleRepository.save(schedule);

        ScheduleResultDto resDto = new ScheduleResultDto();
        resDto.setScheduleId(schedule.getId());
        resDto.setName(schedule.getName());
        resDto.setAlgorithmName(schedule.getAlgorithmUsed());
        resDto.setStatus(schedule.getStatus());
        resDto.setHardViolations(schedule.getHardViolations());
        resDto.setSoftPenalty(schedule.getSoftPenalty());
        resDto.setFitnessScore(schedule.getFitnessScore());
        resDto.setAssignments(SchedulingProblemHelper.toDtoAssignments(currentAssignments));
        resDto.setViolations(valResult.getViolations());
        resDto.setDiagnosticNotes("Manual update validated and saved successfully.");
        return resDto;
    }

    public String exportScheduleCsv(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<ScheduleAssignment> assignments = assignmentRepository.findByScheduleId(scheduleId);
        assignments.sort(Comparator.comparing((ScheduleAssignment a) -> a.getTimeSlot().getDayOfWeek())
                .thenComparingInt(a -> a.getTimeSlot().getPeriodNumber()));

        StringBuilder csv = new StringBuilder();
        csv.append("Day,Period,Start Time,End Time,Subject Code,Subject Name,Faculty,Batch,Room,Room Type\n");

        for (ScheduleAssignment a : assignments) {
            csv.append(escapeCsv(a.getTimeSlot().getDayOfWeek())).append(",")
                    .append(a.getTimeSlot().getPeriodNumber()).append(",")
                    .append(escapeCsv(a.getTimeSlot().getStartTime())).append(",")
                    .append(escapeCsv(a.getTimeSlot().getEndTime())).append(",")
                    .append(escapeCsv(a.getSubject().getCode())).append(",")
                    .append(escapeCsv(a.getSubject().getName())).append(",")
                    .append(escapeCsv(a.getFaculty() != null ? a.getFaculty().getName() : "N/A")).append(",")
                    .append(escapeCsv(a.getStudentBatch().getBatchCode())).append(",")
                    .append(escapeCsv(a.getClassroom().getRoomNumber())).append(",")
                    .append(escapeCsv(a.getClassroom().getRoomType().name())).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private void saveAlgorithmRun(ScheduleResultDto result) {
        AlgorithmRun run = new AlgorithmRun();
        run.setRunName(result.getName() != null ? result.getName() : "Hill Climbing Run");
        run.setAlgorithmName(result.getAlgorithmName());
        run.setStatus(result.getStatus());
        run.setHardViolations(result.getHardViolations());
        run.setSoftPenalty(result.getSoftPenalty());
        run.setFitnessScore(result.getFitnessScore());
        run.setExecutionTimeMs(result.getExecutionTimeMs());
        run.setExploredStates(result.getExploredStates());
        run.setBacktrackingCount(result.getBacktrackingCount());
        run.setIterationsOrGenerations(result.getIterationsOrGenerations());
        run.setDiagnosticNotes(result.getDiagnosticNotes());
        run.setCreatedAt(LocalDateTime.now());

        if (result.getFitnessHistory() != null && !result.getFitnessHistory().isEmpty()) {
            try {
                run.setFitnessHistoryJson(objectMapper.writeValueAsString(result.getFitnessHistory()));
            } catch (JsonProcessingException ignored) {}
        }

        algorithmRunRepository.save(run);
    }
}
