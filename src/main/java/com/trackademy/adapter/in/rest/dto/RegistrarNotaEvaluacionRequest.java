package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarNotaEvaluacionRequest(
        BigDecimal nota,
        LocalDate fechaReal,
        Boolean exonerado,
        Boolean esRezagado,
        String comentarios
) {
    public RegistrarNotaEvaluacionCommand toCommand(Long usuarioPeriodoCursoId, String evaluacionCodigo) {
        return new RegistrarNotaEvaluacionCommand(
                usuarioPeriodoCursoId,
                evaluacionCodigo,
                nota,
                fechaReal,
                exonerado,
                esRezagado,
                comentarios
        );
    }
}
