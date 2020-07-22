package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Long> {

    Optional<Film> findByName(String name);
}
