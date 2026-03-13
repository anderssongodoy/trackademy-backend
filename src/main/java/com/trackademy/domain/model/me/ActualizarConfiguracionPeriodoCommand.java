package com.trackademy.domain.model.me;

import java.util.List;

public record ActualizarConfiguracionPeriodoCommand(
        Long campusId,
        Long carreraId,
        Integer cicloActual,
        List<Long> cursoIds
) {
}
