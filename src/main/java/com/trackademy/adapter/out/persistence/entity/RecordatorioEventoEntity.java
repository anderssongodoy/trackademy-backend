package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recordatorio_evento")
public class RecordatorioEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_id", nullable = false)
    public Long usuarioPeriodoId;

    @Column(name = "usuario_periodo_evaluacion_id")
    public Long usuarioPeriodoEvaluacionId;

    @Column(name = "agenda_evento_id")
    public Long agendaEventoId;

    @Column(name = "usuario_tarea_id")
    public Long usuarioTareaId;

    @Column(name = "fecha_envio", nullable = false)
    public OffsetDateTime fechaEnvio;

    @Column(name = "canal", nullable = false)
    public String canal;

    @Column(name = "estado", nullable = false)
    public String estado;

    @Column(name = "payload_json")
    public String payloadJson;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}
