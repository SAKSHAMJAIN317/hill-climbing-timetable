package com.smartsched.controller;

import com.smartsched.entity.Faculty;
import com.smartsched.repository.FacultyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyRepository facultyRepository;

    public FacultyController(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @GetMapping
    public List<Faculty> getAllFaculty() {
        return facultyRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Faculty> getFacultyById(@PathVariable Long id) {
        return facultyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Faculty> createFaculty(@Valid @RequestBody Faculty faculty) {
        if (facultyRepository.existsByFacultyId(faculty.getFacultyId())) {
            throw new IllegalStateException("Faculty with ID " + faculty.getFacultyId() + " already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyRepository.save(faculty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faculty> updateFaculty(@PathVariable Long id, @Valid @RequestBody Faculty req) {
        return facultyRepository.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setDepartment(req.getDepartment());
            existing.setMaxDailyLoad(req.getMaxDailyLoad());
            existing.setMaxWeeklyLoad(req.getMaxWeeklyLoad());
            existing.setAvailableDays(req.getAvailableDays());
            existing.setAvailableSlots(req.getAvailableSlots());
            existing.setPreferredSlots(req.getPreferredSlots());
            return ResponseEntity.ok(facultyRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        if (!facultyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        facultyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
