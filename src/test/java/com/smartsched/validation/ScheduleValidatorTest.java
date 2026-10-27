package com.smartsched.validation;

import com.smartsched.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleValidatorTest {

    private ScheduleValidator validator;
    private Faculty faculty1;
    private Faculty faculty2;
    private Classroom room1;
    private Classroom lab1;
    private Subject subject1;
    private Subject labSubject;
    private StudentBatch batch1;
    private StudentBatch batch2;
    private TimeSlot slot1;
    private TimeSlot slot2;

    @BeforeEach
    void setUp() {
        validator = new ScheduleValidator();

        faculty1 = new Faculty("F1", "Dr. Alan Turing", "CSE", 4, 18, "MONDAY,TUESDAY", "1,2,3", "1");
        faculty1.setId(1L);

        faculty2 = new Faculty("F2", "Dr. Grace Hopper", "CSE", 4, 18, "MONDAY,TUESDAY", "1,2,3", "2");
        faculty2.setId(2L);

        room1 = new Classroom("101", "Lecture Room 101", 50, RoomType.CLASSROOM, "MONDAY,TUESDAY");
        room1.setId(10L);

        lab1 = new Classroom("LAB-1", "Computing Lab", 40, RoomType.COMPUTER_LAB, "MONDAY,TUESDAY");
        lab1.setId(20L);

        subject1 = new Subject("CS101", "Programming", "CSE", 1, 3, 1, false, RoomType.CLASSROOM, faculty1);
        subject1.setId(100L);

        labSubject = new Subject("CS102", "Programming Lab", "CSE", 1, 2, 2, true, RoomType.COMPUTER_LAB, faculty2);
        labSubject.setId(101L);

        batch1 = new StudentBatch("CSE-A", "CSE", 1, "A", 45, List.of(subject1));
        batch1.setId(1000L);

        batch2 = new StudentBatch("CSE-B", "CSE", 1, "B", 35, List.of(subject1));
        batch2.setId(1001L);

        slot1 = new TimeSlot("MONDAY", 1, "09:00", "10:00", false);
        slot1.setId(500L);

        slot2 = new TimeSlot("MONDAY", 2, "10:00", "11:00", false);
        slot2.setId(501L);
    }

    @Test
    @DisplayName("Valid schedule should have 0 hard violations and be marked VALID")
    void testValidSchedule() {
        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, slot1, 1, false);
        ScheduleAssignment a2 = new ScheduleAssignment(null, subject1, faculty2, batch2, room1, slot2, 1, false);

        ValidationResult result = validator.validate(List.of(a1, a2), List.of(batch1, batch2), 2);
        assertTrue(result.isValid(), "Schedule should be valid");
        assertEquals(0, result.getHardViolationCount());
        assertTrue(result.getFitnessScore() > 80.0);
    }

    @Test
    @DisplayName("Faculty double-booking must be detected as a hard violation")
    void testFacultyClash() {
        // Faculty1 assigned to two different batches at the exact same time slot
        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, slot1, 1, false);
        ScheduleAssignment a2 = new ScheduleAssignment(null, subject1, faculty1, batch2, lab1, slot1, 1, false);

        ValidationResult result = validator.validate(List.of(a1, a2), List.of(batch1, batch2), 2);
        assertFalse(result.isValid());
        assertTrue(result.getHardViolationCount() >= 1);
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "FACULTY_CLASH".equals(v.getRuleName()));
        assertTrue(found, "FACULTY_CLASH rule must trigger");
    }

    @Test
    @DisplayName("Room double-booking must be detected as a hard violation")
    void testRoomClash() {
        // Same room assigned to two different faculties & batches at slot1
        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, slot1, 1, false);
        ScheduleAssignment a2 = new ScheduleAssignment(null, subject1, faculty2, batch2, room1, slot1, 1, false);

        ValidationResult result = validator.validate(List.of(a1, a2), List.of(batch1, batch2), 2);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "ROOM_CLASH".equals(v.getRuleName()));
        assertTrue(found, "ROOM_CLASH rule must trigger");
    }

    @Test
    @DisplayName("Student batch clash must be detected as a hard violation")
    void testBatchClash() {
        // Same batch assigned to two different lectures at slot1
        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, slot1, 1, false);
        ScheduleAssignment a2 = new ScheduleAssignment(null, labSubject, faculty2, batch1, lab1, slot1, 1, false);

        ValidationResult result = validator.validate(List.of(a1, a2), List.of(batch1), 2);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "BATCH_CLASH".equals(v.getRuleName()));
        assertTrue(found, "BATCH_CLASH rule must trigger");
    }

    @Test
    @DisplayName("Room capacity deficit must trigger hard violation")
    void testRoomCapacityDeficit() {
        // Batch student count is 45, but room capacity is 30
        Classroom smallRoom = new Classroom("Small-1", "Small Room", 30, RoomType.CLASSROOM, "MONDAY,TUESDAY");
        smallRoom.setId(99L);

        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, smallRoom, slot1, 1, false);
        ValidationResult result = validator.validate(List.of(a1), List.of(batch1), 1);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "ROOM_CAPACITY_DEFICIT".equals(v.getRuleName()));
        assertTrue(found, "ROOM_CAPACITY_DEFICIT must trigger");
    }

    @Test
    @DisplayName("Laboratory compatibility violation must trigger hard violation")
    void testLabCompatibility() {
        // labSubject requires COMPUTER_LAB, but scheduled in normal CLASSROOM
        ScheduleAssignment a1 = new ScheduleAssignment(null, labSubject, faculty2, batch2, room1, slot1, 1, false);
        ValidationResult result = validator.validate(List.of(a1), List.of(batch2), 1);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "ROOM_TYPE_INCOMPATIBLE".equals(v.getRuleName()));
        assertTrue(found, "ROOM_TYPE_INCOMPATIBLE must trigger");
    }

    @Test
    @DisplayName("Faculty unavailability day/slot must trigger hard violation")
    void testFacultyUnavailability() {
        // faculty1 is only available on MONDAY and TUESDAY
        TimeSlot wedSlot = new TimeSlot("WEDNESDAY", 1, "09:00", "10:00", false);
        wedSlot.setId(700L);

        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, wedSlot, 1, false);
        ValidationResult result = validator.validate(List.of(a1), List.of(batch1), 1);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "FACULTY_UNAVAILABLE_DAY".equals(v.getRuleName()));
        assertTrue(found, "FACULTY_UNAVAILABLE_DAY must trigger");
    }

    @Test
    @DisplayName("Incomplete session coverage must trigger hard violation")
    void testSessionCoverage() {
        ScheduleAssignment a1 = new ScheduleAssignment(null, subject1, faculty1, batch1, room1, slot1, 1, false);
        // Expected 5 sessions, but only 1 assigned
        ValidationResult result = validator.validate(List.of(a1), List.of(batch1), 5);
        assertFalse(result.isValid());
        boolean found = result.getViolations().stream()
                .anyMatch(v -> "SESSION_COVERAGE".equals(v.getRuleName()));
        assertTrue(found, "SESSION_COVERAGE must trigger");
    }
}
