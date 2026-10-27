package com.smartsched.dto;

public class ScheduleAssignmentDto {
    private Long id;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long facultyId;
    private String facultyCode;
    private String facultyName;
    private Long batchId;
    private String batchCode;
    private Long classroomId;
    private String roomNumber;
    private String roomType;
    private Long slotId;
    private String dayOfWeek;
    private int periodNumber;
    private String startTime;
    private String endTime;
    private int durationPeriods;
    private boolean isPractical;
    private boolean isExam;

    public ScheduleAssignmentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getDurationPeriods() { return durationPeriods; }
    public void setDurationPeriods(int durationPeriods) { this.durationPeriods = durationPeriods; }

    public boolean isPractical() { return isPractical; }
    public void setPractical(boolean practical) { isPractical = practical; }

    public boolean isExam() { return isExam; }
    public void setExam(boolean exam) { isExam = exam; }
}
