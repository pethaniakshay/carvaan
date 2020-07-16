package com.codepuran.carvaan.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "geet_mala")
@NoArgsConstructor
@AllArgsConstructor
public class GeetMala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 1000)
    private String name;

    //TODO add more columns as identified with more clarity
}