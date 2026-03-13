package com.trackademy.domain.model.me;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarNotaEvaluacionCommand(
        Long usuarioPeriodoCursoId,
        String evaluacionCodigo,
        BigDecimal nota,
        LocalDate fechaReal,
        Boolean exonerado,
        Boolean esRezagado,
        String comentarios
) {
}
