package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;

import java.time.LocalTime;
import java.util.List;

public record ActualizarHorarioCursoRequest(
        List<BloqueHorarioRequest> bloques
) {
    public ActualizarHorarioCursoCommand toCommand(Long usuarioPeriodoCursoId) {
        return new ActualizarHorarioCursoCommand(
                usuarioPeriodoCursoId,
                bloques == null ? List.of() : bloques.stream().map(BloqueHorarioRequest::toDomain).toList()
        );
    }

    public record BloqueHorarioRequest(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            Integer duracionMin,
            String tipoSesion,
            String ubicacion,
            String urlVirtual
    ) {
        public ActualizarHorarioCursoCommand.BloqueHorario toDomain() {
            return new ActualizarHorarioCursoCommand.BloqueHorario(
                    diaSemana,
                    horaInicio == null || horaInicio.isBlank() ? null : LocalTime.parse(horaInicio),
                    horaFin == null || horaFin.isBlank() ? null : LocalTime.parse(horaFin),
                    duracionMin,
                    tipoSesion,
                    ubicacion,
                    urlVirtual
            );
        }
    }
}
