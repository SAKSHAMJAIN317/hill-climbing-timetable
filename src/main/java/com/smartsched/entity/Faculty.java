package com.smartsched.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Faculty ID is required")
    @Column(nullable = false, unique = true)
    private String facultyId;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @Min(value = 1, message = "Max daily load must be at least 1")
    @Column(nullable = false)
    private int maxDailyLoad = 4;

    @Min(value = 1, message = "Max weekly load must be at least 1")
    @Column(nullable = false)
    private int maxWeeklyLoad = 18;

    // Comma-separated: "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
    @Column(length = 255)
    private String availableDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";

    // Comma-separated period numbers: "1,2,3,4,5,6"
    @Column(length = 255)
    private String availableSlots = "1,2,3,4,5,6";

    // Preferred period numbers: "1,2,3"
    @Column(length = 255)
    private String preferredSlots = "1,2,3";

    public Faculty() {}

    public Faculty(String facultyId, String name, String department, int maxDailyLoad, int maxWeeklyLoad,
                   String availableDays, String availableSlots, String preferredSlots) {
        this.facultyId = facultyId;
        this.name = name;
        this.department = department;
        this.maxDailyLoad = maxDailyLoad;
        this.maxWeeklyLoad = maxWeeklyLoad;
        this.availableDays = availableDays;
        this.availableSlots = availableSlots;
        this.preferredSlots = preferredSlots;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getMaxDailyLoad() { return maxDailyLoad; }
    public void setMaxDailyLoad(int maxDailyLoad) { this.maxDailyLoad = maxDailyLoad; }

    public int getMaxWeeklyLoad() { return maxWeeklyLoad; }
    public void setMaxWeeklyLoad(int maxWeeklyLoad) { this.maxWeeklyLoad = maxWeeklyLoad; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(String availableSlots) { this.availableSlots = availableSlots; }

    public String getPreferredSlots() { return preferredSlots; }
    public void setPreferredSlots(String preferredSlots) { this.preferredSlots = preferredSlots; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faculty faculty = (Faculty) o;
        return Objects.equals(id, faculty.id) || Objects.equals(facultyId, faculty.facultyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facultyId);
    }
}
