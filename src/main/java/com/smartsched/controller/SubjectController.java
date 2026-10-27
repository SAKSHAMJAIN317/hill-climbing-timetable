package com.smartsched.controller;

import com.smartsched.entity.Faculty;
import com.smartsched.entity.Subject;
import com.smartsched.repository.FacultyRepository;
import com.smartsched.repository.SubjectRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;

    public SubjectController(SubjectRepository subjectRepository, FacultyRepository facultyRepository) {
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return subjectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody Subject subject) {
        if (subjectRepository.existsByCode(subject.getCode())) {
            throw new IllegalStateException("Subject code " + subject.getCode() + " already exists.");
        }
        if (subject.getAssignedFaculty() != null && subject.getAssignedFaculty().getId() != null) {
            Faculty f = facultyRepository.findById(subject.getAssignedFaculty().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
            subject.setAssignedFaculty(f);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectRepository.save(subject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @Valid @RequestBody Subject req) {
        return subjectRepository.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setDepartment(req.getDepartment());
            existing.setSemester(req.getSemester());
            existing.setWeeklyLectures(req.getWeeklyLectures());
            existing.setLectureDuration(req.getLectureDuration());
            existing.setPractical(req.isPractical());
            existing.setRequiredRoomType(req.getRequiredRoomType());
            if (req.getAssignedFaculty() != null && req.getAssignedFaculty().getId() != null) {
                Faculty f = facultyRepository.findById(req.getAssignedFaculty().getId()).orElse(null);
                existing.setAssignedFaculty(f);
            }
            return ResponseEntity.ok(subjectRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        if (!subjectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        subjectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
