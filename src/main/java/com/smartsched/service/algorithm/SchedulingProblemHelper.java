package com.smartsched.service.algorithm;

import com.smartsched.dto.ScheduleAssignmentDto;
import com.smartsched.entity.*;
import java.util.*;

public class SchedulingProblemHelper {

    public static List<SchedulingVariable> buildVariables(List<StudentBatch> batches) {
        List<SchedulingVariable> variables = new ArrayList<>();
        int idCounter = 1;

        for (StudentBatch batch : batches) {
            for (Subject subject : batch.getSubjects()) {
                Faculty faculty = subject.getAssignedFaculty();
                int weeklyLectures = subject.getWeeklyLectures();
                int duration = subject.getLectureDuration();
                boolean isPractical = subject.isPractical();
                RoomType roomType = subject.getRequiredRoomType();

                for (int s = 1; s <= weeklyLectures; s++) {
                    variables.add(new SchedulingVariable(
                        idCounter++, subject, batch, faculty, s, duration, isPractical, roomType
                    ));
                }
            }
        }
        return variables;
    }

    public static List<SchedulingPlacement> computeFeasibleDomain(
            SchedulingVariable var,
            List<Classroom> classrooms,
            List<TimeSlot> timeSlots) {

        List<SchedulingPlacement> domain = new ArrayList<>();
        // Group timeSlots by day
        Map<String, List<TimeSlot>> slotsByDay = new HashMap<>();
        for (TimeSlot ts : timeSlots) {
            if (!ts.isBreak()) {
                slotsByDay.computeIfAbsent(ts.getDayOfWeek().toUpperCase(), k -> new ArrayList<>()).add(ts);
            }
        }
        for (List<TimeSlot> list : slotsByDay.values()) {
            list.sort(Comparator.comparingInt(TimeSlot::getPeriodNumber));
        }

        Faculty faculty = var.getFaculty();
        List<String> facultyDays = parseList(faculty != null ? faculty.getAvailableDays() : null);
        List<String> facultySlots = parseList(faculty != null ? faculty.getAvailableSlots() : null);

        for (Classroom room : classrooms) {
            // Capacity check
            if (room.getCapacity() < var.getBatch().getStudentCount()) {
                continue;
            }
            // Room type check
            if (var.isPractical() && var.getRequiredRoomType() != null) {
                if (room.getRoomType() != var.getRequiredRoomType()) {
                    continue;
                }
            } else if (!var.isPractical() && room.getRoomType() != RoomType.CLASSROOM) {
                // Regular lecture prefers normal classroom
                continue;
            }

            List<String> roomDays = parseList(room.getAvailableDays());

            for (Map.Entry<String, List<TimeSlot>> entry : slotsByDay.entrySet()) {
                String day = entry.getKey();
                if (!roomDays.isEmpty() && !roomDays.contains(day)) {
                    continue;
                }
                if (!facultyDays.isEmpty() && !facultyDays.contains(day)) {
                    continue;
                }

                List<TimeSlot> daySlots = entry.getValue();
                for (int i = 0; i < daySlots.size(); i++) {
                    TimeSlot firstSlot = daySlots.get(i);

                    // Check faculty slot availability
                    if (!facultySlots.isEmpty() && !facultySlots.contains(String.valueOf(firstSlot.getPeriodNumber()))) {
                        continue;
                    }

                    // Check consecutive slots if duration > 1
                    if (var.getDuration() > 1) {
                        boolean validConsecutive = true;
                        int expectedPeriod = firstSlot.getPeriodNumber();
                        for (int d = 0; d < var.getDuration(); d++) {
                            int targetPeriod = expectedPeriod + d;
                            boolean found = false;
                            for (TimeSlot s : daySlots) {
                                if (s.getPeriodNumber() == targetPeriod && !s.isBreak()) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                validConsecutive = false;
                                break;
                            }
                        }
                        if (!validConsecutive) continue;
                    }

                    domain.add(new SchedulingPlacement(firstSlot, room));
                }
            }
        }

        return domain;
    }

