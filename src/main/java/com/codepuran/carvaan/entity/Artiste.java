package com.codepuran.carvaan.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Builder
@Entity
@Table(name = "artiste")
@NoArgsConstructor
@AllArgsConstructor
public class Artiste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 1000)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artiste artiste = (Artiste) o;
        return id.equals(artiste.id) &&
                name.equals(artiste.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
