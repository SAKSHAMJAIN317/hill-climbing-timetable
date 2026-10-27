package com.smartsched.dto;

public class TimetableGenerationRequest {
    private String algorithm = "HILL_CLIMBING";
    private String scheduleType = "CLASS"; // CLASS or EXAM
    private String name = "Academic Timetable";

    // Hill Climbing parameters
    private int maxIterations = 2000;
    private int randomRestarts = 5;

    public TimetableGenerationRequest() {}

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }

    public int getRandomRestarts() { return randomRestarts; }
    public void setRandomRestarts(int randomRestarts) { this.randomRestarts = randomRestarts; }
}
