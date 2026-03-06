package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_periodo_curso_confianza")
public class UsuarioPeriodoCursoConfianzaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_curso_id", nullable = false)
    public Long usuarioPeriodoCursoId;

    @Column(name = "nivel_confianza", nullable = false)
    public Short nivelConfianza;

    @Column(name = "comentario")
    public String comentario;
}
