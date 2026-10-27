package com.smartsched.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "algorithm_runs")
public class AlgorithmRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String runName;

    @Column(nullable = false)
    private String algorithmName;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, TIMEOUT, INFEASIBLE

    @Column(nullable = false)
    private int hardViolations;

    @Column(nullable = false)
    private double softPenalty;

    @Column(nullable = false)
    private double fitnessScore;

    @Column(nullable = false)
    private long executionTimeMs;

    private long exploredStates;

    private long backtrackingCount;

    private int iterationsOrGenerations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String fitnessHistoryJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String diagnosticNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AlgorithmRun() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRunName() { return runName; }
    public void setRunName(String runName) { this.runName = runName; }

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

    public String getFitnessHistoryJson() { return fitnessHistoryJson; }
    public void setFitnessHistoryJson(String fitnessHistoryJson) { this.fitnessHistoryJson = fitnessHistoryJson; }

    public String getDiagnosticNotes() { return diagnosticNotes; }
    public void setDiagnosticNotes(String diagnosticNotes) { this.diagnosticNotes = diagnosticNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
