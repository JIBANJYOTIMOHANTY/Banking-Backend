package com.learning.Project.service;

import com.learning.Project.model.SessionLog;
import java.util.List;

public interface SessionLogService {
    SessionLog logActivity(String username, String deviceName, String deviceIcon, String ipAddress, String activity, String status);
    List<SessionLog> getLogsForUser(String username);
}
