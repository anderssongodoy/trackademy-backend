package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "silabo_evaluacion")
public class SilaboEvaluacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "silabo_id", nullable = false)
    public Long silaboId;

    @Column(name = "codigo", nullable = false)
    public String codigo;

    @Column(name = "tipo")
    public String tipo;

    @Column(name = "descripcion")
    public String descripcion;

    @Column(name = "porcentaje")
    public BigDecimal porcentaje;

    @Column(name = "semana")
    public Integer semana;

    @Column(name = "observacion")
    public String observacion;
}
