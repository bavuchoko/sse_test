package com.example.sse_test.sse.repository;

import com.example.sse_test.sse.entity.SseTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface SseJpaRepository extends JpaRepository<SseTest, Integer> {
}
