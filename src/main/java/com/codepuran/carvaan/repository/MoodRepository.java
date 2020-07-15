package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodRepository extends JpaRepository<Mood, Long> {
}
