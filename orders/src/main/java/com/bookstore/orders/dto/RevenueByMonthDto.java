package com.bookstore.orders.dto;

public class RevenueByMonthDto {
    private String period;
    private Double totalRevenue;

    // Constructors
    public RevenueByMonthDto(String period, Double totalRevenue) {
        this.period = period;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}