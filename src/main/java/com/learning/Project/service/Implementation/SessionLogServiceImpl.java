package com.learning.Project.service.Implementation;

import com.learning.Project.model.SessionLog;
import com.learning.Project.repository.SessionLogRepository;
import com.learning.Project.service.SessionLogService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SessionLogServiceImpl implements SessionLogService {

    private final SessionLogRepository sessionLogRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    SessionLogServiceImpl(SessionLogRepository sessionLogRepository) {
        this.sessionLogRepository = sessionLogRepository;
    }

    @Override
    public SessionLog logActivity(String username, String deviceName, String deviceIcon, String ipAddress,
            String activity, String status) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        SessionLog log = new SessionLog(username, deviceName, deviceIcon, ipAddress, activity, timestamp, status);
        return sessionLogRepository.save(log);
    }

    @Override
    public List<SessionLog> getLogsForUser(String username) {
        return sessionLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
}
