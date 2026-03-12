package com.trackademy.domain.model.me;

import java.time.LocalTime;

public record MiHorarioCurso(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigo,
        String nombre,
        String modalidad,
        Integer bloqueNro,
        Short diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Short duracionMin,
        String tipoSesion,
        String ubicacion,
        String urlVirtual
) {
}
