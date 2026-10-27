package com.smartsched.service.algorithm;

import com.smartsched.entity.*;
import java.util.Objects;

public class SchedulingVariable {
    private final int id;
    private final Subject subject;
    private final StudentBatch batch;
    private final Faculty faculty;
    private final int sessionIndex; // 1 to weeklyLectures
    private final int duration;     // 1 or 2 periods
    private final boolean isPractical;
    private final RoomType requiredRoomType;

    public SchedulingVariable(int id, Subject subject, StudentBatch batch, Faculty faculty,
                              int sessionIndex, int duration, boolean isPractical, RoomType requiredRoomType) {
        this.id = id;
        this.subject = subject;
        this.batch = batch;
        this.faculty = faculty;
        this.sessionIndex = sessionIndex;
        this.duration = duration;
        this.isPractical = isPractical;
        this.requiredRoomType = requiredRoomType;
    }

    public int getId() { return id; }
    public Subject getSubject() { return subject; }
    public StudentBatch getBatch() { return batch; }
    public Faculty getFaculty() { return faculty; }
    public int getSessionIndex() { return sessionIndex; }
    public int getDuration() { return duration; }
    public boolean isPractical() { return isPractical; }
    public RoomType getRequiredRoomType() { return requiredRoomType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingVariable that = (SchedulingVariable) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
