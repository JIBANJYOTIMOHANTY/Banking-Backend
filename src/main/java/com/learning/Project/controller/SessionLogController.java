package com.learning.Project.controller;

import com.learning.Project.dto.ApiResponse;
import com.learning.Project.model.SessionLog;
import com.learning.Project.service.SessionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bank/session-logs")
@Tag(name = "Session Logs", description = "Endpoints for administrator device session logs")
public class SessionLogController {

    @Autowired
    private SessionLogService sessionLogService;

    @GetMapping
    @Operation(summary = "Get sessions logs for the authenticated user", description = "Retrieves all device session audit logs for the currently logged in administrator.")
    public ResponseEntity<ApiResponse<SessionLog>> getSessionLogs(Principal principal) {
        String username = principal.getName();
        List<SessionLog> logs = sessionLogService.getLogsForUser(username);
        return ResponseEntity.ok(new ApiResponse<>(0, "Session logs retrieved successfully", logs));
    }

    @PostMapping
    @Operation(summary = "Publish a new session log entry", description = "Appends a new device session log or audit event for the currently authenticated administrator.")
    public ResponseEntity<ApiResponse<SessionLog>> addSessionLog(@RequestBody SessionLogRequest request, Principal principal) {
        String username = principal.getName();
        SessionLog createdLog = sessionLogService.logActivity(
                username,
                request.getDeviceName(),
                request.getDeviceIcon(),
                request.getIpAddress(),
                request.getActivity(),
                request.getStatus()
        );
        return ResponseEntity.ok(new ApiResponse<>(0, "Session log registered successfully", List.of(createdLog)));
    }

    // Static inner class for Request payload mapping
    public static class SessionLogRequest {
        private String deviceName;
        private String deviceIcon;
        private String ipAddress;
        private String activity;
        private String status;

        public SessionLogRequest() {
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceIcon() {
            return deviceIcon;
        }

        public void setDeviceIcon(String deviceIcon) {
            this.deviceIcon = deviceIcon;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
