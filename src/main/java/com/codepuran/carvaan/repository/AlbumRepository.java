package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
