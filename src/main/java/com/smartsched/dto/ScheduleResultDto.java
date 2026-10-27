package com.smartsched.dto;

import java.util.ArrayList;
import java.util.List;

public class ScheduleResultDto {
    private Long scheduleId;
    private String name;
    private String algorithmName;
    private String status; // "VALID", "INVALID", "FAILED", "TIMEOUT", "INFEASIBLE"
    private int hardViolations = 0;
    private double softPenalty = 0.0;
    private double fitnessScore = 0.0;
    private long executionTimeMs = 0;
    private long exploredStates = 0;
    private long backtrackingCount = 0;
    private int iterationsOrGenerations = 0;
    private String diagnosticNotes;
    private List<ScheduleAssignmentDto> assignments = new ArrayList<>();
    private List<Double> fitnessHistory = new ArrayList<>();
    private List<ConstraintViolationDto> violations = new ArrayList<>();

    public ScheduleResultDto() {}

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }

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

    public List<ScheduleAssignmentDto> getAssignments() { return assignments; }
    public void setAssignments(List<ScheduleAssignmentDto> assignments) { this.assignments = assignments; }

    public List<Double> getFitnessHistory() { return fitnessHistory; }
    public void setFitnessHistory(List<Double> fitnessHistory) { this.fitnessHistory = fitnessHistory; }

    public List<ConstraintViolationDto> getViolations() { return violations; }
    public void setViolations(List<ConstraintViolationDto> violations) { this.violations = violations; }
}
