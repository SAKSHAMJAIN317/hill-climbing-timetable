package com.smartsched.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "student_batches")
public class StudentBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Batch code is required")
    @Column(nullable = false, unique = true)
    private String batchCode; // e.g. "CSE-3A"

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @Min(value = 1, message = "Semester must be at least 1")
    @Column(nullable = false)
    private int semester;

    @NotBlank(message = "Section is required")
    @Column(nullable = false)
    private String section; // e.g. "A"

    @Min(value = 1, message = "Student count must be at least 1")
    @Column(nullable = false)
    private int studentCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "batch_subjects",
        joinColumns = @JoinColumn(name = "batch_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @JsonIgnoreProperties({"assignedFaculty"})
    private List<Subject> subjects = new ArrayList<>();

    public StudentBatch() {}

    public StudentBatch(String batchCode, String department, int semester, String section, int studentCount, List<Subject> subjects) {
        this.batchCode = batchCode;
        this.department = department;
        this.semester = semester;
        this.section = section;
        this.studentCount = studentCount;
        if (subjects != null) {
            this.subjects = subjects;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentBatch that = (StudentBatch) o;
        return Objects.equals(id, that.id) || Objects.equals(batchCode, that.batchCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchCode);
    }
}
