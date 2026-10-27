package com.smartsched.dto;

import jakarta.validation.constraints.NotNull;

public class ManualEditRequest {
    @NotNull(message = "Time slot ID is required")
    private Long timeSlotId;

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    public ManualEditRequest() {}

    public ManualEditRequest(Long timeSlotId, Long classroomId) {
        this.timeSlotId = timeSlotId;
        this.classroomId = classroomId;
    }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
}
