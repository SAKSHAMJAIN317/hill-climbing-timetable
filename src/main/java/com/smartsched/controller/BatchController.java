package com.smartsched.controller;

import com.smartsched.entity.StudentBatch;
import com.smartsched.entity.Subject;
import com.smartsched.repository.StudentBatchRepository;
import com.smartsched.repository.SubjectRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final StudentBatchRepository batchRepository;
    private final SubjectRepository subjectRepository;

    public BatchController(StudentBatchRepository batchRepository, SubjectRepository subjectRepository) {
        this.batchRepository = batchRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public List<StudentBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentBatch> getBatchById(@PathVariable Long id) {
        return batchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentBatch> createBatch(@Valid @RequestBody StudentBatch batch) {
        if (batchRepository.existsByBatchCode(batch.getBatchCode())) {
            throw new IllegalStateException("Batch code " + batch.getBatchCode() + " already exists.");
        }
        if (batch.getSubjects() != null && !batch.getSubjects().isEmpty()) {
            List<Subject> managed = new ArrayList<>();
            for (Subject s : batch.getSubjects()) {
                if (s.getId() != null) {
                    subjectRepository.findById(s.getId()).ifPresent(managed::add);
                }
            }
            batch.setSubjects(managed);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(batchRepository.save(batch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentBatch> updateBatch(@PathVariable Long id, @Valid @RequestBody StudentBatch req) {
        return batchRepository.findById(id).map(existing -> {
            existing.setDepartment(req.getDepartment());
            existing.setSemester(req.getSemester());
            existing.setSection(req.getSection());
            existing.setStudentCount(req.getStudentCount());
            if (req.getSubjects() != null) {
                List<Subject> managed = new ArrayList<>();
                for (Subject s : req.getSubjects()) {
                    if (s.getId() != null) {
                        subjectRepository.findById(s.getId()).ifPresent(managed::add);
                    }
                }
                existing.setSubjects(managed);
            }
            return ResponseEntity.ok(batchRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        if (!batchRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        batchRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
