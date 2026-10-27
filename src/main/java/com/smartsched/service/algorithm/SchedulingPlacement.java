package com.smartsched.service.algorithm;

import com.smartsched.entity.Classroom;
import com.smartsched.entity.TimeSlot;
import java.util.Objects;

public class SchedulingPlacement {
    private final TimeSlot slot;
    private final Classroom room;

    public SchedulingPlacement(TimeSlot slot, Classroom room) {
        this.slot = slot;
        this.room = room;
    }

    public TimeSlot getSlot() { return slot; }
    public Classroom getRoom() { return room; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingPlacement that = (SchedulingPlacement) o;
        return Objects.equals(slot, that.slot) && Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, room);
    }
}
