package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "campus")
public class CampusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "universidad_id", nullable = false)
    public Long universidadId;

    @Column(name = "nombre", nullable = false)
    public String nombre;

    @Column(name = "timezone", nullable = false)
    public String timezone;
}
