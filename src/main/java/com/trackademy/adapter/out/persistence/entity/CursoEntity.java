package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "curso")
public class CursoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "universidad_id", nullable = false)
    public Long universidadId;

    @Column(name = "codigo", nullable = false)
    public String codigo;

    @Column(name = "nombre", nullable = false)
    public String nombre;

    @Column(name = "course_key", nullable = false)
    public String courseKey;

    @Column(name = "modalidad")
    public String modalidad;

    @Column(name = "creditos")
    public Integer creditos;

    @Column(name = "horas_semanales")
    public Integer horasSemanales;
}
