package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiHorarioCurso;

import java.time.LocalTime;

public record MiHorarioCursoResponse(
        Long usuarioPeriodoCursoId,
        Long cursoId,
        String codigo,
        String nombre,
        Long campusId,
        String campusNombre,
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
    public static MiHorarioCursoResponse from(MiHorarioCurso m) {
        return new MiHorarioCursoResponse(
                m.usuarioPeriodoCursoId(),
                m.cursoId(),
                m.codigo(),
                m.nombre(),
                m.campusId(),
                m.campusNombre(),
                m.modalidad(),
                m.bloqueNro(),
                m.diaSemana(),
                m.horaInicio(),
                m.horaFin(),
                m.duracionMin(),
                m.tipoSesion(),
                m.ubicacion(),
                m.urlVirtual()
        );
    }
}
