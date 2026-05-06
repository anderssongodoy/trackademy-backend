package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_report")
public class FeedbackReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "tipo", nullable = false)
    public String tipo; // sugerencia, error, silabo_desactualizado, curso_faltante, otro

    @Column(name = "motivo", nullable = false, length = 255)
    public String motivo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    public String descripcion;

    @Column(name = "nombre_reportante", nullable = false, length = 255)
    public String nombreReportante;

    @Column(name = "email_reportante", nullable = false, length = 255)
    public String emailReportante;

    @Column(name = "whatsapp_reportante", length = 20)
    public String whatsappReportante;

    @Column(name = "imagen_url", length = 500)
    public String imagenUrl;

    @Column(name = "curso_id")
    public Long cursoId;

    @Column(name = "carrera_id")
    public Long carreraId;

    @Column(name = "ciclo")
    public Integer ciclo;

    @Column(name = "pagina_actual", length = 255)
    public String paginaActual;

    @Column(name = "fecha_reporte", nullable = false)
    public LocalDateTime fechaReporte;

    @Column(name = "numero_reporte", nullable = false, unique = true, length = 50)
    public String numeroReporte;

    @Column(name = "estado", nullable = false, length = 20)
    public String estado; // abierto, en_revision, resuelto, cerrado

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        fechaReporte = LocalDateTime.now();
        estado = "abierto";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
