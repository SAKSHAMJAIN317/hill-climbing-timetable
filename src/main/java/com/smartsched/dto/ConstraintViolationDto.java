package com.smartsched.dto;

public class ConstraintViolationDto {
    private String type; // "HARD" or "SOFT"
    private String ruleName;
    private String description;
    private double penalty;

    public ConstraintViolationDto() {}

    public ConstraintViolationDto(String type, String ruleName, String description, double penalty) {
        this.type = type;
        this.ruleName = ruleName;
        this.description = description;
        this.penalty = penalty;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPenalty() { return penalty; }
    public void setPenalty(double penalty) { this.penalty = penalty; }
}
