package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "silabo_analysis_snapshot")
public class SilaboAnalysisSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "silabo_id", nullable = false)
    public Long silaboId;

    @Column(name = "hash_pdf", nullable = false)
    public String hashPdf;

    @Column(name = "resumen", nullable = false, columnDefinition = "text")
    public String resumen;

    @Column(name = "temas_json", nullable = false, columnDefinition = "text")
    public String temasJson;

    @Column(name = "recursos_json", nullable = false, columnDefinition = "text")
    public String recursosJson;

    @Column(name = "model", nullable = false)
    public String model;

    @Column(name = "prompt_tokens")
    public Integer promptTokens;

    @Column(name = "completion_tokens")
    public Integer completionTokens;

    @Column(name = "proximos_pasos_json", columnDefinition = "text")
    public String proximosPasosJson;

    @Column(name = "generated_at", nullable = false)
    public OffsetDateTime generatedAt;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}
