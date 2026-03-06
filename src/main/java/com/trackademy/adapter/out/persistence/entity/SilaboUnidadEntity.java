package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "silabo_unidad")
public class SilaboUnidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "silabo_id", nullable = false)
    public Long silaboId;

    @Column(name = "nro", nullable = false)
    public Integer nro;

    @Column(name = "titulo")
    public String titulo;

    @Column(name = "semana_inicio")
    public Integer semanaInicio;

    @Column(name = "semana_fin")
    public Integer semanaFin;

    @Column(name = "logro_especifico")
    public String logroEspecifico;
}
