package com.smartsched.repository;

import com.smartsched.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findAllByOrderByExamDateAsc();
    List<Examination> findByStudentBatchId(Long batchId);
}
