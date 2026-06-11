package com.learning.Project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "session_logs")
public class SessionLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String deviceIcon;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String activity;

    @Column(nullable = false)
    private String timestamp; // yyyy-MM-dd HH:mm:ss format

    @Column(nullable = false)
    private String status; // Active, Terminated, Expired

    public SessionLog() {
    }

    public SessionLog(String username, String deviceName, String deviceIcon, String ipAddress, String activity, String timestamp, String status) {
        this.username = username;
        this.deviceName = deviceName;
        this.deviceIcon = deviceIcon;
        this.ipAddress = ipAddress;
        this.activity = activity;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
