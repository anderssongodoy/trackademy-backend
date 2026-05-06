package com.trackademy.adapter.out.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "academic_radar_snapshot")
public class AcademicRadarSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "usuario_periodo_id", nullable = false)
    public Long usuarioPeriodoId;

    @Column(name = "input_hash", nullable = false)
    public String inputHash;

    @Column(name = "radar_version", nullable = false)
    public String radarVersion;

    @Column(name = "model")
    public String model;

    @Column(name = "ai_generated", nullable = false)
    public boolean aiGenerated;

    @Column(name = "prompt_tokens")
    public Integer promptTokens;

    @Column(name = "completion_tokens")
    public Integer completionTokens;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb", nullable = false)
    public JsonNode payloadJson;

    @Column(name = "generated_at", nullable = false)
    public OffsetDateTime generatedAt;

    @Column(name = "valid_until", nullable = false)
    public OffsetDateTime validUntil;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
