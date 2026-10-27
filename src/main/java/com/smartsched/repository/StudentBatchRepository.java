package com.smartsched.repository;

import com.smartsched.entity.StudentBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentBatchRepository extends JpaRepository<StudentBatch, Long> {
    Optional<StudentBatch> findByBatchCode(String batchCode);
    boolean existsByBatchCode(String batchCode);
}
