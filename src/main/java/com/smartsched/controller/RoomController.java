package com.smartsched.controller;

import com.smartsched.entity.Classroom;
import com.smartsched.repository.ClassroomRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final ClassroomRepository classroomRepository;

    public RoomController(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    public List<Classroom> getAllRooms() {
        return classroomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getRoomById(@PathVariable Long id) {
        return classroomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Classroom> createRoom(@Valid @RequestBody Classroom room) {
        if (classroomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new IllegalStateException("Room number " + room.getRoomNumber() + " already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomRepository.save(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classroom> updateRoom(@PathVariable Long id, @Valid @RequestBody Classroom req) {
        return classroomRepository.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setCapacity(req.getCapacity());
            existing.setRoomType(req.getRoomType());
            existing.setAvailableDays(req.getAvailableDays());
            return ResponseEntity.ok(classroomRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        if (!classroomRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        classroomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
