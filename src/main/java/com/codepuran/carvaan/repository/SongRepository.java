package com.codepuran.carvaan.repository;

import com.codepuran.carvaan.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SongRepository extends JpaRepository<Song,Long>{

    List<Song> findByNameIgnoreCase(String name);
}
