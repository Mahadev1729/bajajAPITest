package com.example.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.webhook.entity.SolutionRecord;

public interface SolutionRecordRepository extends JpaRepository<SolutionRecord, Long> {}
