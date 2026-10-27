package com.smartsched.dto;

import java.util.List;

public class DashboardSummaryDto {
    private long totalFaculty;
    private long totalSubjects;
    private long totalBatches;
    private long totalRooms;
    private long totalScheduledClasses;
    private String currentTimetableStatus;
    private int hardViolations;
    private double softPenalty;
    private double scheduleQualityPercent;
    private List<ScheduleResultDto> recentRuns;

    public DashboardSummaryDto() {}

    public long getTotalFaculty() { return totalFaculty; }
    public void setTotalFaculty(long totalFaculty) { this.totalFaculty = totalFaculty; }

    public long getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }

    public long getTotalBatches() { return totalBatches; }
    public void setTotalBatches(long totalBatches) { this.totalBatches = totalBatches; }

    public long getTotalRooms() { return totalRooms; }
    public void setTotalRooms(long totalRooms) { this.totalRooms = totalRooms; }

    public long getTotalScheduledClasses() { return totalScheduledClasses; }
    public void setTotalScheduledClasses(long totalScheduledClasses) { this.totalScheduledClasses = totalScheduledClasses; }

    public String getCurrentTimetableStatus() { return currentTimetableStatus; }
    public void setCurrentTimetableStatus(String currentTimetableStatus) { this.currentTimetableStatus = currentTimetableStatus; }

    public int getHardViolations() { return hardViolations; }
    public void setHardViolations(int hardViolations) { this.hardViolations = hardViolations; }

    public double getSoftPenalty() { return softPenalty; }
    public void setSoftPenalty(double softPenalty) { this.softPenalty = softPenalty; }

    public double getScheduleQualityPercent() { return scheduleQualityPercent; }
    public void setScheduleQualityPercent(double scheduleQualityPercent) { this.scheduleQualityPercent = scheduleQualityPercent; }

    public List<ScheduleResultDto> getRecentRuns() { return recentRuns; }
    public void setRecentRuns(List<ScheduleResultDto> recentRuns) { this.recentRuns = recentRuns; }
}
