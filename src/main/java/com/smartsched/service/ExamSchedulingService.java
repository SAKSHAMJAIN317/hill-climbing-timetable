package com.smartsched.service;

import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.entity.*;
import com.smartsched.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ExamSchedulingService {

    private final ExaminationRepository examRepository;
    private final StudentBatchRepository batchRepository;
    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ExamSchedulingService(
            ExaminationRepository examRepository,
            StudentBatchRepository batchRepository,
            ClassroomRepository classroomRepository,
            FacultyRepository facultyRepository,
            TimeSlotRepository timeSlotRepository) {
        this.examRepository = examRepository;
        this.batchRepository = batchRepository;
        this.classroomRepository = classroomRepository;
        this.facultyRepository = facultyRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public List<Examination> generateExamSchedule() {
        examRepository.deleteAllInBatch();
        examRepository.flush();

        List<StudentBatch> batches = batchRepository.findAll();
        List<Classroom> rooms = classroomRepository.findAll();
        List<Faculty> faculties = facultyRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findByIsBreakFalseOrderByPeriodNumberAsc();

        if (batches.isEmpty() || rooms.isEmpty() || faculties.isEmpty() || slots.isEmpty()) {
            return Collections.emptyList();
        }

        // Exam dates starting next Monday
        LocalDate startDate = LocalDate.now().plusDays(3);
        List<LocalDate> examDates = new ArrayList<>();
        for (int d = 0; d < 10; d++) {
            LocalDate dt = startDate.plusDays(d);
            if (dt.getDayOfWeek().getValue() <= 5) { // Weekdays only
                examDates.add(dt);
            }
        }

        List<Examination> exams = new ArrayList<>();
        // Tracking: Date + "_" + SlotId + "_" + RoomId -> occupied
        Set<String> roomOccupancy = new HashSet<>();
        // Tracking: Date + "_" + BatchId -> has exam that day (max 1 exam per batch per day)
        Set<String> batchDateOccupancy = new HashSet<>();
        // Tracking: Date + "_" + SlotId + "_" + FacultyId -> invigilator occupied
        Set<String> invigilatorOccupancy = new HashSet<>();

        int facultyIndex = 0;

        for (StudentBatch batch : batches) {
            int dateIdx = 0;
            for (Subject subject : batch.getSubjects()) {
                boolean scheduled = false;

                for (int attempt = 0; attempt < examDates.size(); attempt++) {
                    LocalDate examDate = examDates.get((dateIdx + attempt) % examDates.size());
                    String batchDateKey = examDate.toString() + "_" + batch.getId();
                    if (batchDateOccupancy.contains(batchDateKey)) {
                        continue; // Already has exam on this day
                    }

                    for (TimeSlot slot : slots) {
                        for (Classroom room : rooms) {
                            if (room.getCapacity() < batch.getStudentCount()) continue;

                            String roomKey = examDate.toString() + "_" + slot.getId() + "_" + room.getId();
                            if (roomOccupancy.contains(roomKey)) continue;

                            // Find available invigilator
                            Faculty invigilator = null;
                            for (int f = 0; f < faculties.size(); f++) {
                                Faculty candidate = faculties.get((facultyIndex + f) % faculties.size());
                                String invigKey = examDate.toString() + "_" + slot.getId() + "_" + candidate.getId();
                                if (!invigilatorOccupancy.contains(invigKey)) {
                                    invigilator = candidate;
                                    facultyIndex = (facultyIndex + f + 1) % faculties.size();
                                    invigilatorOccupancy.add(invigKey);
                                    break;
                                }
                            }

                            if (invigilator == null) continue;

                            roomOccupancy.add(roomKey);
                            batchDateOccupancy.add(batchDateKey);

                            Examination exam = new Examination(
                                    subject, batch, examDate, slot, 180, room, invigilator
                            );
                            exams.add(exam);
                            scheduled = true;
                            break;
                        }
                        if (scheduled) break;
                    }
                    if (scheduled) {
                        dateIdx++;
                        break;
                    }
                }
            }
        }

        return examRepository.saveAll(exams);
    }

    public List<Examination> getAllExams() {
        return examRepository.findAllByOrderByExamDateAsc();
    }

    public String exportExamCsv() {
        List<Examination> exams = getAllExams();
        StringBuilder csv = new StringBuilder();
        csv.append("Exam Date,Day,Start Time,End Time,Subject Code,Subject Name,Batch,Room,Invigilator\n");
        for (Examination ex : exams) {
            csv.append(ex.getExamDate()).append(",")
                    .append(ex.getTimeSlot().getDayOfWeek()).append(",")
                    .append(ex.getTimeSlot().getStartTime()).append(",")
                    .append(ex.getTimeSlot().getEndTime()).append(",")
                    .append(ex.getSubject().getCode()).append(",")
                    .append(ex.getSubject().getName()).append(",")
                    .append(ex.getStudentBatch().getBatchCode()).append(",")
                    .append(ex.getAssignedRoom().getRoomNumber()).append(",")
                    .append(ex.getAssignedInvigilator().getName()).append("\n");
        }
        return csv.toString();
    }
}
