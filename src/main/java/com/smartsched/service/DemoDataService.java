package com.smartsched.service;

import com.smartsched.entity.*;
import com.smartsched.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DemoDataService {

    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final StudentBatchRepository batchRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final ExaminationRepository examRepository;

    public DemoDataService(
            FacultyRepository facultyRepository,
            ClassroomRepository classroomRepository,
            SubjectRepository subjectRepository,
            StudentBatchRepository batchRepository,
            TimeSlotRepository timeSlotRepository,
            ScheduleRepository scheduleRepository,
            ScheduleAssignmentRepository assignmentRepository,
            ExaminationRepository examRepository) {
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.batchRepository = batchRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public void resetAndLoadDemoData(String scenario) {
        // Clear old data safely in dependency order with immediate flush
        assignmentRepository.deleteAllInBatch();
        examRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        batchRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
        facultyRepository.deleteAllInBatch();
        classroomRepository.deleteAllInBatch();
        timeSlotRepository.deleteAllInBatch();
        timeSlotRepository.flush();
        classroomRepository.flush();
        facultyRepository.flush();
        subjectRepository.flush();
        batchRepository.flush();

        // 1. Time Slots (Mon-Fri, 6 periods + 1 lunch break)
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        String[][] periodTimes = {
                {"1", "09:00", "10:00", "false"},
                {"2", "10:00", "11:00", "false"},
                {"3", "11:15", "12:15", "false"},
                {"4", "12:15", "01:15", "true"},  // Lunch break
                {"5", "01:15", "02:15", "false"},
                {"6", "02:15", "03:15", "false"},
                {"7", "03:15", "04:15", "false"}
        };

        List<TimeSlot> slots = new ArrayList<>();
        for (String day : days) {
            for (String[] pt : periodTimes) {
                slots.add(new TimeSlot(day, Integer.parseInt(pt[0]), pt[1], pt[2], Boolean.parseBoolean(pt[3])));
            }
        }
        timeSlotRepository.saveAll(slots);

        // 2. Classrooms & Labs
        List<Classroom> rooms = new ArrayList<>();
        if ("IMPOSSIBLE".equalsIgnoreCase(scenario)) {
            // Under impossible scenario: room capacity is tiny (15) while batch count is 60, making assignment impossible!
            rooms.add(new Classroom("LH-101", "Mini Room 1", 15, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("LH-102", "Mini Room 2", 15, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
        } else {
            rooms.add(new Classroom("LH-101", "Main Lecture Hall 1", 65, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("LH-102", "Main Lecture Hall 2", 60, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("CR-201", "Smart Classroom 201", 55, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("CR-202", "Smart Classroom 202", 50, RoomType.CLASSROOM, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("LAB-1", "Advanced Computing Lab", 55, RoomType.COMPUTER_LAB, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("LAB-2", "Software Systems Lab", 50, RoomType.COMPUTER_LAB, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
            rooms.add(new Classroom("LAB-ECE", "Microcontroller Hardware Lab", 45, RoomType.SPECIALIZED_LAB, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"));
        }
        classroomRepository.saveAll(rooms);

        // 3. Faculty Members (11 members)
        List<Faculty> faculties = new ArrayList<>();
        faculties.add(new Faculty("FAC-01", "Dr. Rajesh Sharma", "CSE", 3, 15, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "1,2,3"));
        faculties.add(new Faculty("FAC-02", "Prof. Priya Menon", "CSE", 4, 18, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "2,3,5"));
        faculties.add(new Faculty("FAC-03", "Dr. Amit Verma", "CSE", 3, 16, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "1,3,6"));
        faculties.add(new Faculty("FAC-04", "Prof. Sunita Rao", "CSE", 4, 18, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "5,6,7"));
        faculties.add(new Faculty("FAC-05", "Dr. Vikram Rathore", "IT", 3, 15, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "1,2,5"));
        faculties.add(new Faculty("FAC-06", "Prof. Neha Gupta", "IT", 4, 16, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "2,3,6"));
        faculties.add(new Faculty("FAC-07", "Dr. Sandeep Kulkarni", "ECE", 3, 14, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "1,2,3"));
        faculties.add(new Faculty("FAC-08", "Prof. Anjali Deshmukh", "ECE", 4, 16, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "3,5,6"));
        faculties.add(new Faculty("FAC-09", "Dr. Harish Chandra", "Math", 4, 18, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "1,2"));
        faculties.add(new Faculty("FAC-10", "Prof. Kavita Nair", "Management", 3, 12, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "6,7"));
        faculties.add(new Faculty("FAC-11", "Dr. Rohan Sen", "AI/DS", 3, 15, "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY", "1,2,3,5,6,7", "2,3,5"));
        facultyRepository.saveAll(faculties);

        // 4. Subjects (14 subjects)
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("CS301", "Database Management Systems", "CSE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(0)));
        subjects.add(new Subject("CS302", "Operating Systems", "CSE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(1)));
        subjects.add(new Subject("CS303", "Design & Analysis of Algorithms", "CSE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(2)));
        subjects.add(new Subject("CS304", "DBMS Practical Lab", "CSE", 5, 2, 2, true, RoomType.COMPUTER_LAB, faculties.get(3)));
        subjects.add(new Subject("CS305", "Operating Systems Lab", "CSE", 5, 2, 2, true, RoomType.COMPUTER_LAB, faculties.get(1)));

        subjects.add(new Subject("IT501", "Web Technologies", "IT", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(4)));
        subjects.add(new Subject("IT502", "Cloud Computing & DevOps", "IT", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(5)));
        subjects.add(new Subject("IT503", "Full Stack Web Lab", "IT", 5, 2, 2, true, RoomType.COMPUTER_LAB, faculties.get(4)));

        subjects.add(new Subject("EC301", "Digital Signal Processing", "ECE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(6)));
        subjects.add(new Subject("EC302", "Microprocessors & Interfacing", "ECE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(7)));
        subjects.add(new Subject("EC303", "Microcontroller Hardware Lab", "ECE", 5, 2, 2, true, RoomType.SPECIALIZED_LAB, faculties.get(7)));

        subjects.add(new Subject("MA301", "Discrete Mathematics", "CSE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(8)));
        subjects.add(new Subject("MG301", "Engineering Economics", "IT", 5, 2, 1, false, RoomType.CLASSROOM, faculties.get(9)));
        subjects.add(new Subject("AI401", "Applied Artificial Intelligence", "CSE", 5, 3, 1, false, RoomType.CLASSROOM, faculties.get(10)));

        subjectRepository.saveAll(subjects);

        // 5. Student Batches (4 batches)
        List<StudentBatch> batches = new ArrayList<>();
        int defaultCount = "IMPOSSIBLE".equalsIgnoreCase(scenario) ? 60 : 45;

        // CSE-3A
        List<Subject> cse3aSubjects = List.of(subjects.get(0), subjects.get(1), subjects.get(2), subjects.get(3), subjects.get(11));
        batches.add(new StudentBatch("CSE-5A", "CSE", 5, "A", defaultCount, cse3aSubjects));

        // CSE-3B
        List<Subject> cse3bSubjects = List.of(subjects.get(0), subjects.get(1), subjects.get(4), subjects.get(11), subjects.get(13));
        batches.add(new StudentBatch("CSE-5B", "CSE", 5, "B", defaultCount, cse3bSubjects));

        // IT-5A
        List<Subject> it5aSubjects = List.of(subjects.get(5), subjects.get(6), subjects.get(7), subjects.get(12), subjects.get(13));
        batches.add(new StudentBatch("IT-5A", "IT", 5, "A", defaultCount - 5, it5aSubjects));

        // ECE-5A
        List<Subject> ece5aSubjects = List.of(subjects.get(8), subjects.get(9), subjects.get(10), subjects.get(11), subjects.get(12));
        batches.add(new StudentBatch("ECE-5A", "ECE", 5, "A", defaultCount - 10, ece5aSubjects));

        batchRepository.saveAll(batches);
    }
}
