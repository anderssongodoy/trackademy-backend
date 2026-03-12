package com.trackademy.domain.model.me;

import java.time.LocalTime;
import java.util.List;

public record ActualizarHorarioCursoCommand(
        Long usuarioPeriodoCursoId,
        List<BloqueHorario> bloques
) {
    public record BloqueHorario(
            Integer diaSemana,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer duracionMin,
            String tipoSesion,
            String ubicacion,
            String urlVirtual
    ) {
    }
}
