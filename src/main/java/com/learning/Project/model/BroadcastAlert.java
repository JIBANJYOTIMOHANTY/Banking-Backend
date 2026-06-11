package com.learning.Project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "broadcast_alerts")
public class BroadcastAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String createdAt;

    @Column(name = "is_active", nullable = false)
    private int isActive = 1;

    @Column(name = "alert_type", nullable = false)
    private String alertType = "INFO";

    public BroadcastAlert() {
    }

    public BroadcastAlert(String message, String createdAt, int isActive) {
        this.message = message;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.alertType = "INFO";
    }

    public BroadcastAlert(String message, String createdAt, int isActive, String alertType) {
        this.message = message;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.alertType = alertType != null ? alertType : "INFO";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
}