    private static List<String> parseList(String str) {
        if (str == null || str.trim().isEmpty()) return Collections.emptyList();
        String[] parts = str.toUpperCase().split("\\s*,\\s*");
        return Arrays.asList(parts);
    }

    public static boolean hasConflict(
            SchedulingVariable v1, SchedulingPlacement p1,
            SchedulingVariable v2, SchedulingPlacement p2) {

        // Check if on same day
        if (!p1.getSlot().getDayOfWeek().equalsIgnoreCase(p2.getSlot().getDayOfWeek())) {
            return false;
        }

        // Check if periods overlap
        int start1 = p1.getSlot().getPeriodNumber();
        int end1 = start1 + v1.getDuration() - 1;

        int start2 = p2.getSlot().getPeriodNumber();
        int end2 = start2 + v2.getDuration() - 1;

        boolean timeOverlaps = Math.max(start1, start2) <= Math.min(end1, end2);
        if (!timeOverlaps) {
            return false;
        }

        // 1. Room conflict: same room at overlapping time
        if (p1.getRoom().getId().equals(p2.getRoom().getId())) {
            return true;
        }

        // 2. Batch conflict: same batch at overlapping time
        if (v1.getBatch().getId().equals(v2.getBatch().getId())) {
            return true;
        }

        // 3. Faculty conflict: same faculty at overlapping time
        if (v1.getFaculty() != null && v2.getFaculty() != null &&
            v1.getFaculty().getId().equals(v2.getFaculty().getId())) {
            return true;
        }

        return false;
    }

    public static List<ScheduleAssignment> toEntityAssignments(
            Map<SchedulingVariable, SchedulingPlacement> assignmentMap,
            Schedule schedule) {

        List<ScheduleAssignment> list = new ArrayList<>();
        for (Map.Entry<SchedulingVariable, SchedulingPlacement> entry : assignmentMap.entrySet()) {
            SchedulingVariable var = entry.getKey();
            SchedulingPlacement pl = entry.getValue();

            ScheduleAssignment sa = new ScheduleAssignment();
            sa.setSchedule(schedule);
            sa.setSubject(var.getSubject());
            sa.setFaculty(var.getFaculty());
            sa.setStudentBatch(var.getBatch());
            sa.setClassroom(pl.getRoom());
            sa.setTimeSlot(pl.getSlot());
            sa.setDurationPeriods(var.getDuration());
            sa.setExam(false);

            list.add(sa);
        }
        return list;
    }

    public static List<ScheduleAssignmentDto> toDtoAssignments(List<ScheduleAssignment> list) {
        List<ScheduleAssignmentDto> dtos = new ArrayList<>();
        for (ScheduleAssignment sa : list) {
            ScheduleAssignmentDto dto = new ScheduleAssignmentDto();
            dto.setId(sa.getId());
            dto.setSubjectId(sa.getSubject().getId());
            dto.setSubjectCode(sa.getSubject().getCode());
            dto.setSubjectName(sa.getSubject().getName());
            if (sa.getFaculty() != null) {
                dto.setFacultyId(sa.getFaculty().getId());
                dto.setFacultyCode(sa.getFaculty().getFacultyId());
                dto.setFacultyName(sa.getFaculty().getName());
            }
            dto.setBatchId(sa.getStudentBatch().getId());
            dto.setBatchCode(sa.getStudentBatch().getBatchCode());
            dto.setClassroomId(sa.getClassroom().getId());
            dto.setRoomNumber(sa.getClassroom().getRoomNumber());
            dto.setRoomType(sa.getClassroom().getRoomType().name());
            dto.setSlotId(sa.getTimeSlot().getId());
            dto.setDayOfWeek(sa.getTimeSlot().getDayOfWeek());
            dto.setPeriodNumber(sa.getTimeSlot().getPeriodNumber());
            dto.setStartTime(sa.getTimeSlot().getStartTime());
            dto.setEndTime(sa.getTimeSlot().getEndTime());
            dto.setDurationPeriods(sa.getDurationPeriods());
            dto.setPractical(sa.getSubject().isPractical());
            dto.setExam(sa.isExam());
            dtos.add(dto);
        }
        return dtos;
    }
}
