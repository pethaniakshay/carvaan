package com.codepuran.carvaan.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@Table(name = "song")
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 1000)
    private String name;

    @ManyToMany
    @JoinTable(name = "songs_artistes",
    joinColumns = @JoinColumn(name = "song_id"),
    inverseJoinColumns = @JoinColumn(name = "artiste_id"))
    private Set<Artiste> artistes;

    @ManyToOne
    @JoinColumn(name = "album_id", referencedColumnName = "id")
    private Album album;

    @ManyToOne
    @JoinColumn(name = "film_id", referencedColumnName = "id")
    private Film film;

    @ManyToOne(cascade = {CascadeType.DETACH})
    @JoinColumn(name = "mood_id", referencedColumnName = "id")
    private Mood mood;
}
