package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "usuario_preferencia_estudio")
public class UsuarioPreferenciaEstudioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_id", nullable = false)
    public Long usuarioPeriodoId;

    @Column(name = "dia_semana", nullable = false)
    public Short diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    public LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    public LocalTime horaFin;

    @Column(name = "prioridad", nullable = false)
    public Short prioridad;

    @Column(name = "tipo", nullable = false)
    public String tipo;
}
