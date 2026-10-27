package com.smartsched.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject code is required")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "Subject name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @Min(value = 1, message = "Semester must be at least 1")
    @Column(nullable = false)
    private int semester;

    @Min(value = 1, message = "Weekly lectures must be at least 1")
    @Column(nullable = false)
    private int weeklyLectures = 3;

    @Min(value = 1, message = "Lecture duration must be at least 1")
    @Column(nullable = false)
    private int lectureDuration = 1; // 1 period for regular class, 2 for continuous lab session

    @Column(nullable = false)
    private boolean practical = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType requiredRoomType = RoomType.CLASSROOM;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Faculty assignedFaculty;

    public Subject() {}

    public Subject(String code, String name, String department, int semester, int weeklyLectures,
                   int lectureDuration, boolean practical, RoomType requiredRoomType, Faculty assignedFaculty) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.weeklyLectures = weeklyLectures;
        this.lectureDuration = lectureDuration;
        this.practical = practical;
        this.requiredRoomType = requiredRoomType;
        this.assignedFaculty = assignedFaculty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getWeeklyLectures() { return weeklyLectures; }
    public void setWeeklyLectures(int weeklyLectures) { this.weeklyLectures = weeklyLectures; }

    public int getLectureDuration() { return lectureDuration; }
    public void setLectureDuration(int lectureDuration) { this.lectureDuration = lectureDuration; }

    public boolean isPractical() { return practical; }
    public void setPractical(boolean practical) { this.practical = practical; }

    public RoomType getRequiredRoomType() { return requiredRoomType; }
    public void setRequiredRoomType(RoomType requiredRoomType) { this.requiredRoomType = requiredRoomType; }

    public Faculty getAssignedFaculty() { return assignedFaculty; }
    public void setAssignedFaculty(Faculty assignedFaculty) { this.assignedFaculty = assignedFaculty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(id, subject.id) || Objects.equals(code, subject.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
