package com.ofss.model;

public class ChatRequest {

    private String message;      // e.g., "Recommend stocks for me"
    private Double budget;       // optional: amount user wants to invest
    private String riskProfile;  // optional: user's risk appetite
    // getters and setters

    public String getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(String riskProfile) {
        this.riskProfile = riskProfile;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
