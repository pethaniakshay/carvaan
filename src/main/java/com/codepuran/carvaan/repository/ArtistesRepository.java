package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Artiste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistesRepository extends JpaRepository<Artiste, Long> {

    Optional<Artiste> findByNameIgnoreCase(String name);
}
