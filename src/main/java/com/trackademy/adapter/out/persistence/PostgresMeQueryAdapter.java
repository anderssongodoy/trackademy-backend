package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CampusEntity;
import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEventoEntity;
import com.trackademy.adapter.out.persistence.entity.RecordatorioEventoEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.CalendarSyncAccountEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoHorarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioTareaEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.CalendarSyncAccountPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.CampusPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoEventoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.RecordatorioEventoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoHorarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioTareaPanacheRepository;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiCalendarSyncAccount;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiDashboardResumen;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiEvaluacionesCursoResumen;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiRecordatorio;
import com.trackademy.domain.model.me.MiTarea;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PostgresMeQueryAdapter implements MeQueryPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final CalendarSyncAccountPanacheRepository calendarSyncAccountRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final UsuarioPeriodoCursoHorarioPanacheRepository usuarioPeriodoCursoHorarioRepository;
    private final UsuarioPeriodoEvaluacionPanacheRepository usuarioPeriodoEvaluacionRepository;
    private final UsuarioTareaPanacheRepository usuarioTareaRepository;
    private final RecordatorioEventoPanacheRepository recordatorioEventoRepository;
    private final CampusPanacheRepository campusRepository;
    private final CursoPanacheRepository cursoRepository;
    private final PeriodoPanacheRepository periodoRepository;
    private final PeriodoEventoPanacheRepository periodoEventoRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboEvaluacionPanacheRepository silaboEvaluacionRepository;

    public PostgresMeQueryAdapter(
            UsuarioPanacheRepository usuarioRepository,
            CalendarSyncAccountPanacheRepository calendarSyncAccountRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            UsuarioPeriodoCursoHorarioPanacheRepository usuarioPeriodoCursoHorarioRepository,
            UsuarioPeriodoEvaluacionPanacheRepository usuarioPeriodoEvaluacionRepository,
            UsuarioTareaPanacheRepository usuarioTareaRepository,
            RecordatorioEventoPanacheRepository recordatorioEventoRepository,
            CampusPanacheRepository campusRepository,
            CursoPanacheRepository cursoRepository,
            PeriodoPanacheRepository periodoRepository,
            PeriodoEventoPanacheRepository periodoEventoRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboEvaluacionPanacheRepository silaboEvaluacionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.calendarSyncAccountRepository = calendarSyncAccountRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.usuarioPeriodoCursoHorarioRepository = usuarioPeriodoCursoHorarioRepository;
        this.usuarioPeriodoEvaluacionRepository = usuarioPeriodoEvaluacionRepository;
        this.usuarioTareaRepository = usuarioTareaRepository;
        this.recordatorioEventoRepository = recordatorioEventoRepository;
        this.campusRepository = campusRepository;
        this.cursoRepository = cursoRepository;
        this.periodoRepository = periodoRepository;
        this.periodoEventoRepository = periodoEventoRepository;
        this.silaboRepository = silaboRepository;
        this.silaboEvaluacionRepository = silaboEvaluacionRepository;
    }

    @Override
    public Optional<MiPeriodoActual> obtenerPeriodoActual(String email) {
        return obtenerContexto(email).map(this::toPeriodoActual);
    }

    @Override
    public Optional<MiDashboardResumen> obtenerDashboard(String email) {
        Optional<ContextoActual> contextoOpt = obtenerContexto(email);
        if (contextoOpt.isEmpty()) {
            return Optional.empty();
        }

        ContextoActual contexto = contextoOpt.get();
        List<MiEvaluacionCurso> evaluaciones = construirEvaluaciones(contexto, null);
        List<MiHorarioCurso> horarios = construirHorarios(contexto);
        LocalDate hoy = LocalDate.now();
        List<MiCalendarioEvento> calendario = construirCalendario(contexto, hoy, hoy.plusDays(30));

        List<MiEvaluacionCurso> proximasEvaluaciones = evaluaciones.stream()
                .filter(item -> item.fechaEstimada() != null && !item.fechaEstimada().isBefore(hoy))
                .sorted(Comparator.comparing(MiEvaluacionCurso::fechaEstimada))
                .limit(6)
                .toList();

        List<MiCalendarioEvento> proximasSesiones = calendario.stream()
                .filter(item -> "horario".equals(item.origen()))
                .sorted(Comparator.comparing(MiCalendarioEvento::inicio))
                .limit(6)
                .toList();

        List<MiCalendarioEvento> proximosEventosPeriodo = calendario.stream()
                .filter(item -> "periodo".equals(item.origen()))
                .sorted(Comparator.comparing(MiCalendarioEvento::inicio))
                .limit(6)
                .toList();

        long notasRegistradas = evaluaciones.stream().filter(item -> item.nota() != null).count();
        long evaluacionesPendientes = evaluaciones.stream().filter(item -> item.nota() == null).count();
        long cursosActivos = contexto.upcs().stream().filter(upc -> Boolean.TRUE.equals(upc.activo)).count();
        long horariosRegistrados = horarios.size();

        return Optional.of(new MiDashboardResumen(
                toPeriodoActual(contexto),
                calcularSemanaActual(contexto.periodo()),
                calcularProgresoPeriodo(contexto.periodo()),
                cursosActivos,
                horariosRegistrados,
                evaluacionesPendientes,
                notasRegistradas,
                proximasEvaluaciones,
                proximasSesiones,
                proximosEventosPeriodo
        ));
    }

    @Override
    public List<MiCurso> listarMisCursos(String email) {
        return obtenerContexto(email)
                .map(this::construirCursos)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<MiHorarioCurso> listarMisHorarios(String email) {
        return obtenerContexto(email)
                .map(this::construirHorarios)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<MiEvaluacionCurso> listarMisEvaluaciones(String email, Long cursoId) {
        return obtenerContexto(email)
                .map(contexto -> construirEvaluaciones(contexto, cursoId))
                .orElse(Collections.emptyList());
    }

    @Override
    public MiEvaluacionesCursoResumen obtenerResumenEvaluaciones(String email, Long cursoId) {
        List<MiEvaluacionCurso> evaluaciones = listarMisEvaluaciones(email, cursoId);
        return construirResumenEvaluaciones(evaluaciones);
    }

    @Override
    public List<MiCalendarioEvento> listarCalendario(String email, LocalDate from, LocalDate to) {
        Optional<ContextoActual> contextoOpt = obtenerContexto(email);
        if (contextoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        ContextoActual contexto = contextoOpt.get();
        LocalDate inicio = from != null ? from : fechaInicioPorDefecto(contexto.periodo());
        LocalDate fin = to != null ? to : inicio.plusDays(30);
        if (fin.isBefore(inicio)) {
            LocalDate swap = inicio;
            inicio = fin;
            fin = swap;
        }

        return construirCalendario(contexto, inicio, fin);
    }

    @Override
    public List<MiTarea> listarMisTareas(String email) {
        Optional<ContextoActual> contextoOpt = obtenerContexto(email);
        if (contextoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        ContextoActual contexto = contextoOpt.get();
        Map<Long, UsuarioTareaEntity> tareasById = new HashMap<>();
        for (UsuarioTareaEntity tarea : usuarioTareaRepository.listarPorUsuarioPeriodo(contexto.usuarioPeriodo().id)) {
            tareasById.put(tarea.id, tarea);
        }

        Map<Long, RecordatorioEventoEntity> remindersByTaskId = new HashMap<>();
        for (RecordatorioEventoEntity reminder : recordatorioEventoRepository.listarPorUsuarioPeriodo(
                contexto.usuarioPeriodo().id,
                OffsetDateTime.now().minusYears(1),
                OffsetDateTime.now().plusYears(5)
        )) {
            if (reminder.usuarioTareaId != null
                    && "pendiente".equalsIgnoreCase(reminder.estado)
                    && !remindersByTaskId.containsKey(reminder.usuarioTareaId)) {
                remindersByTaskId.put(reminder.usuarioTareaId, reminder);
            }
        }

        return tareasById.values().stream()
                .map(tarea -> toMiTarea(tarea, remindersByTaskId.get(tarea.id), contexto))
                .sorted(Comparator
                        .comparing(MiTarea::fechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MiTarea::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<MiRecordatorio> listarMisRecordatorios(String email, LocalDate from, LocalDate to) {
        Optional<ContextoActual> contextoOpt = obtenerContexto(email);
        if (contextoOpt.isEmpty()) {
            return Collections.emptyList();
        }

        ContextoActual contexto = contextoOpt.get();
        OffsetDateTime inicio = (from != null ? from : LocalDate.now()).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime fin = (to != null ? to : inicio.toLocalDate().plusDays(30))
                .atTime(LocalTime.MAX)
                .atOffset(OffsetDateTime.now().getOffset());

        Map<Long, UsuarioTareaEntity> tareasById = new HashMap<>();
        for (UsuarioTareaEntity tarea : usuarioTareaRepository.listarPorUsuarioPeriodo(contexto.usuarioPeriodo().id)) {
            tareasById.put(tarea.id, tarea);
        }

        return recordatorioEventoRepository.listarPorUsuarioPeriodo(contexto.usuarioPeriodo().id, inicio, fin).stream()
                .filter(item -> item.usuarioTareaId != null && "pendiente".equalsIgnoreCase(item.estado))
                .map(item -> toMiRecordatorio(item, tareasById.get(item.usuarioTareaId), contexto))
                .filter(item -> item != null)
                .toList();
    }

    @Override
    public List<MiCalendarSyncAccount> listarSincronizacionesCalendario(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CalendarSyncAccountEntity> accountsByProvider = new HashMap<>();
        for (CalendarSyncAccountEntity entity : calendarSyncAccountRepository.listarPorUsuario(usuarioOpt.get().id)) {
            accountsByProvider.put(entity.provider, entity);
        }

        List<MiCalendarSyncAccount> accounts = new ArrayList<>();
        accounts.add(toCalendarSyncAccount("google", accountsByProvider.get("google")));
        accounts.add(toCalendarSyncAccount("microsoft", accountsByProvider.get("microsoft")));
        return accounts;
    }

    private Optional<ContextoActual> obtenerContexto(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        if (usuarioPeriodoOpt.isEmpty()) {
            return Optional.empty();
        }

        UsuarioPeriodoEntity usuarioPeriodo = usuarioPeriodoOpt.get();
        PeriodoEntity periodo = periodoRepository.findById(usuarioPeriodo.periodoId);
        List<UsuarioPeriodoCursoEntity> upcs = usuarioPeriodoCursoRepository.listarPorUsuarioPeriodo(usuarioPeriodo.id);
        Map<Long, UsuarioPeriodoCursoEntity> upcById = new HashMap<>();
        for (UsuarioPeriodoCursoEntity upc : upcs) {
            upcById.put(upc.id, upc);
        }

        Map<Long, CursoEntity> cursoById = new HashMap<>();
        if (!upcs.isEmpty()) {
            List<Long> cursoIds = upcs.stream().map(item -> item.cursoId).toList();
            for (CursoEntity curso : cursoRepository.listarPorIds(cursoIds)) {
                cursoById.put(curso.id, curso);
            }
        }

        Map<Long, List<UsuarioPeriodoCursoHorarioEntity>> horariosByUpc = new HashMap<>();
        Map<String, UsuarioPeriodoEvaluacionEntity> evaluacionesByKey = new HashMap<>();
        if (!upcs.isEmpty()) {
            List<Long> upcIds = upcs.stream().map(item -> item.id).toList();
            for (UsuarioPeriodoCursoHorarioEntity horario : usuarioPeriodoCursoHorarioRepository.listarPorUsuarioPeriodoCursos(upcIds)) {
                horariosByUpc.computeIfAbsent(horario.usuarioPeriodoCursoId, key -> new ArrayList<>()).add(horario);
            }
            for (UsuarioPeriodoEvaluacionEntity evaluacion : usuarioPeriodoEvaluacionRepository.listarPorUsuarioPeriodoCursos(upcIds)) {
                evaluacionesByKey.put(evaluationKey(evaluacion.usuarioPeriodoCursoId, evaluacion.codigo), evaluacion);
            }
        }

        CampusEntity campus = usuarioPeriodo.campusId == null ? null : campusRepository.findById(usuarioPeriodo.campusId);

        return Optional.of(new ContextoActual(usuarioOpt.get(), usuarioPeriodo, campus, periodo, upcs, upcById, cursoById, horariosByUpc, evaluacionesByKey));
    }

    private MiPeriodoActual toPeriodoActual(ContextoActual contexto) {
        UsuarioPeriodoEntity up = contexto.usuarioPeriodo();
        PeriodoEntity periodo = contexto.periodo();
        return new MiPeriodoActual(
                contexto.usuario().id,
                contexto.usuario().nombre,
                contexto.usuario().nombrePreferido,
                contexto.usuario().emailInstitucional,
                up.id,
                up.periodoId,
                up.campusId,
                contexto.campus() == null ? null : contexto.campus().nombre,
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
    }

    private List<MiCurso> construirCursos(ContextoActual contexto) {
        return contexto.upcs().stream().map(upc -> {
            CursoEntity c = contexto.cursoById().get(upc.cursoId);
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

    private List<MiHorarioCurso> construirHorarios(ContextoActual contexto) {
        List<MiHorarioCurso> horarios = new ArrayList<>();
        for (UsuarioPeriodoCursoEntity upc : contexto.upcs()) {
            CursoEntity curso = contexto.cursoById().get(upc.cursoId);
            for (UsuarioPeriodoCursoHorarioEntity horario : contexto.horariosByUpc().getOrDefault(upc.id, List.of())) {
                horarios.add(new MiHorarioCurso(
                        horario.usuarioPeriodoCursoId,
                        upc.cursoId,
                        curso == null ? null : curso.codigo,
                        curso == null ? null : curso.nombre,
                        contexto.usuarioPeriodo().campusId,
                        contexto.campus() == null ? null : contexto.campus().nombre,
                        curso == null ? null : curso.modalidad,
                        horario.bloqueNro,
                        horario.diaSemana,
                        horario.horaInicio,
                        horario.horaFin,
                        horario.duracionMin,
                        horario.tipoSesion,
                        horario.ubicacion,
                        horario.urlVirtual
                ));
            }
        }
        return horarios;
    }

    private List<MiEvaluacionCurso> construirEvaluaciones(ContextoActual contexto, Long cursoId) {
        List<MiEvaluacionCurso> evaluaciones = new ArrayList<>();

        List<UsuarioPeriodoCursoEntity> upcs = contexto.upcs();
        if (cursoId != null) {
            upcs = upcs.stream().filter(upc -> upc.cursoId != null && upc.cursoId.equals(cursoId)).toList();
        }

        for (UsuarioPeriodoCursoEntity upc : upcs) {
            CursoEntity curso = contexto.cursoById().get(upc.cursoId);
            Optional<SilaboEntity> silaboOpt = resolveSilabo(upc);
            if (silaboOpt.isEmpty()) {
                continue;
            }

            List<SilaboEvaluacionEntity> evaluacionesSilabo = silaboEvaluacionRepository.listarPorSilabo(silaboOpt.get().id);
            if (evaluacionesSilabo.isEmpty()) {
                continue;
            }

            Short diaPreferido = contexto.horariosByUpc().getOrDefault(upc.id, List.of()).stream()
                    .map(horario -> horario.diaSemana)
                    .filter(dia -> dia != null)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            for (SilaboEvaluacionEntity evaluacionSilabo : evaluacionesSilabo) {
                UsuarioPeriodoEvaluacionEntity evaluacionGuardada = contexto.evaluacionesByKey().get(evaluationKey(upc.id, evaluacionSilabo.codigo));
                LocalDate fechaEstimada = evaluacionGuardada != null && evaluacionGuardada.fechaEstimada != null
                        ? evaluacionGuardada.fechaEstimada
                        : construirFechaEstimada(contexto.periodo(), evaluacionSilabo.semana, diaPreferido);

                evaluaciones.add(new MiEvaluacionCurso(
                        evaluacionGuardada == null ? null : evaluacionGuardada.id,
                        upc.id,
                        upc.cursoId,
                        curso == null ? null : curso.codigo,
                        curso == null ? null : curso.nombre,
                        evaluacionSilabo.codigo,
                        evaluacionSilabo.tipo,
                        evaluacionSilabo.descripcion,
                        evaluacionSilabo.porcentaje,
                        evaluacionGuardada != null && evaluacionGuardada.semana != null ? evaluacionGuardada.semana : evaluacionSilabo.semana,
                        fechaEstimada,
                        evaluacionGuardada == null ? null : evaluacionGuardada.fechaReal,
                        evaluacionGuardada == null ? null : evaluacionGuardada.nota,
                        evaluacionGuardada == null ? Boolean.FALSE : evaluacionGuardada.exonerado,
                        evaluacionGuardada == null ? Boolean.FALSE : evaluacionGuardada.esRezagado,
                        evaluacionSilabo.observacion,
                        evaluacionGuardada == null ? null : evaluacionGuardada.comentarios
                ));
            }
        }

        evaluaciones.sort(Comparator
                .comparing(MiEvaluacionCurso::fechaEstimada, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MiEvaluacionCurso::semana, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MiEvaluacionCurso::evaluacionCodigo, Comparator.nullsLast(Comparator.naturalOrder())));
        return evaluaciones;
    }

    private MiEvaluacionesCursoResumen construirResumenEvaluaciones(List<MiEvaluacionCurso> evaluaciones) {
        BigDecimal acumulado = BigDecimal.ZERO;
        BigDecimal porcentajeEvaluado = BigDecimal.ZERO;
        long registradas = 0;
        long pendientes = 0;

        for (MiEvaluacionCurso evaluacion : evaluaciones) {
            if (evaluacion.nota() == null) {
                pendientes++;
                continue;
            }

            registradas++;
            if (evaluacion.porcentaje() == null) {
                continue;
            }

            porcentajeEvaluado = porcentajeEvaluado.add(evaluacion.porcentaje());
            acumulado = acumulado.add(evaluacion.nota()
                    .multiply(evaluacion.porcentaje())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        }

        return new MiEvaluacionesCursoResumen(
                acumulado.setScale(2, RoundingMode.HALF_UP),
                porcentajeEvaluado.setScale(2, RoundingMode.HALF_UP),
                registradas,
                pendientes,
                evaluaciones
        );
    }

    private List<MiCalendarioEvento> construirCalendario(ContextoActual contexto, LocalDate from, LocalDate to) {
        List<MiCalendarioEvento> eventos = new ArrayList<>();

        if (contexto.periodo() != null) {
            for (PeriodoEventoEntity evento : periodoEventoRepository.listarPorPeriodo(contexto.periodo().id)) {
                LocalDate fechaFin = evento.fechaFin == null ? evento.fechaInicio : evento.fechaFin;
                if (fechaFin.isBefore(from) || evento.fechaInicio.isAfter(to)) {
                    continue;
                }
                eventos.add(new MiCalendarioEvento(
                        "periodo",
                        evento.tipo,
                        evento.titulo,
                        evento.descripcion,
                        evento.fechaInicio.atStartOfDay(),
                        fechaFin.atTime(23, 59),
                        true,
                        null,
                        null,
                        null,
                        null,
                        evento.tipo
                ));
            }
        }

        for (MiEvaluacionCurso evaluacion : construirEvaluaciones(contexto, null)) {
            if (evaluacion.fechaEstimada() == null || evaluacion.fechaEstimada().isBefore(from) || evaluacion.fechaEstimada().isAfter(to)) {
                continue;
            }
            eventos.add(new MiCalendarioEvento(
                    "evaluacion",
                    evaluacion.evaluacionCodigo(),
                    evaluacion.descripcion() != null ? evaluacion.descripcion() : evaluacion.evaluacionCodigo(),
                    evaluacion.nombreCurso(),
                    evaluacion.fechaEstimada().atStartOfDay(),
                    evaluacion.fechaEstimada().atTime(23, 59),
                    true,
                    evaluacion.usuarioPeriodoCursoId(),
                    evaluacion.cursoId(),
                    evaluacion.codigoCurso(),
                    evaluacion.nombreCurso(),
                    evaluacion.evaluacionCodigo()
            ));
        }

        if (contexto.periodo() != null && contexto.periodo().fechaInicio != null && contexto.periodo().fechaFin != null) {
            LocalDate start = from.isBefore(contexto.periodo().fechaInicio) ? contexto.periodo().fechaInicio : from;
            LocalDate end = to.isAfter(contexto.periodo().fechaFin) ? contexto.periodo().fechaFin : to;
            if (!end.isBefore(start)) {
                for (MiHorarioCurso horario : construirHorarios(contexto)) {
                    if (horario.diaSemana() == null || horario.horaInicio() == null || horario.horaFin() == null) {
                        continue;
                    }
                    LocalDate primeraFecha = alinearFecha(start, horario.diaSemana());
                    for (LocalDate fecha = primeraFecha; !fecha.isAfter(end); fecha = fecha.plusWeeks(1)) {
                        eventos.add(new MiCalendarioEvento(
                                "horario",
                                horario.tipoSesion(),
                                horario.nombre(),
                                horario.tipoSesion(),
                                LocalDateTime.of(fecha, horario.horaInicio()),
                                LocalDateTime.of(fecha, horario.horaFin()),
                                false,
                                horario.usuarioPeriodoCursoId(),
                                horario.cursoId(),
                                horario.codigo(),
                                horario.nombre(),
                                horario.tipoSesion()
                        ));
                    }
                }
            }
        }

        eventos.sort(Comparator.comparing(MiCalendarioEvento::inicio));
        return eventos;
    }

    private LocalDate fechaInicioPorDefecto(PeriodoEntity periodo) {
        if (periodo != null && periodo.fechaInicio != null) {
            return periodo.fechaInicio;
        }
        return LocalDate.now().withDayOfMonth(1);
    }

    private Integer calcularSemanaActual(PeriodoEntity periodo) {
        if (periodo == null || periodo.fechaInicio == null) {
            return null;
        }
        long diff = java.time.temporal.ChronoUnit.DAYS.between(periodo.fechaInicio, LocalDate.now());
        if (diff < 0) {
            return 0;
        }
        return (int) (diff / 7) + 1;
    }

    private Integer calcularProgresoPeriodo(PeriodoEntity periodo) {
        if (periodo == null || periodo.fechaInicio == null || periodo.fechaFin == null) {
            return null;
        }
        long total = java.time.temporal.ChronoUnit.DAYS.between(periodo.fechaInicio, periodo.fechaFin);
        if (total <= 0) {
            return 0;
        }
        long elapsed = java.time.temporal.ChronoUnit.DAYS.between(periodo.fechaInicio, LocalDate.now());
        elapsed = Math.max(0, Math.min(elapsed, total));
        return (int) Math.round((elapsed * 100.0) / total);
    }

    private LocalDate construirFechaEstimada(PeriodoEntity periodo, Integer semana, Short diaPreferido) {
        if (periodo == null || periodo.fechaInicio == null || semana == null) {
            return null;
        }
        LocalDate inicioSemana = periodo.fechaInicio.plusDays((long) (semana - 1) * 7L);
        if (diaPreferido == null) {
            return inicioSemana;
        }
        return inicioSemana.plusDays(diaPreferido - 1L);
    }

    private LocalDate alinearFecha(LocalDate from, Short diaSemana) {
        DayOfWeek target = DayOfWeek.of(diaSemana);
        int diff = target.getValue() - from.getDayOfWeek().getValue();
        if (diff < 0) {
            diff += 7;
        }
        return from.plusDays(diff);
    }

    private String evaluationKey(Long usuarioPeriodoCursoId, String codigo) {
        return usuarioPeriodoCursoId + "::" + codigo;
    }

    private Optional<SilaboEntity> resolveSilabo(UsuarioPeriodoCursoEntity upc) {
        if (upc.silaboId != null) {
            return Optional.ofNullable(silaboRepository.findById(upc.silaboId));
        }
        return silaboRepository.buscarVigentePorCursoId(upc.cursoId);
    }

    private MiCalendarSyncAccount toCalendarSyncAccount(String provider, CalendarSyncAccountEntity entity) {
        if (entity == null) {
            return new MiCalendarSyncAccount(
                    provider,
                    false,
                    null,
                    null,
                    "bidirectional",
                    "pending",
                    null
            );
        }

        boolean connected = !"revoked".equalsIgnoreCase(entity.estado)
                && entity.accessTokenEncrypted != null
                && !entity.accessTokenEncrypted.isBlank();

        return new MiCalendarSyncAccount(
                provider,
                connected,
                entity.email,
                entity.calendarId,
                entity.syncDirection,
                entity.estado,
                entity.lastSyncAt
        );
    }

    private MiTarea toMiTarea(UsuarioTareaEntity tarea, RecordatorioEventoEntity reminder, ContextoActual contexto) {
        UsuarioPeriodoCursoEntity upc = tarea.usuarioPeriodoCursoId == null ? null : contexto.upcById().get(tarea.usuarioPeriodoCursoId);
        CursoEntity curso = upc == null ? null : contexto.cursoById().get(upc.cursoId);

        return new MiTarea(
                tarea.id,
                tarea.usuarioPeriodoId,
                tarea.usuarioPeriodoCursoId,
                upc == null ? null : upc.cursoId,
                curso == null ? null : curso.codigo,
                curso == null ? null : curso.nombre,
                tarea.titulo,
                tarea.descripcion,
                tarea.tipo,
                tarea.prioridad,
                tarea.estado,
                tarea.fechaVencimiento,
                reminder == null ? null : reminder.fechaEnvio,
                reminder == null ? null : reminder.canal,
                tarea.completedAt,
                tarea.createdAt,
                tarea.updatedAt
        );
    }

    private MiRecordatorio toMiRecordatorio(RecordatorioEventoEntity reminder, UsuarioTareaEntity tarea, ContextoActual contexto) {
        if (tarea == null) {
            return null;
        }
        UsuarioPeriodoCursoEntity upc = tarea.usuarioPeriodoCursoId == null ? null : contexto.upcById().get(tarea.usuarioPeriodoCursoId);
        CursoEntity curso = upc == null ? null : contexto.cursoById().get(upc.cursoId);

        return new MiRecordatorio(
                reminder.id,
                tarea.id,
                tarea.usuarioPeriodoCursoId,
                upc == null ? null : upc.cursoId,
                curso == null ? null : curso.codigo,
                curso == null ? null : curso.nombre,
                tarea.titulo,
                tarea.descripcion,
                reminder.fechaEnvio,
                reminder.canal,
                reminder.estado,
                "tarea"
        );
    }

    private record ContextoActual(
            UsuarioEntity usuario,
            UsuarioPeriodoEntity usuarioPeriodo,
            CampusEntity campus,
            PeriodoEntity periodo,
            List<UsuarioPeriodoCursoEntity> upcs,
            Map<Long, UsuarioPeriodoCursoEntity> upcById,
            Map<Long, CursoEntity> cursoById,
            Map<Long, List<UsuarioPeriodoCursoHorarioEntity>> horariosByUpc,
            Map<String, UsuarioPeriodoEvaluacionEntity> evaluacionesByKey
    ) {
    }
}
