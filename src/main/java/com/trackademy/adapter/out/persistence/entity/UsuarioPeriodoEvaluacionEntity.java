package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usuario_periodo_evaluacion")
public class UsuarioPeriodoEvaluacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_curso_id", nullable = false)
    public Long usuarioPeriodoCursoId;

    @Column(name = "silabo_evaluacion_id")
    public Long silaboEvaluacionId;

    @Column(name = "codigo")
    public String codigo;

    @Column(name = "semana")
    public Integer semana;

    @Column(name = "fecha_estimada")
    public LocalDate fechaEstimada;

    @Column(name = "fecha_real")
    public LocalDate fechaReal;

    @Column(name = "nota")
    public BigDecimal nota;

    @Column(name = "exonerado")
    public Boolean exonerado;

    @Column(name = "es_rezagado")
    public Boolean esRezagado;

    @Column(name = "reemplaza_a_id")
    public Long reemplazaAId;

    @Column(name = "estado_migracion", nullable = false)
    public String estadoMigracion = "activa";

    @Column(name = "comentarios")
    public String comentarios;
}
