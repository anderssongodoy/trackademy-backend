package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoHorarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PostgresMeQueryAdapter implements MeQueryPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final UsuarioPeriodoCursoHorarioPanacheRepository usuarioPeriodoCursoHorarioRepository;
    private final CursoPanacheRepository cursoRepository;
    private final PeriodoPanacheRepository periodoRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboEvaluacionPanacheRepository silaboEvaluacionRepository;

    public PostgresMeQueryAdapter(
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            UsuarioPeriodoCursoHorarioPanacheRepository usuarioPeriodoCursoHorarioRepository,
            CursoPanacheRepository cursoRepository,
            PeriodoPanacheRepository periodoRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboEvaluacionPanacheRepository silaboEvaluacionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.usuarioPeriodoCursoHorarioRepository = usuarioPeriodoCursoHorarioRepository;
        this.cursoRepository = cursoRepository;
        this.periodoRepository = periodoRepository;
        this.silaboRepository = silaboRepository;
        this.silaboEvaluacionRepository = silaboEvaluacionRepository;
    }

    @Override
    public Optional<MiPeriodoActual> obtenerPeriodoActual(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        return usuarioPeriodoOpt.map(up -> {
            PeriodoEntity periodo = periodoRepository.findById(up.periodoId);
            return new MiPeriodoActual(
                    up.usuarioId,
                    up.id,
                    up.periodoId,
                    up.campusId,
                    up.carreraId,
                    up.cicloActual,
                    up.onboardingEstado,
                    up.onboardingCompletadoAt,
                    up.metaPromedioCiclo,
                    up.horasEstudioSemanaObjetivo,
                    periodo == null ? null : periodo.etiqueta,
                    periodo == null ? null : periodo.fechaInicio,
                    periodo == null ? null : periodo.fechaFin
            );
        });
    }

    @Override
    public List<MiCurso> listarMisCursos(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        if (usuarioPeriodoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<UsuarioPeriodoCursoEntity> upcs = usuarioPeriodoCursoRepository.listarPorUsuarioPeriodo(usuarioPeriodoOpt.get().id);
        if (upcs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> cursoIds = upcs.stream().map(x -> x.cursoId).toList();
        Map<Long, CursoEntity> cursoById = new HashMap<>();
        for (CursoEntity c : cursoRepository.listarPorIds(cursoIds)) {
            cursoById.put(c.id, c);
        }

        return upcs.stream().map(upc -> {
            CursoEntity c = cursoById.get(upc.cursoId);
            return new MiCurso(
                    upc.id,
                    upc.cursoId,
                    c == null ? null : c.codigo,
                    c == null ? null : c.nombre,
                    upc.estado,
                    upc.activo,
                    upc.seccion,
                    upc.profesor,
                    upc.modalidad
            );
        }).toList();
    }

    @Override
    public List<MiHorarioCurso> listarMisHorarios(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        if (usuarioPeriodoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<UsuarioPeriodoCursoEntity> upcs = usuarioPeriodoCursoRepository.listarPorUsuarioPeriodo(usuarioPeriodoOpt.get().id);
        if (upcs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> upcIds = upcs.stream().map(x -> x.id).toList();
        List<UsuarioPeriodoCursoHorarioEntity> horarios = usuarioPeriodoCursoHorarioRepository.listarPorUsuarioPeriodoCursos(upcIds);
        if (horarios.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UsuarioPeriodoCursoEntity> upcById = new HashMap<>();
        for (UsuarioPeriodoCursoEntity upc : upcs) {
            upcById.put(upc.id, upc);
        }

        List<Long> cursoIds = upcs.stream().map(x -> x.cursoId).toList();
        Map<Long, CursoEntity> cursoById = new HashMap<>();
        for (CursoEntity c : cursoRepository.listarPorIds(cursoIds)) {
            cursoById.put(c.id, c);
        }

        return horarios.stream().map(h -> {
            UsuarioPeriodoCursoEntity upc = upcById.get(h.usuarioPeriodoCursoId);
            CursoEntity c = upc == null ? null : cursoById.get(upc.cursoId);
            return new MiHorarioCurso(
                    h.usuarioPeriodoCursoId,
                    upc == null ? null : upc.cursoId,
                    c == null ? null : c.codigo,
                    c == null ? null : c.nombre,
                    c == null ? null : c.modalidad,
                    h.bloqueNro,
                    h.diaSemana,
                    h.horaInicio,
                    h.horaFin,
                    h.duracionMin,
                    h.tipoSesion,
                    h.ubicacion,
                    h.urlVirtual
            );
        }).toList();
    }

    @Override
    public List<MiEvaluacionCurso> listarMisEvaluaciones(String email, Long cursoId) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        if (usuarioPeriodoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoOpt.get();
        PeriodoEntity periodo = periodoRepository.findById(usuarioPeriodo.periodoId);

        List<UsuarioPeriodoCursoEntity> upcs = usuarioPeriodoCursoRepository.listarPorUsuarioPeriodo(usuarioPeriodo.id);
        if (cursoId != null) {
            upcs = upcs.stream().filter(upc -> upc.cursoId != null && upc.cursoId.equals(cursoId)).toList();
        }
        if (upcs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> cursoIds = upcs.stream().map(x -> x.cursoId).toList();
        Map<Long, CursoEntity> cursoById = new HashMap<>();
        for (CursoEntity c : cursoRepository.listarPorIds(cursoIds)) {
            cursoById.put(c.id, c);
        }

        Map<Long, List<UsuarioPeriodoCursoHorarioEntity>> horariosByUpc = new HashMap<>();
        List<Long> upcIds = upcs.stream().map(x -> x.id).toList();
        for (UsuarioPeriodoCursoHorarioEntity h : usuarioPeriodoCursoHorarioRepository.listarPorUsuarioPeriodoCursos(upcIds)) {
            horariosByUpc.computeIfAbsent(h.usuarioPeriodoCursoId, key -> new java.util.ArrayList<>()).add(h);
        }

        List<MiEvaluacionCurso> evaluaciones = new java.util.ArrayList<>();

        for (UsuarioPeriodoCursoEntity upc : upcs) {
            CursoEntity curso = cursoById.get(upc.cursoId);
            Optional<SilaboEntity> silaboOpt = silaboRepository.buscarVigentePorCursoId(upc.cursoId);
            if (silaboOpt.isEmpty()) {
                continue;
            }

            List<SilaboEvaluacionEntity> evaluacionesSilabo = silaboEvaluacionRepository.listarPorSilabo(silaboOpt.get().id);
            if (evaluacionesSilabo.isEmpty()) {
                continue;
            }

            LocalDate fechaInicio = periodo == null ? null : periodo.fechaInicio;

            Short diaPreferido = null;
            List<UsuarioPeriodoCursoHorarioEntity> horariosCurso = horariosByUpc.getOrDefault(upc.id, List.of());
            if (!horariosCurso.isEmpty()) {
                diaPreferido = horariosCurso.stream()
                        .map(h -> h.diaSemana)
                        .filter(dia -> dia != null)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
            }

            for (SilaboEvaluacionEntity eval : evaluacionesSilabo) {
                LocalDate fechaEstimada = null;
                if (fechaInicio != null && eval.semana != null) {
                    LocalDate inicioSemana = fechaInicio.plusDays((long) (eval.semana - 1) * 7L);
                    if (diaPreferido != null) {
                        fechaEstimada = inicioSemana.plusDays(diaPreferido - 1L);
                    } else {
                        fechaEstimada = inicioSemana;
                    }
                }

                evaluaciones.add(new MiEvaluacionCurso(
                        upc.id,
                        upc.cursoId,
                        curso == null ? null : curso.codigo,
                        curso == null ? null : curso.nombre,
                        eval.codigo,
                        eval.tipo,
                        eval.descripcion,
                        eval.porcentaje,
                        eval.semana,
                        fechaEstimada,
                        eval.observacion
                ));
            }
        }

        evaluaciones.sort(Comparator
                .comparing(MiEvaluacionCurso::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MiEvaluacionCurso::semana, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MiEvaluacionCurso::evaluacionCodigo, Comparator.nullsLast(Comparator.naturalOrder())));

        return evaluaciones;
    }
}
