package com.learning.Project.repository;

import com.learning.Project.model.BroadcastAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadcastAlertRepository extends JpaRepository<BroadcastAlert, Long> {
    List<BroadcastAlert> findByIsActive(int isActive);
}
