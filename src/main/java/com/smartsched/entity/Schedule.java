package com.smartsched.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String scheduleType = "CLASS"; // "CLASS" or "EXAM"

    @Column(nullable = false)
    private String algorithmUsed; // "CSP", "BACKTRACKING", "GENETIC", "HILL_CLIMBING"

    @Column(nullable = false)
    private String status; // "VALID", "INVALID", "FAILED", "TIMEOUT"

    @Column(nullable = false)
    private int hardViolations = 0;

    @Column(nullable = false)
    private double softPenalty = 0.0;

    @Column(nullable = false)
    private double fitnessScore = 0.0;

    @Column(nullable = false)
    private long executionTimeMs = 0;

    private long exploredStates = 0;

    private long backtrackingCount = 0;

    private int iterationsOrGenerations = 0;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String diagnosticNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScheduleAssignment> assignments = new ArrayList<>();

    public Schedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public String getAlgorithmUsed() { return algorithmUsed; }
    public void setAlgorithmUsed(String algorithmUsed) { this.algorithmUsed = algorithmUsed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getHardViolations() { return hardViolations; }
    public void setHardViolations(int hardViolations) { this.hardViolations = hardViolations; }

    public double getSoftPenalty() { return softPenalty; }
    public void setSoftPenalty(double softPenalty) { this.softPenalty = softPenalty; }

    public double getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(double fitnessScore) { this.fitnessScore = fitnessScore; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public long getExploredStates() { return exploredStates; }
    public void setExploredStates(long exploredStates) { this.exploredStates = exploredStates; }

    public long getBacktrackingCount() { return backtrackingCount; }
    public void setBacktrackingCount(long backtrackingCount) { this.backtrackingCount = backtrackingCount; }

    public int getIterationsOrGenerations() { return iterationsOrGenerations; }
    public void setIterationsOrGenerations(int iterationsOrGenerations) { this.iterationsOrGenerations = iterationsOrGenerations; }

    public String getDiagnosticNotes() { return diagnosticNotes; }
    public void setDiagnosticNotes(String diagnosticNotes) { this.diagnosticNotes = diagnosticNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ScheduleAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ScheduleAssignment> assignments) {
        this.assignments = assignments;
        if (assignments != null) {
            for (ScheduleAssignment a : assignments) {
                a.setSchedule(this);
            }
        }
    }

    public void addAssignment(ScheduleAssignment assignment) {
        assignments.add(assignment);
        assignment.setSchedule(this);
    }
}
