package com.smartsched.repository;

import com.smartsched.entity.AlgorithmRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlgorithmRunRepository extends JpaRepository<AlgorithmRun, Long> {
    List<AlgorithmRun> findAllByOrderByCreatedAtDesc();
    List<AlgorithmRun> findTop10ByOrderByCreatedAtDesc();
}
