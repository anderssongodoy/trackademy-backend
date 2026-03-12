package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoHorarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.out.MeCommandPort;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class PostgresMeCommandAdapter implements MeCommandPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository;

    public PostgresMeCommandAdapter(
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional
    public ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command) {
        if (command == null || command.usuarioPeriodoCursoId() == null) {
            throw new IllegalArgumentException("Falta identificar el curso a actualizar.");
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un periodo activo para el usuario."));

        UsuarioPeriodoCursoEntity usuarioPeriodoCurso = usuarioPeriodoCursoRepository.findById(command.usuarioPeriodoCursoId());
        if (usuarioPeriodoCurso == null || !usuarioPeriodo.id.equals(usuarioPeriodoCurso.usuarioPeriodoId)) {
            throw new IllegalArgumentException("El curso no pertenece al periodo actual del usuario.");
        }

        horarioRepository.borrarPorUsuarioPeriodoCurso(usuarioPeriodoCurso.id);

        List<ActualizarHorarioCursoCommand.BloqueHorario> bloques = command.bloques() == null ? List.of() : command.bloques();
        int bloquesRegistrados = 0;
        for (ActualizarHorarioCursoCommand.BloqueHorario bloque : bloques) {
            validarBloque(bloque);
            UsuarioPeriodoCursoHorarioEntity entity = new UsuarioPeriodoCursoHorarioEntity();
            entity.usuarioPeriodoCursoId = usuarioPeriodoCurso.id;
            entity.bloqueNro = ++bloquesRegistrados;
            entity.diaSemana = bloque.diaSemana().shortValue();
            entity.horaInicio = bloque.horaInicio();
            entity.horaFin = bloque.horaFin();
            entity.duracionMin = bloque.duracionMin() == null ? (short) 45 : bloque.duracionMin().shortValue();
            entity.tipoSesion = limpiarTexto(bloque.tipoSesion());
            entity.ubicacion = limpiarTexto(bloque.ubicacion());
            entity.urlVirtual = limpiarTexto(bloque.urlVirtual());
            horarioRepository.persist(entity);
        }

        return new ActualizarHorarioCursoResult(usuarioPeriodoCurso.id, bloquesRegistrados);
    }

    private void validarBloque(ActualizarHorarioCursoCommand.BloqueHorario bloque) {
        if (bloque == null) {
            throw new IllegalArgumentException("Hay un bloque de horario vacio.");
        }
        if (bloque.diaSemana() == null || bloque.diaSemana() < 1 || bloque.diaSemana() > 7) {
            throw new IllegalArgumentException("Cada bloque debe indicar un dia valido.");
        }
        if (bloque.horaInicio() == null || bloque.horaFin() == null) {
            throw new IllegalArgumentException("Cada bloque debe indicar hora de inicio y fin.");
        }
        if (!bloque.horaFin().isAfter(bloque.horaInicio())) {
            throw new IllegalArgumentException("La hora fin debe ser mayor que la hora inicio.");
        }
        if (bloque.horaInicio().isBefore(LocalTime.of(7, 0))) {
            throw new IllegalArgumentException("La hora inicio no puede ser menor a 07:00.");
        }
    }

    private String limpiarTexto(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
