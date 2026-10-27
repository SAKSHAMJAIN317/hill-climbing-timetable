package com.smartsched.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "schedule_assignments")
public class ScheduleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    @JsonIgnore
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private StudentBatch studentBatch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private int durationPeriods = 1;

    @Column(nullable = false)
    private boolean isExam = false;

    public ScheduleAssignment() {}

    public ScheduleAssignment(Schedule schedule, Subject subject, Faculty faculty, StudentBatch studentBatch,
                              Classroom classroom, TimeSlot timeSlot, int durationPeriods, boolean isExam) {
        this.schedule = schedule;
        this.subject = subject;
        this.faculty = faculty;
        this.studentBatch = studentBatch;
        this.classroom = classroom;
        this.timeSlot = timeSlot;
        this.durationPeriods = durationPeriods;
        this.isExam = isExam;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public StudentBatch getStudentBatch() { return studentBatch; }
    public void setStudentBatch(StudentBatch studentBatch) { this.studentBatch = studentBatch; }

    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }

    public int getDurationPeriods() { return durationPeriods; }
    public void setDurationPeriods(int durationPeriods) { this.durationPeriods = durationPeriods; }

    public boolean isExam() { return isExam; }
    public void setExam(boolean exam) { isExam = exam; }
}
