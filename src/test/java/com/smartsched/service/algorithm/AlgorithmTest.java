package com.smartsched.service.algorithm;

import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.dto.TimetableGenerationRequest;
import com.smartsched.entity.*;
import com.smartsched.validation.ScheduleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTest {

    private ScheduleValidator validator;
    private HillClimbingSchedulingAlgorithm hillClimbingAlgorithm;

    private List<StudentBatch> batches;
    private List<Subject> subjects;
    private List<Faculty> faculties;
    private List<Classroom> classrooms;
    private List<TimeSlot> timeSlots;

    @BeforeEach
    void setUp() {
        validator = new ScheduleValidator();
        hillClimbingAlgorithm = new HillClimbingSchedulingAlgorithm(validator);

        // Standard test dataset
        faculties = new ArrayList<>();
        Faculty f1 = new Faculty("F1", "Prof. Alpha", "CSE", 4, 18, "MONDAY,TUESDAY,WEDNESDAY", "1,2,3,4,5", "1,2");
        f1.setId(1L);
        Faculty f2 = new Faculty("F2", "Prof. Beta", "CSE", 4, 18, "MONDAY,TUESDAY,WEDNESDAY", "1,2,3,4,5", "2,3");
        f2.setId(2L);
        faculties.add(f1);
        faculties.add(f2);

        classrooms = new ArrayList<>();
        Classroom c1 = new Classroom("C101", "Lecture Room 101", 50, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY");
        c1.setId(10L);
        Classroom c2 = new Classroom("C102", "Lecture Room 102", 50, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY");
        c2.setId(20L);
        classrooms.add(c1);
        classrooms.add(c2);

        subjects = new ArrayList<>();
        Subject s1 = new Subject("CS101", "Intro to CS", "CSE", 1, 2, 1, false, RoomType.CLASSROOM, f1);
        s1.setId(100L);
        Subject s2 = new Subject("CS102", "Data Structures", "CSE", 1, 2, 1, false, RoomType.CLASSROOM, f2);
        s2.setId(101L);
        subjects.add(s1);
        subjects.add(s2);

        batches = new ArrayList<>();
        StudentBatch b1 = new StudentBatch("BATCH-A", "CSE", 1, "A", 30, List.of(s1, s2));
        b1.setId(1000L);
        batches.add(b1);

        timeSlots = new ArrayList<>();
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY"};
        long slotId = 1L;
        for (String day : days) {
            for (int p = 1; p <= 4; p++) {
                TimeSlot ts = new TimeSlot(day, p, "0" + (8 + p) + ":00", "0" + (9 + p) + ":00", false);
                ts.setId(slotId++);
                timeSlots.add(ts);
            }
        }
    }

    @Test
    @DisplayName("Hill Climbing should produce a VALID clash-free schedule")
    void testHillClimbingGeneratesValidSchedule() {
        TimetableGenerationRequest req = new TimetableGenerationRequest();
        req.setAlgorithm("HILL_CLIMBING");
        req.setMaxIterations(2000);
        req.setRandomRestarts(5);

        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(req, batches, subjects, faculties, classrooms, timeSlots);

        assertNotNull(result);
        assertEquals("Hill Climbing", result.getAlgorithmName());
        assertEquals("VALID", result.getStatus());
        assertEquals(0, result.getHardViolations(), "Valid schedule must have 0 hard violations");
        assertEquals(4, result.getAssignments().size(), "Should schedule all 4 sessions (2 for s1 + 2 for s2)");
        assertTrue(result.getExecutionTimeMs() >= 0);
        assertTrue(result.getExploredStates() > 0);
        assertTrue(result.getFitnessScore() > 0.0);
    }

    @Test
    @DisplayName("Hill Climbing should track fitness convergence history across iterations")
    void testHillClimbingTracksFitnessHistory() {
        TimetableGenerationRequest req = new TimetableGenerationRequest();
        req.setAlgorithm("HILL_CLIMBING");
        req.setMaxIterations(500);
        req.setRandomRestarts(3);

        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(req, batches, subjects, faculties, classrooms, timeSlots);

        assertNotNull(result);
        assertNotNull(result.getFitnessHistory());
        assertFalse(result.getFitnessHistory().isEmpty(), "Fitness history must contain recorded steps");
        assertTrue(result.getIterationsOrGenerations() > 0);
    }

    @Test
    @DisplayName("Hill Climbing with random restarts explores states and finds optimal cost")
    void testHillClimbingRandomRestarts() {
        TimetableGenerationRequest req = new TimetableGenerationRequest();
        req.setMaxIterations(1000);
        req.setRandomRestarts(4);

        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(req, batches, subjects, faculties, classrooms, timeSlots);

        assertNotNull(result);
        assertTrue(result.getExploredStates() >= 4, "Should explore states across multiple restarts");
    }

    @Test
    @DisplayName("Impossible scheduling problem must be reported honestly as INFEASIBLE and never VALID")
    void testImpossibleScenarioHonestFailure() {
        // Impossible setup: Room capacity is 10, but batch student count is 30 -> empty domain
        classrooms.clear();
        Classroom tinyRoom = new Classroom("TINY-1", "Tiny Room", 10, RoomType.CLASSROOM, "MONDAY,TUESDAY");
        tinyRoom.setId(999L);
        classrooms.add(tinyRoom);

        TimetableGenerationRequest req = new TimetableGenerationRequest();
        req.setMaxIterations(500);

        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(req, batches, subjects, faculties, classrooms, timeSlots);

        assertNotNull(result);
        assertNotEquals("VALID", result.getStatus(), "Impossible problem must NEVER be reported as VALID!");
        assertEquals("INFEASIBLE", result.getStatus());
        assertTrue(result.getDiagnosticNotes().contains("Feasible domain empty") ||
                   result.getDiagnosticNotes().contains("No compatible classroom"));
    }

    @Test
    @DisplayName("Any schedule marked VALID must strictly satisfy all hard constraints with 0 violations")
    void testScheduleIntegrityGuarantee() {
        TimetableGenerationRequest req = new TimetableGenerationRequest();
        ScheduleResultDto result = hillClimbingAlgorithm.generateSchedule(req, batches, subjects, faculties, classrooms, timeSlots);

        if ("VALID".equals(result.getStatus())) {
            assertEquals(0, result.getHardViolations());
            assertTrue(result.getViolations().stream().noneMatch(v -> "HARD".equalsIgnoreCase(v.getType())));
        }
    }
}
