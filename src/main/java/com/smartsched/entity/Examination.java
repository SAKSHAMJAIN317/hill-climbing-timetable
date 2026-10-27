package com.smartsched.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "examinations")
public class Examination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonIgnoreProperties({"assignedFaculty"})
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"subjects"})
    private StudentBatch studentBatch;

    @Column(nullable = false)
    private LocalDate examDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private int durationMinutes = 180;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom assignedRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invigilator_id", nullable = false)
    private Faculty assignedInvigilator;

    public Examination() {}

    public Examination(Subject subject, StudentBatch studentBatch, LocalDate examDate,
                       TimeSlot timeSlot, int durationMinutes, Classroom assignedRoom, Faculty assignedInvigilator) {
        this.subject = subject;
        this.studentBatch = studentBatch;
        this.examDate = examDate;
        this.timeSlot = timeSlot;
        this.durationMinutes = durationMinutes;
        this.assignedRoom = assignedRoom;
        this.assignedInvigilator = assignedInvigilator;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public StudentBatch getStudentBatch() { return studentBatch; }
    public void setStudentBatch(StudentBatch studentBatch) { this.studentBatch = studentBatch; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public Classroom getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(Classroom assignedRoom) { this.assignedRoom = assignedRoom; }

    public Faculty getAssignedInvigilator() { return assignedInvigilator; }
    public void setAssignedInvigilator(Faculty assignedInvigilator) { this.assignedInvigilator = assignedInvigilator; }
}
