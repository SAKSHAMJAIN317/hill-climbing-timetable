package com.smartsched.validation;

import com.smartsched.entity.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScheduleValidator {

    /**
     * Independently validates an entire schedule against all hard and soft constraints.
     */
    public ValidationResult validate(List<ScheduleAssignment> assignments,
                                     List<StudentBatch> batches,
                                     int expectedTotalSessions) {
        ValidationResult result = new ValidationResult();
        if (assignments == null || assignments.isEmpty()) {
            if (expectedTotalSessions > 0) {
                result.addHardViolation("SESSION_COVERAGE", "Schedule has no assignments, but " + expectedTotalSessions + " sessions were required.");
            }
            result.setFitnessScore(0.0);
            return result;
        }

        // 1. Session Coverage Check
        if (expectedTotalSessions > 0 && assignments.size() < expectedTotalSessions) {
            result.addHardViolation("SESSION_COVERAGE", 
                "Missing sessions: " + assignments.size() + " assigned out of " + expectedTotalSessions + " required.");
        }

        // Maps to track simultaneous bookings
        // Key: Day + "_" + Period
        Map<String, Set<Long>> facultyOccupancy = new HashMap<>();
        Map<String, Set<Long>> batchOccupancy = new HashMap<>();
        Map<String, Set<Long>> roomOccupancy = new HashMap<>();

        // Soft constraint tracking
        // Batch -> Day -> List of Periods
        Map<Long, Map<String, List<Integer>>> batchDayPeriods = new HashMap<>();
        // Faculty -> Day -> List of Periods
        Map<Long, Map<String, List<Integer>>> facultyDayPeriods = new HashMap<>();
        // Faculty -> Total Periods
        Map<Long, Integer> facultyWeeklyLoad = new HashMap<>();
        // Batch -> Day -> List of (Period, RoomId)
        Map<Long, Map<String, List<int[]>>> batchRoomSchedule = new HashMap<>();

        for (ScheduleAssignment a : assignments) {
            Subject subject = a.getSubject();
            Faculty faculty = a.getFaculty();
            StudentBatch batch = a.getStudentBatch();
            Classroom room = a.getClassroom();
            TimeSlot slot = a.getTimeSlot();

            if (subject == null || faculty == null || batch == null || room == null || slot == null) {
                result.addHardViolation("NULL_REFERENCE", "Assignment contains incomplete or null entity references.");
                continue;
            }

            int startPeriod = slot.getPeriodNumber();
            int duration = a.getDurationPeriods() > 0 ? a.getDurationPeriods() : 1;
            String day = slot.getDayOfWeek().toUpperCase();

            for (int p = 0; p < duration; p++) {
                int period = startPeriod + p;
                String slotKey = day + "_" + period;

                // Hard Constraint: Break check
                if (slot.isBreak()) {
                    result.addHardViolation("BREAK_VIOLATION",
                        "Assignment for " + subject.getCode() + " is scheduled during break slot: " + day + " Period " + period);
                }

                // Hard Constraint 1: Faculty Clash
                facultyOccupancy.putIfAbsent(slotKey, new HashSet<>());
                if (!facultyOccupancy.get(slotKey).add(faculty.getId())) {
                    result.addHardViolation("FACULTY_CLASH",
                        "Faculty " + faculty.getName() + " (" + faculty.getFacultyId() + ") has multiple overlapping sessions on " + day + " Period " + period);
                }

                // Hard Constraint 2: Student Batch Clash
                batchOccupancy.putIfAbsent(slotKey, new HashSet<>());
                if (!batchOccupancy.get(slotKey).add(batch.getId())) {
                    result.addHardViolation("BATCH_CLASH",
                        "Batch " + batch.getBatchCode() + " has multiple overlapping classes on " + day + " Period " + period);
                }

                // Hard Constraint 3: Room Clash
                roomOccupancy.putIfAbsent(slotKey, new HashSet<>());
                if (!roomOccupancy.get(slotKey).add(room.getId())) {
                    result.addHardViolation("ROOM_CLASH",
                        "Room " + room.getRoomNumber() + " is double-booked on " + day + " Period " + period);
                }

                // Track for soft constraints
                batchDayPeriods.computeIfAbsent(batch.getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>()).add(period);

                facultyDayPeriods.computeIfAbsent(faculty.getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>()).add(period);

                facultyWeeklyLoad.put(faculty.getId(), facultyWeeklyLoad.getOrDefault(faculty.getId(), 0) + 1);

                batchRoomSchedule.computeIfAbsent(batch.getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>()).add(new int[]{period, room.getId().intValue()});
            }

            // Hard Constraint 4: Room Capacity
            if (room.getCapacity() < batch.getStudentCount()) {
                result.addHardViolation("ROOM_CAPACITY_DEFICIT",
                    "Room " + room.getRoomNumber() + " capacity (" + room.getCapacity() +
                    ") is insufficient for batch " + batch.getBatchCode() + " (" + batch.getStudentCount() + " students).");
            }

            // Hard Constraint 5: Laboratory Compatibility
            if (subject.isPractical() && subject.getRequiredRoomType() != null) {
                if (room.getRoomType() != subject.getRequiredRoomType()) {
                    result.addHardViolation("ROOM_TYPE_INCOMPATIBLE",
                        "Subject " + subject.getCode() + " requires " + subject.getRequiredRoomType() +
                        ", but room " + room.getRoomNumber() + " is " + room.getRoomType());
                }
            }

            // Hard Constraint 6: Faculty Availability
            if (faculty.getAvailableDays() != null && !faculty.getAvailableDays().isEmpty()) {
                List<String> availDays = Arrays.asList(faculty.getAvailableDays().toUpperCase().split("\\s*,\\s*"));
                if (!availDays.contains(day)) {
                    result.addHardViolation("FACULTY_UNAVAILABLE_DAY",
                        "Faculty " + faculty.getName() + " is not available on " + day);
                }
            }
            if (faculty.getAvailableSlots() != null && !faculty.getAvailableSlots().isEmpty()) {
                List<String> availSlots = Arrays.asList(faculty.getAvailableSlots().split("\\s*,\\s*"));
                if (!availSlots.contains(String.valueOf(startPeriod))) {
                    result.addHardViolation("FACULTY_UNAVAILABLE_SLOT",
                        "Faculty " + faculty.getName() + " is not available during period " + startPeriod);
                }
            }

            // Hard Constraint 7: Room Availability
            if (room.getAvailableDays() != null && !room.getAvailableDays().isEmpty()) {
                List<String> availDays = Arrays.asList(room.getAvailableDays().toUpperCase().split("\\s*,\\s*"));
                if (!availDays.contains(day)) {
                    result.addHardViolation("ROOM_UNAVAILABLE_DAY",
                        "Room " + room.getRoomNumber() + " is not available on " + day);
                }
            }

            // Soft Constraint: Faculty Time Preferences
            if (faculty.getPreferredSlots() != null && !faculty.getPreferredSlots().isEmpty()) {
                List<String> prefSlots = Arrays.asList(faculty.getPreferredSlots().split("\\s*,\\s*"));
                if (!prefSlots.contains(String.valueOf(startPeriod))) {
                    result.addSoftViolation("FACULTY_NON_PREFERRED_SLOT",
                        "Faculty " + faculty.getName() + " prefers slots " + faculty.getPreferredSlots() + ", scheduled at " + startPeriod, 5.0);
                }
            }
        }

        // Evaluate Soft Constraints:
        // 1. Idle gaps in student timetable
        for (Map.Entry<Long, Map<String, List<Integer>>> batchEntry : batchDayPeriods.entrySet()) {
            for (Map.Entry<String, List<Integer>> dayEntry : batchEntry.getValue().entrySet()) {
                List<Integer> periods = dayEntry.getValue();
                Collections.sort(periods);
                for (int i = 0; i < periods.size() - 1; i++) {
                    int gap = periods.get(i + 1) - periods.get(i) - 1;
                    if (gap > 0) {
                        result.addSoftViolation("STUDENT_IDLE_GAP",
                            "Batch " + batchEntry.getKey() + " has " + gap + " idle period(s) on " + dayEntry.getKey(), gap * 10.0);
                    }
                }
            }
        }

        // 2. Excessive consecutive faculty teaching (> 3 consecutive periods)
        for (Map.Entry<Long, Map<String, List<Integer>>> facultyEntry : facultyDayPeriods.entrySet()) {
            for (Map.Entry<String, List<Integer>> dayEntry : facultyEntry.getValue().entrySet()) {
                List<Integer> periods = dayEntry.getValue();
                Collections.sort(periods);
                int consecutive = 1;
                for (int i = 0; i < periods.size() - 1; i++) {
                    if (periods.get(i + 1) == periods.get(i) + 1) {
                        consecutive++;
                        if (consecutive > 3) {
                            result.addSoftViolation("EXCESSIVE_CONSECUTIVE_TEACHING",
                                "Faculty " + facultyEntry.getKey() + " teaches " + consecutive + " consecutive periods on " + dayEntry.getKey(), 15.0);
                        }
                    } else {
                        consecutive = 1;
                    }
                }
            }
        }

        // 3. Faculty Daily and Weekly Overload
        for (Map.Entry<Long, Map<String, List<Integer>>> facultyEntry : facultyDayPeriods.entrySet()) {
            for (Map.Entry<String, List<Integer>> dayEntry : facultyEntry.getValue().entrySet()) {
                int dailyLoad = dayEntry.getValue().size();
                if (dailyLoad > 4) { // typical max daily load
                    result.addSoftViolation("FACULTY_DAILY_OVERLOAD",
                        "Faculty " + facultyEntry.getKey() + " has " + dailyLoad + " periods on " + dayEntry.getKey() + " (max 4 recommended)", (dailyLoad - 4) * 12.0);
                }
            }
        }

        // 4. Unnecessary room changes for batch on same day
        for (Map.Entry<Long, Map<String, List<int[]>>> batchEntry : batchRoomSchedule.entrySet()) {
            for (Map.Entry<String, List<int[]>> dayEntry : batchEntry.getValue().entrySet()) {
                List<int[]> sessions = dayEntry.getValue();
                sessions.sort(Comparator.comparingInt(a -> a[0]));
                for (int i = 0; i < sessions.size() - 1; i++) {
                    if (sessions.get(i + 1)[0] == sessions.get(i)[0] + 1) { // consecutive period
                        if (sessions.get(i + 1)[1] != sessions.get(i)[1]) {
                            result.addSoftViolation("ROOM_CHANGE_FATIGUE",
                                "Batch " + batchEntry.getKey() + " switches rooms consecutively on " + dayEntry.getKey(), 4.0);
                        }
                    }
                }
            }
        }

        // Compute overall fitness score as a normalized percentage (0.0% to 100.0%):
        // Hard violations heavily penalize (1000 pts each), Soft penalties subtract proportionately
        double hardPenaltyTotal = result.getHardViolationCount() * 1000.0;
        double totalPenalty = hardPenaltyTotal + result.getSoftPenalty();
        double fitness = 100.0 / (1.0 + 0.005 * totalPenalty);
        if (result.getHardViolationCount() > 0) {
            // Cap fitness of invalid schedules strictly below 35%
            fitness = Math.min(35.0, fitness);
        }
        result.setFitnessScore(Math.round(fitness * 100.0) / 100.0);

        return result;
    }
}
