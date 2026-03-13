package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEvaluacionEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoHorarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoConfianzaPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.out.MeCommandPort;
import com.trackademy.domain.model.me.ActualizarConfiguracionPeriodoCommand;
import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;
import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgresMeCommandAdapter implements MeCommandPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final CursoPanacheRepository cursoRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository;
    private final UsuarioPeriodoCursoConfianzaPanacheRepository confianzaRepository;
    private final UsuarioPeriodoEvaluacionPanacheRepository usuarioPeriodoEvaluacionRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboEvaluacionPanacheRepository silaboEvaluacionRepository;
    private final PeriodoPanacheRepository periodoRepository;

    public PostgresMeCommandAdapter(
            UsuarioPanacheRepository usuarioRepository,
            CursoPanacheRepository cursoRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            UsuarioPeriodoCursoHorarioPanacheRepository horarioRepository,
            UsuarioPeriodoCursoConfianzaPanacheRepository confianzaRepository,
            UsuarioPeriodoEvaluacionPanacheRepository usuarioPeriodoEvaluacionRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboEvaluacionPanacheRepository silaboEvaluacionRepository,
            PeriodoPanacheRepository periodoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.horarioRepository = horarioRepository;
        this.confianzaRepository = confianzaRepository;
        this.usuarioPeriodoEvaluacionRepository = usuarioPeriodoEvaluacionRepository;
        this.silaboRepository = silaboRepository;
        this.silaboEvaluacionRepository = silaboEvaluacionRepository;
        this.periodoRepository = periodoRepository;
    }

    @Override
    @Transactional
    public MiPeriodoActual actualizarConfiguracionPeriodo(String email, ActualizarConfiguracionPeriodoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("No llegaron datos para reconfigurar el ciclo.");
        }
        if (command.campusId() == null) {
            throw new IllegalArgumentException("Debes elegir un campus.");
        }
        if (command.carreraId() == null) {
            throw new IllegalArgumentException("Debes elegir una carrera.");
        }
        if (command.cicloActual() == null || command.cicloActual() < 1 || command.cicloActual() > 12) {
            throw new IllegalArgumentException("El ciclo actual debe estar entre 1 y 12.");
        }
        List<Long> cursoIds = command.cursoIds() == null ? List.of() : command.cursoIds().stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (cursoIds.isEmpty()) {
            throw new IllegalArgumentException("Debes dejar al menos un curso en este ciclo.");
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un periodo activo para el usuario."));

        usuarioPeriodo.campusId = command.campusId();
        usuarioPeriodo.carreraId = command.carreraId();
        usuarioPeriodo.cicloActual = command.cicloActual();

        List<UsuarioPeriodoCursoEntity> existentes = usuarioPeriodoCursoRepository.listarPorUsuarioPeriodo(usuarioPeriodo.id);
        List<Long> existentesIds = existentes.stream().map(item -> item.cursoId).toList();

        for (UsuarioPeriodoCursoEntity existente : existentes) {
            if (cursoIds.contains(existente.cursoId)) {
                existente.activo = true;
                existente.estado = "matriculado";
                if (existente.modalidad == null) {
                    existente.modalidad = cursoRepository.findByIdOptional(existente.cursoId).map(c -> c.modalidad).orElse(null);
                }
                continue;
            }

            horarioRepository.borrarPorUsuarioPeriodoCurso(existente.id);
            confianzaRepository.borrarPorUsuarioPeriodoCurso(existente.id);
            usuarioPeriodoEvaluacionRepository.delete("usuarioPeriodoCursoId", existente.id);
            usuarioPeriodoCursoRepository.delete(existente);
        }

        for (Long cursoId : cursoIds) {
            if (existentesIds.contains(cursoId)) {
                continue;
            }

            CursoEntity curso = cursoRepository.findById(cursoId);
            if (curso == null) {
                throw new IllegalArgumentException("Uno de los cursos seleccionados ya no existe en catalogo.");
            }

            UsuarioPeriodoCursoEntity nuevo = new UsuarioPeriodoCursoEntity();
            nuevo.usuarioPeriodoId = usuarioPeriodo.id;
            nuevo.cursoId = cursoId;
            nuevo.estado = "matriculado";
            nuevo.activo = true;
            nuevo.origen = "reconfiguracion";
            nuevo.modalidad = curso.modalidad;
            usuarioPeriodoCursoRepository.persist(nuevo);
        }

        PeriodoEntity periodo = periodoRepository.findById(usuarioPeriodo.periodoId);

        return new MiPeriodoActual(
                usuario.id,
                usuarioPeriodo.id,
                usuarioPeriodo.periodoId,
                usuarioPeriodo.campusId,
                usuarioPeriodo.carreraId,
                usuarioPeriodo.cicloActual,
                usuarioPeriodo.onboardingEstado,
                usuarioPeriodo.onboardingCompletadoAt,
                usuarioPeriodo.metaPromedioCiclo,
                usuarioPeriodo.horasEstudioSemanaObjetivo,
                periodo != null ? periodo.etiqueta : null,
                periodo != null ? periodo.fechaInicio : null,
                periodo != null ? periodo.fechaFin : null
        );
    }

    @Override
    @Transactional
    public MiPeriodoActual actualizarPerfilAcademico(String email, ActualizarPerfilAcademicoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("No llegaron datos para actualizar el perfil.");
        }
        if (command.metaPromedioCiclo() == null || command.metaPromedioCiclo().compareTo(BigDecimal.ZERO) < 0 || command.metaPromedioCiclo().compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("La meta de promedio debe estar entre 0 y 20.");
        }
        if (command.horasEstudioSemanaObjetivo() == null || command.horasEstudioSemanaObjetivo() < 1 || command.horasEstudioSemanaObjetivo() > 80) {
            throw new IllegalArgumentException("Las horas objetivo deben estar entre 1 y 80.");
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un periodo activo para el usuario."));

        usuarioPeriodo.metaPromedioCiclo = command.metaPromedioCiclo();
        usuarioPeriodo.horasEstudioSemanaObjetivo = command.horasEstudioSemanaObjetivo();

        PeriodoEntity periodo = periodoRepository.findById(usuarioPeriodo.periodoId);

        return new MiPeriodoActual(
                usuario.id,
                usuarioPeriodo.id,
                usuarioPeriodo.periodoId,
                usuarioPeriodo.campusId,
                usuarioPeriodo.carreraId,
                usuarioPeriodo.cicloActual,
                usuarioPeriodo.onboardingEstado,
                usuarioPeriodo.onboardingCompletadoAt,
                usuarioPeriodo.metaPromedioCiclo,
                usuarioPeriodo.horasEstudioSemanaObjetivo,
                periodo != null ? periodo.etiqueta : null,
                periodo != null ? periodo.fechaInicio : null,
                periodo != null ? periodo.fechaFin : null
        );
    }

    @Override
    @Transactional
    public MiCurso actualizarDatosCurso(String email, ActualizarDatosCursoCommand command) {
        if (command == null || command.usuarioPeriodoCursoId() == null) {
            throw new IllegalArgumentException("Falta identificar el curso a actualizar.");
        }

        UsuarioPeriodoCursoEntity usuarioPeriodoCurso = validarAccesoCurso(email, command.usuarioPeriodoCursoId());
        CursoEntity curso = cursoRepository.findById(usuarioPeriodoCurso.cursoId);
        if (curso == null) {
            throw new IllegalArgumentException("No encontramos el curso asociado.");
        }

        usuarioPeriodoCurso.seccion = limpiarTexto(command.seccion());
        usuarioPeriodoCurso.profesor = limpiarTexto(command.profesor());

        return new MiCurso(
                usuarioPeriodoCurso.id,
                usuarioPeriodoCurso.cursoId,
                curso.codigo,
                curso.nombre,
                usuarioPeriodoCurso.estado,
                usuarioPeriodoCurso.activo,
                usuarioPeriodoCurso.seccion,
                usuarioPeriodoCurso.profesor,
                usuarioPeriodoCurso.modalidad != null ? usuarioPeriodoCurso.modalidad : curso.modalidad
        );
    }

    @Override
    @Transactional
    public ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command) {
        if (command == null || command.usuarioPeriodoCursoId() == null) {
            throw new IllegalArgumentException("Falta identificar el curso a actualizar.");
        }

        UsuarioPeriodoCursoEntity usuarioPeriodoCurso = validarAccesoCurso(email, command.usuarioPeriodoCursoId());

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

    @Override
    @Transactional
    public MiEvaluacionCurso registrarNotaEvaluacion(String email, RegistrarNotaEvaluacionCommand command) {
        if (command == null || command.usuarioPeriodoCursoId() == null) {
            throw new IllegalArgumentException("Falta identificar el curso de la evaluacion.");
        }
        if (command.evaluacionCodigo() == null || command.evaluacionCodigo().isBlank()) {
            throw new IllegalArgumentException("Falta el codigo de evaluacion.");
        }
        if (command.nota() != null && (command.nota().doubleValue() < 0 || command.nota().doubleValue() > 20)) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 20.");
        }

        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un periodo activo para el usuario."));

        UsuarioPeriodoCursoEntity usuarioPeriodoCurso = validarAccesoCurso(email, command.usuarioPeriodoCursoId());
        PeriodoEntity periodo = periodoRepository.findById(usuarioPeriodo.periodoId);

        Optional<SilaboEntity> silaboOpt = silaboRepository.buscarVigentePorCursoId(usuarioPeriodoCurso.cursoId);
        if (silaboOpt.isEmpty()) {
            throw new IllegalArgumentException("El curso no tiene un silabo vigente asociado.");
        }

        SilaboEvaluacionEntity silaboEvaluacion = silaboEvaluacionRepository.listarPorSilabo(silaboOpt.get().id).stream()
                .filter(item -> item.codigo != null && item.codigo.equalsIgnoreCase(command.evaluacionCodigo()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No encontramos esa evaluacion en el silabo vigente."));

        UsuarioPeriodoEvaluacionEntity evaluacion = usuarioPeriodoEvaluacionRepository
                .buscarPorUsuarioPeriodoCursoYCodigo(usuarioPeriodoCurso.id, silaboEvaluacion.codigo)
                .orElseGet(() -> {
                    UsuarioPeriodoEvaluacionEntity nueva = new UsuarioPeriodoEvaluacionEntity();
                    nueva.usuarioPeriodoCursoId = usuarioPeriodoCurso.id;
                    nueva.codigo = silaboEvaluacion.codigo;
                    nueva.exonerado = false;
                    nueva.esRezagado = false;
                    return nueva;
                });

        evaluacion.silaboEvaluacionId = silaboEvaluacion.id;
        evaluacion.semana = silaboEvaluacion.semana;
        evaluacion.fechaEstimada = construirFechaEstimada(periodo, silaboEvaluacion.semana, usuarioPeriodoCurso.id);
        evaluacion.fechaReal = command.fechaReal();
        evaluacion.nota = command.nota();
        evaluacion.exonerado = Boolean.TRUE.equals(command.exonerado());
        evaluacion.esRezagado = Boolean.TRUE.equals(command.esRezagado());
        evaluacion.comentarios = limpiarTexto(command.comentarios());

        if (evaluacion.id == null) {
            usuarioPeriodoEvaluacionRepository.persist(evaluacion);
        }

        return new MiEvaluacionCurso(
                evaluacion.id,
                usuarioPeriodoCurso.id,
                usuarioPeriodoCurso.cursoId,
                null,
                null,
                silaboEvaluacion.codigo,
                silaboEvaluacion.tipo,
                silaboEvaluacion.descripcion,
                silaboEvaluacion.porcentaje,
                evaluacion.semana,
                evaluacion.fechaEstimada,
                evaluacion.fechaReal,
                evaluacion.nota,
                evaluacion.exonerado,
                evaluacion.esRezagado,
                silaboEvaluacion.observacion,
                evaluacion.comentarios
        );
    }

    private UsuarioPeriodoCursoEntity validarAccesoCurso(String email, Long usuarioPeriodoCursoId) {
        UsuarioEntity usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos el usuario autenticado."));

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos un periodo activo para el usuario."));

        UsuarioPeriodoCursoEntity usuarioPeriodoCurso = usuarioPeriodoCursoRepository.findById(usuarioPeriodoCursoId);
        if (usuarioPeriodoCurso == null || !usuarioPeriodo.id.equals(usuarioPeriodoCurso.usuarioPeriodoId)) {
            throw new IllegalArgumentException("El curso no pertenece al periodo actual del usuario.");
        }
        return usuarioPeriodoCurso;
    }

    private LocalDate construirFechaEstimada(PeriodoEntity periodo, Integer semana, Long usuarioPeriodoCursoId) {
        if (periodo == null || periodo.fechaInicio == null || semana == null) {
            return null;
        }
        Short primerDia = horarioRepository.listarPorUsuarioPeriodoCursos(List.of(usuarioPeriodoCursoId)).stream()
                .map(item -> item.diaSemana)
                .filter(item -> item != null)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDate inicioSemana = periodo.fechaInicio.plusDays((long) (semana - 1) * 7L);
        return primerDia == null ? inicioSemana : inicioSemana.plusDays(primerDia - 1L);
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
