package com.smartsched.controller;

import com.smartsched.entity.Examination;
import com.smartsched.service.ExamSchedulingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamSchedulingService examService;

    public ExamController(ExamSchedulingService examService) {
        this.examService = examService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Examination>> generateExams() {
        List<Examination> exams = examService.generateExamSchedule();
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/schedules")
    public List<Examination> getExamSchedules() {
        return examService.getAllExams();
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportExamCsv() {
        String csv = examService.exportExamCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exam-schedule.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
