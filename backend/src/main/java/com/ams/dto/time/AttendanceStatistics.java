package com.ams.dto.time;

import java.time.LocalDate;

public class AttendanceStatistics {

    private LocalDate startDate;
    private LocalDate endDate;
    private Double averageHours;
    private Double totalHours;
    private long workingDays;

    // Default constructor
    public AttendanceStatistics() {
    }

    // Constructor
    public AttendanceStatistics(LocalDate startDate, LocalDate endDate, Double averageHours, 
                               Double totalHours, long workingDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.averageHours = averageHours != null ? averageHours : 0.0;
        this.totalHours = totalHours != null ? totalHours : 0.0;
        this.workingDays = workingDays;
    }

    // Getters and Setters
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getAverageHours() {
        return averageHours;
    }

    public void setAverageHours(Double averageHours) {
        this.averageHours = averageHours;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public long getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(long workingDays) {
        this.workingDays = workingDays;
    }

    @Override
    public String toString() {
        return "AttendanceStatistics{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", averageHours=" + averageHours +
                ", totalHours=" + totalHours +
                ", workingDays=" + workingDays +
                '}';
    }
}