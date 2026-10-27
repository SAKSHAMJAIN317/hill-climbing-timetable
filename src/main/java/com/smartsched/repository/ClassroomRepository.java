package com.smartsched.repository;

import com.smartsched.entity.Classroom;
import com.smartsched.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
    List<Classroom> findByRoomType(RoomType roomType);
}
