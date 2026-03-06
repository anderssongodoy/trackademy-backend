package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "periodo_evento")
public class PeriodoEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "periodo_id", nullable = false)
    public Long periodoId;

    @Column(name = "tipo", nullable = false)
    public String tipo;

    @Column(name = "titulo", nullable = false)
    public String titulo;

    @Column(name = "fecha_inicio", nullable = false)
    public LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    public LocalDate fechaFin;

    @Column(name = "descripcion")
    public String descripcion;
}
