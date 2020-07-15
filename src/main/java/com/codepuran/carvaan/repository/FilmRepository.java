package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Long> {
}
