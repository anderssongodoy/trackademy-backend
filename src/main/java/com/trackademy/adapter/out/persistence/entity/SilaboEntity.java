package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "silabo")
public class SilaboEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "curso_id", nullable = false)
    public Long cursoId;

    @Column(name = "version", nullable = false)
    public String version;

    @Column(name = "vigente", nullable = false)
    public Boolean vigente;

    @Column(name = "anio")
    public Integer anio;

    @Column(name = "periodo_texto")
    public String periodoTexto;

    @Column(name = "sumilla")
    public String sumilla;

    @Column(name = "fundamentacion")
    public String fundamentacion;

    @Column(name = "metodologia")
    public String metodologia;

    @Column(name = "logro_general")
    public String logroGeneral;
}
