package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class job {

    private String title;
    private String skills;
    private double minSalary;
    private double maxSalary;

    // REQUIRED: no-args constructor for deserialization
    public job() {}

    public job(String title, String skills, double minSalary, double maxSalary) {
        this.title = title;
        this.skills = skills;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public double getMinSalary() { return minSalary; }
    public void setMinSalary(double minSalary) { this.minSalary = minSalary; }

    public double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(double maxSalary) { this.maxSalary = maxSalary; }
}
