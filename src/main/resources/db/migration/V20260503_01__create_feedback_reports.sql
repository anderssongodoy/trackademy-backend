CREATE TABLE feedback_report (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    nombre_reportante VARCHAR(255) NOT NULL,
    email_reportante VARCHAR(255) NOT NULL,
    whatsapp_reportante VARCHAR(20),
    imagen_url VARCHAR(500),
    curso_id BIGINT,
    carrera_id BIGINT,
    ciclo INTEGER,
    pagina_actual VARCHAR(255),
    fecha_reporte TIMESTAMPTZ NOT NULL DEFAULT now(),
    numero_reporte VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'abierto',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_feedback_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_feedback_curso FOREIGN KEY (curso_id) REFERENCES curso(id),
    CONSTRAINT fk_feedback_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id),
    CONSTRAINT feedback_tipo_check CHECK (tipo IN ('sugerencia', 'error', 'silabo_desactualizado', 'curso_faltante', 'otro')),
    CONSTRAINT feedback_estado_check CHECK (estado IN ('abierto', 'en_revision', 'resuelto', 'cerrado'))
);

CREATE INDEX IF NOT EXISTS idx_feedback_usuario_id ON feedback_report(usuario_id);
CREATE INDEX IF NOT EXISTS idx_feedback_estado ON feedback_report(estado);
CREATE INDEX IF NOT EXISTS idx_feedback_fecha ON feedback_report(fecha_reporte);
CREATE INDEX IF NOT EXISTS idx_feedback_numero ON feedback_report(numero_reporte);
