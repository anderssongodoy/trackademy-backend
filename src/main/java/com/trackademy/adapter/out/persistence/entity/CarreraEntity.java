package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "carrera")
public class CarreraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "universidad_id", nullable = false)
    public Long universidadId;

    @Column(name = "nombre", nullable = false)
    public String nombre;
}
