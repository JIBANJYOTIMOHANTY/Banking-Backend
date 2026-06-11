package com.learning.Project.repository;

import com.learning.Project.model.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    List<SessionLog> findByUsernameOrderByTimestampDesc(String username);
}
