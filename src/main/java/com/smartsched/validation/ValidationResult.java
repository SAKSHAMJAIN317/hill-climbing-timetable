package com.smartsched.validation;

import com.smartsched.dto.ConstraintViolationDto;
import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private int hardViolationCount = 0;
    private double softPenalty = 0.0;
    private double fitnessScore = 0.0;
    private List<ConstraintViolationDto> violations = new ArrayList<>();

    public ValidationResult() {
        this.valid = true;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public int getHardViolationCount() { return hardViolationCount; }
    public void setHardViolationCount(int hardViolationCount) { this.hardViolationCount = hardViolationCount; }

    public double getSoftPenalty() { return softPenalty; }
    public void setSoftPenalty(double softPenalty) { this.softPenalty = softPenalty; }

    public double getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(double fitnessScore) { this.fitnessScore = fitnessScore; }

    public List<ConstraintViolationDto> getViolations() { return violations; }
    public void setViolations(List<ConstraintViolationDto> violations) { this.violations = violations; }

    public void addHardViolation(String ruleName, String description) {
        this.valid = false;
        this.hardViolationCount++;
        this.violations.add(new ConstraintViolationDto("HARD", ruleName, description, 100.0));
    }

    public void addSoftViolation(String ruleName, String description, double penalty) {
        this.softPenalty += penalty;
        this.violations.add(new ConstraintViolationDto("SOFT", ruleName, description, penalty));
    }
}
