package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "periodo")
public class PeriodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "universidad_id", nullable = false)
    public Long universidadId;

    @Column(name = "etiqueta", nullable = false)
    public String etiqueta;

    @Column(name = "fecha_inicio")
    public LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    public LocalDate fechaFin;

    @Column(name = "estado", nullable = false)
    public String estado;
}
