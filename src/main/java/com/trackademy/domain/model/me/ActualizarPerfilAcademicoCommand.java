package com.trackademy.domain.model.me;

import java.math.BigDecimal;

public record ActualizarPerfilAcademicoCommand(
        BigDecimal metaPromedioCiclo,
        Integer horasEstudioSemanaObjetivo
) {
}
