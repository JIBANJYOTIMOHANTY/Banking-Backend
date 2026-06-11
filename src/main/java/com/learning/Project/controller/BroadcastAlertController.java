package com.learning.Project.controller;

import com.learning.Project.dto.ApiResponse;
import com.learning.Project.model.BroadcastAlert;
import com.learning.Project.repository.BroadcastAlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bank/broadcast")
@Tag(name = "Broadcast Alerts", description = "Operations related to system-wide announcements")
public class BroadcastAlertController {

    @Autowired
    private BroadcastAlertRepository broadcastAlertRepository;

    public static class BroadcastRequest {
        private String message;
        private String alertType;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }
    }

    @PostMapping
    @Operation(summary = "Publish a new system broadcast alert", description = "Deactivates any previous active alerts and publishes a new active banner announcement.")
    public ResponseEntity<ApiResponse<BroadcastAlert>> createAlert(@RequestBody BroadcastRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(1, "Message content is required", List.of()));
        }

        String alertType = request.getAlertType();
        if (alertType != null) {
            alertType = alertType.trim().toUpperCase();
            if (!alertType.equals("INFO") && !alertType.equals("CRITICAL")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(1, "Invalid alert category. Must be INFO or CRITICAL.", List.of()));
            }
        } else {
            alertType = "INFO";
        }

        // Deactivate all existing active alerts
        List<BroadcastAlert> activeAlerts = broadcastAlertRepository.findByIsActive(1);
        for (BroadcastAlert alert : activeAlerts) {
            alert.setIsActive(0);
            broadcastAlertRepository.save(alert);
        }

        // Create new active alert
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        BroadcastAlert newAlert = new BroadcastAlert(request.getMessage().trim(), currentTime, 1, alertType);
        BroadcastAlert savedAlert = broadcastAlertRepository.save(newAlert);


        ApiResponse<BroadcastAlert> response = new ApiResponse<>(0, "Alert broadcasted successfully", List.of(savedAlert));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get current active system broadcast alert", description = "Returns the active system announcement alert, if any exists.")
    public ResponseEntity<ApiResponse<BroadcastAlert>> getActiveAlert() {
        List<BroadcastAlert> activeAlerts = broadcastAlertRepository.findByIsActive(1);
        String message = activeAlerts.isEmpty() ? "No active broadcast alert found" : "Active alert retrieved successfully";
        ApiResponse<BroadcastAlert> response = new ApiResponse<>(0, message, activeAlerts, activeAlerts.size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/active")
    @Operation(summary = "Clear/deactivate the current active broadcast alert", description = "Deactivates all active system announcements.")
    public ResponseEntity<ApiResponse<String>> clearActiveAlert() {
        List<BroadcastAlert> activeAlerts = broadcastAlertRepository.findByIsActive(1);
        for (BroadcastAlert alert : activeAlerts) {
            alert.setIsActive(0);
            broadcastAlertRepository.save(alert);
        }
        ApiResponse<String> response = new ApiResponse<>(0, "Broadcast alert cleared successfully", List.of());
        return ResponseEntity.ok(response);
    }
}
