package com.learning.Project.dto;

import java.util.List;

public class ApiResponse<T> {
    private int status;
    private String message;
    private int totalRecords;
    private List<T> data;

    public ApiResponse() {
    }

    public ApiResponse(int status, String message, List<T> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(int status, String message, List<T> data, int totalRecords) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.totalRecords = totalRecords;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
