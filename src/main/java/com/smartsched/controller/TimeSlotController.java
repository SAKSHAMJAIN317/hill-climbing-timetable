package com.smartsched.controller;

import com.smartsched.entity.TimeSlot;
import com.smartsched.repository.TimeSlotRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
public class TimeSlotController {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotController(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @GetMapping
    public List<TimeSlot> getAllSlots() {
        return timeSlotRepository.findAllByOrderByPeriodNumberAsc();
    }

    @PostMapping
    public ResponseEntity<TimeSlot> createSlot(@Valid @RequestBody TimeSlot slot) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSlotRepository.save(slot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        if (!timeSlotRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeSlotRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
