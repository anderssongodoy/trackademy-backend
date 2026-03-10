package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboTemaEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboUnidadEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboTemaPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboUnidadPanacheRepository;
import com.trackademy.application.port.out.CursoQueryPort;
import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;
import com.trackademy.domain.model.CursoEvaluacionDetalle;
import com.trackademy.domain.model.CursoUnidadDetalle;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PostgresCursoQueryAdapter implements CursoQueryPort {

    private final CursoPanacheRepository cursoRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboUnidadPanacheRepository unidadRepository;
    private final SilaboTemaPanacheRepository temaRepository;
    private final SilaboEvaluacionPanacheRepository evaluacionRepository;

    public PostgresCursoQueryAdapter(
            CursoPanacheRepository cursoRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboUnidadPanacheRepository unidadRepository,
            SilaboTemaPanacheRepository temaRepository,
            SilaboEvaluacionPanacheRepository evaluacionRepository
    ) {
        this.cursoRepository = cursoRepository;
        this.silaboRepository = silaboRepository;
        this.unidadRepository = unidadRepository;
        this.temaRepository = temaRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Override
    public List<Curso> listarCursos() {
        return cursoRepository.listarOrdenadosPorCodigo().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Curso> listarCursosPorCarrera(Long carreraId) {
        List<Long> ids = cursoRepository.listarIdsPorCarrera(carreraId);
        if (ids.isEmpty()) {
            return List.of();
        }
        return cursoRepository.listarPorIds(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Curso> buscarCursos(Long carreraId, String query, Integer limit, Integer offset) {
        return cursoRepository.buscar(carreraId, query, limit, offset).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoRepository.buscarPorCodigo(codigo).map(this::toDomain);
    }


    @Override
    public Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo) {
        Optional<CursoEntity> cursoOpt = cursoRepository.buscarPorCodigo(codigo);
        if (cursoOpt.isEmpty()) {
            return Optional.empty();
        }

        CursoEntity cursoEntity = cursoOpt.get();
        Curso curso = toDomain(cursoEntity);

        Optional<SilaboEntity> silaboOpt = silaboRepository.buscarVigentePorCursoId(cursoEntity.id);
        if (silaboOpt.isEmpty()) {
            return Optional.of(new CursoDetalle(
                    curso,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of()
            ));
        }

        SilaboEntity silabo = silaboOpt.get();

        List<SilaboUnidadEntity> unidadesEntity = unidadRepository.listarPorSilabo(silabo.id);
        List<Long> unidadIds = unidadesEntity.stream().map(u -> u.id).toList();
        List<SilaboTemaEntity> temasEntity = temaRepository.listarPorUnidades(unidadIds);

        Map<Long, List<String>> temasPorUnidad = new HashMap<>();
        for (SilaboTemaEntity tema : temasEntity) {
            temasPorUnidad.computeIfAbsent(tema.silaboUnidadId, k -> new ArrayList<>()).add(tema.titulo);
        }

        List<CursoUnidadDetalle> unidades = unidadesEntity.stream()
                .map(u -> new CursoUnidadDetalle(
                        u.nro,
                        u.titulo,
                        u.semanaInicio,
                        u.semanaFin,
                        u.logroEspecifico,
                        temasPorUnidad.getOrDefault(u.id, List.of())
                ))
                .toList();

        List<CursoEvaluacionDetalle> evaluaciones = evaluacionRepository.listarPorSilabo(silabo.id).stream()
                .map(this::toEvaluacionDetalle)
                .toList();

        return Optional.of(new CursoDetalle(
                curso,
                silabo.version,
                silabo.anio,
                silabo.periodoTexto,
                silabo.sumilla,
                silabo.fundamentacion,
                silabo.metodologia,
                silabo.logroGeneral,
                unidades,
                evaluaciones
        ));
    }

    private Curso toDomain(CursoEntity entity) {
        return new Curso(
                entity.id,
                entity.codigo,
                entity.nombre,
                entity.creditos,
                entity.horasSemanales,
                entity.modalidad
        );
    }

    private CursoEvaluacionDetalle toEvaluacionDetalle(SilaboEvaluacionEntity e) {
        return new CursoEvaluacionDetalle(
                e.codigo,
                e.tipo,
                e.descripcion,
                e.porcentaje,
                e.semana,
                e.observacion
        );
    }
}
