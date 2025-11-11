package com.example.webhook.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class SolutionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;

    @Lob
    private String finalQuery;

    private Instant createdAt = Instant.now();

    public SolutionRecord() {}

    public SolutionRecord(String regNo, String finalQuery) {
        this.regNo = regNo;
        this.finalQuery = finalQuery;
    }

    // ✅ Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
