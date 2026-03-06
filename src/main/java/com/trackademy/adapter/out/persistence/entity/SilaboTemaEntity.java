package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "silabo_tema")
public class SilaboTemaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "silabo_unidad_id", nullable = false)
    public Long silaboUnidadId;

    @Column(name = "orden", nullable = false)
    public Integer orden;

    @Column(name = "titulo", nullable = false)
    public String titulo;
}
