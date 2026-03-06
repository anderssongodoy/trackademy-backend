package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.*;
import com.trackademy.adapter.out.persistence.repository.*;
import com.trackademy.application.port.out.OnboardingCommandPort;
import com.trackademy.domain.model.onboarding.OnboardingCommand;
import com.trackademy.domain.model.onboarding.OnboardingResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PostgresOnboardingCommandAdapter implements OnboardingCommandPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository;
    private final UsuarioPreferenciaEstudioPanacheRepository preferenciaEstudioRepository;
    private final UsuarioPeriodoCursoConfianzaPanacheRepository confianzaRepository;

    public PostgresOnboardingCommandAdapter(
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository,
            UsuarioPreferenciaEstudioPanacheRepository preferenciaEstudioRepository,
            UsuarioPeriodoCursoConfianzaPanacheRepository confianzaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.horarioRepository = horarioRepository;
        this.preferenciaEstudioRepository = preferenciaEstudioRepository;
        this.confianzaRepository = confianzaRepository;
    }

    @Override
    @Transactional
    public OnboardingResult completarOnboardingBasico(OnboardingCommand command) {
        String email = required(command.email(), "email");
        required(command.periodoId(), "periodoId");
        required(command.campusId(), "campusId");
        if (command.cursos() == null || command.cursos().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un curso en onboarding.");
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseGet(() -> {
                    UsuarioEntity nuevo = new UsuarioEntity();
                    nuevo.email = email;
                    nuevo.nombre = command.nombre();
                    usuarioRepository.persist(nuevo);
                    return nuevo;
                });

        if (usuario.nombre == null && command.nombre() != null && !command.nombre().isBlank()) {
            usuario.nombre = command.nombre();
        }

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository
                .buscarPorUsuarioYPeriodo(usuario.id, command.periodoId())
                .orElseGet(() -> {
                    UsuarioPeriodoEntity nuevo = new UsuarioPeriodoEntity();
                    nuevo.usuarioId = usuario.id;
                    nuevo.periodoId = command.periodoId();
                    nuevo.onboardingEstado = "en_progreso";
                    usuarioPeriodoRepository.persist(nuevo);
                    return nuevo;
                });

        usuarioPeriodo.campusId = command.campusId();
        usuarioPeriodo.carreraId = command.carreraId();
        usuarioPeriodo.cicloActual = command.cicloActual();
        usuarioPeriodo.metaPromedioCiclo = command.metaPromedioCiclo();
        usuarioPeriodo.horasEstudioSemanaObjetivo = command.horasEstudioSemanaObjetivo();
        usuarioPeriodo.onboardingEstado = "completado";
        usuarioPeriodo.onboardingCompletadoAt = OffsetDateTime.now();

        int cursosRegistrados = 0;
        int horariosRegistrados = 0;

        List<UsuarioPeriodoCursoEntity> upcs = new ArrayList<>();

        for (OnboardingCommand.CursoSeleccionado cursoSeleccionado : command.cursos()) {
            Long cursoId = required(cursoSeleccionado.cursoId(), "cursoId");

            UsuarioPeriodoCursoEntity upc = usuarioPeriodoCursoRepository
                    .buscarPorUsuarioPeriodoYCurso(usuarioPeriodo.id, cursoId)
                    .orElseGet(() -> {
                        UsuarioPeriodoCursoEntity nuevo = new UsuarioPeriodoCursoEntity();
                        nuevo.usuarioPeriodoId = usuarioPeriodo.id;
                        nuevo.cursoId = cursoId;
                        nuevo.estado = "matriculado";
                        nuevo.activo = true;
                        nuevo.origen = "onboarding";
                        usuarioPeriodoCursoRepository.persist(nuevo);
                        return nuevo;
                    });

            upc.seccion = cursoSeleccionado.seccion();
            upc.profesor = cursoSeleccionado.profesor();
            upc.modalidad = cursoSeleccionado.modalidad();
            upc.estado = "matriculado";
            upc.activo = true;

            horarioRepository.borrarPorUsuarioPeriodoCurso(upc.id);
            int bloque = 1;
            if (cursoSeleccionado.horarios() != null) {
                for (OnboardingCommand.HorarioCurso h : cursoSeleccionado.horarios()) {
                    UsuarioPeriodoCursoHorarioEntity horario = new UsuarioPeriodoCursoHorarioEntity();
                    horario.usuarioPeriodoCursoId = upc.id;
                    horario.bloqueNro = bloque++;
                    horario.diaSemana = h.diaSemana() == null ? null : h.diaSemana().shortValue();
                    horario.horaInicio = parseTimeNullable(h.horaInicio());
                    horario.horaFin = parseTimeNullable(h.horaFin());
                    horario.duracionMin = 45;
                    horario.tipoSesion = h.tipoSesion();
                    horario.ubicacion = h.ubicacion();
                    horario.urlVirtual = h.urlVirtual();
                    horarioRepository.persist(horario);
                    horariosRegistrados++;
                }
            }

            upcs.add(upc);
            cursosRegistrados++;
        }

        int franjasRegistradas = 0;
        preferenciaEstudioRepository.borrarPorUsuarioPeriodo(usuarioPeriodo.id);
        if (command.franjasPreferidasEstudio() != null) {
            for (OnboardingCommand.FranjaEstudioPreferida f : command.franjasPreferidasEstudio()) {
                UsuarioPreferenciaEstudioEntity pref = new UsuarioPreferenciaEstudioEntity();
                pref.usuarioPeriodoId = usuarioPeriodo.id;
                pref.diaSemana = required(f.diaSemana(), "franja.diaSemana").shortValue();
                pref.horaInicio = LocalTime.parse(required(f.horaInicio(), "franja.horaInicio"));
                pref.horaFin = LocalTime.parse(required(f.horaFin(), "franja.horaFin"));
                pref.prioridad = (short) (f.prioridad() == null ? 1 : f.prioridad());
                preferenciaEstudioRepository.persist(pref);
                franjasRegistradas++;
            }
        }

        int confianzasRegistradas = 0;
        if (command.confianzaPorCurso() != null) {
            for (OnboardingCommand.ConfianzaCurso confianza : command.confianzaPorCurso()) {
                Long cursoId = required(confianza.cursoId(), "confianza.cursoId");
                UsuarioPeriodoCursoEntity target = upcs.stream()
                        .filter(x -> x.cursoId.equals(cursoId))
                        .findFirst()
                        .orElse(null);
                if (target == null) {
                    continue;
                }
                confianzaRepository.borrarPorUsuarioPeriodoCurso(target.id);
                UsuarioPeriodoCursoConfianzaEntity c = new UsuarioPeriodoCursoConfianzaEntity();
                c.usuarioPeriodoCursoId = target.id;
                c.nivelConfianza = required(confianza.nivelConfianza(), "confianza.nivel").shortValue();
                c.comentario = confianza.comentario();
                confianzaRepository.persist(c);
                confianzasRegistradas++;
            }
        }

        return new OnboardingResult(
                usuario.id,
                usuarioPeriodo.id,
                cursosRegistrados,
                horariosRegistrados,
                franjasRegistradas,
                confianzasRegistradas
        );
    }

    private LocalTime parseTimeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value);
    }

    private <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Campo requerido faltante: " + field);
        }
        return value;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Campo requerido faltante: " + field);
        }
        return value;
    }
}
