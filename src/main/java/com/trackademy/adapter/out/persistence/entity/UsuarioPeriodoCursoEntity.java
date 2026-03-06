package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_periodo_curso")
public class UsuarioPeriodoCursoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_id", nullable = false)
    public Long usuarioPeriodoId;

    @Column(name = "curso_id", nullable = false)
    public Long cursoId;

    @Column(name = "seccion")
    public String seccion;

    @Column(name = "profesor")
    public String profesor;

    @Column(name = "modalidad")
    public String modalidad;

    @Column(name = "estado")
    public String estado;

    @Column(name = "activo")
    public Boolean activo;

    @Column(name = "origen")
    public String origen;
}
