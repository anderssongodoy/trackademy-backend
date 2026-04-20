package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboEvaluacionEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboPdfAssetEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboTemaEntity;
import com.trackademy.adapter.out.persistence.entity.SilaboUnidadEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboEvaluacionPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPdfAssetPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboTemaPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboUnidadPanacheRepository;
import com.trackademy.application.port.out.CursoQueryPort;
import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;
import com.trackademy.domain.model.CursoEvaluacionDetalle;
import com.trackademy.domain.model.CursoSilaboDownload;
import com.trackademy.domain.model.CursoSilaboPdf;
import com.trackademy.domain.model.CursoSilaboVersion;
import com.trackademy.domain.model.CursoUnidadDetalle;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PostgresCursoQueryAdapter implements CursoQueryPort {

    private final CursoPanacheRepository cursoRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboPdfAssetPanacheRepository silaboPdfAssetRepository;
    private final SilaboUnidadPanacheRepository unidadRepository;
    private final SilaboTemaPanacheRepository temaRepository;
    private final SilaboEvaluacionPanacheRepository evaluacionRepository;

    public PostgresCursoQueryAdapter(
            CursoPanacheRepository cursoRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboPdfAssetPanacheRepository silaboPdfAssetRepository,
            SilaboUnidadPanacheRepository unidadRepository,
            SilaboTemaPanacheRepository temaRepository,
            SilaboEvaluacionPanacheRepository evaluacionRepository
    ) {
        this.cursoRepository = cursoRepository;
        this.silaboRepository = silaboRepository;
        this.silaboPdfAssetRepository = silaboPdfAssetRepository;
        this.unidadRepository = unidadRepository;
        this.temaRepository = temaRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Override
    public List<Curso> listarCursos() {
        return cursoRepository.listarOrdenadosPorCodigo().stream()
                .map(entity -> toDomain(entity, null))
                .toList();
    }

    @Override
    public List<Curso> listarCursosPorCarrera(Long carreraId) {
        return cursoRepository.listarPorCarreraConCiclo(carreraId).stream()
                .map(item -> toDomain(item.curso(), item.cicloReferencial()))
                .toList();
    }

    @Override
    public List<Curso> buscarCursos(Long carreraId, String query, Integer limit, Integer offset) {
        if (carreraId != null) {
            return cursoRepository.buscarConCiclo(carreraId, query, limit, offset).stream()
                    .map(item -> toDomain(item.curso(), item.cicloReferencial()))
                    .toList();
        }
        return cursoRepository.buscar(carreraId, query, limit, offset).stream()
                .map(entity -> toDomain(entity, null))
                .toList();
    }

    @Override
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoRepository.buscarPorCodigo(codigo).map(entity -> toDomain(entity, null));
    }

    @Override
    public Optional<Curso> obtenerPorPublicId(UUID publicId) {
        return cursoRepository.buscarPorPublicId(publicId).map(entity -> toDomain(entity, null));
    }


    @Override
    public Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo) {
        Optional<CursoEntity> cursoOpt = cursoRepository.buscarPorCodigo(codigo);
        return obtenerDetalle(cursoOpt);
    }

    @Override
    public Optional<CursoDetalle> obtenerDetallePorPublicId(UUID publicId) {
        Optional<CursoEntity> cursoOpt = cursoRepository.buscarPorPublicId(publicId);
        return obtenerDetalle(cursoOpt);
    }

    private Optional<CursoDetalle> obtenerDetalle(Optional<CursoEntity> cursoOpt) {
        if (cursoOpt.isEmpty()) {
            return Optional.empty();
        }

        CursoEntity cursoEntity = cursoOpt.get();
        Curso curso = toDomain(cursoEntity, null);

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
                    null,
                    null,
                    null,
                    List.of(),
                    List.of()
            ));
        }

        SilaboEntity silabo = silaboOpt.get();
        CursoSilaboPdf pdf = toCursoSilaboPdf(silabo);

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
                silabo.id,
                silabo.version,
                pdf,
                pdf != null && pdf.disponibleDescarga() ? "/api/v1/catalog/cursos/silabos/" + silabo.id + "/pdf" : null,
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

    @Override
    public List<CursoSilaboVersion> listarSilabosPorCodigo(String codigo) {
        Optional<CursoEntity> cursoOpt = cursoRepository.buscarPorCodigo(codigo);
        return listarSilabos(cursoOpt);
    }

    @Override
    public List<CursoSilaboVersion> listarSilabosPorPublicId(UUID publicId) {
        Optional<CursoEntity> cursoOpt = cursoRepository.buscarPorPublicId(publicId);
        return listarSilabos(cursoOpt);
    }

    @Override
    public Optional<CursoSilaboVersion> obtenerSilaboVigentePorCodigo(String codigo) {
        return listarSilabosPorCodigo(codigo).stream().filter(CursoSilaboVersion::vigente).findFirst();
    }

    @Override
    public Optional<CursoSilaboVersion> obtenerSilaboVigentePorPublicId(UUID publicId) {
        return listarSilabosPorPublicId(publicId).stream().filter(CursoSilaboVersion::vigente).findFirst();
    }

    private List<CursoSilaboVersion> listarSilabos(Optional<CursoEntity> cursoOpt) {
        if (cursoOpt.isEmpty()) {
            return List.of();
        }

        List<SilaboEntity> silabos = silaboRepository.find("cursoId = ?1 order by vigente desc, id desc", cursoOpt.get().id).list();
        return silabos.stream()
                .map(silabo -> new CursoSilaboVersion(
                        silabo.id,
                        silabo.version,
                        Boolean.TRUE.equals(silabo.vigente),
                        silabo.anio,
                        silabo.periodoTexto,
                        silabo.extraidoEn,
                        toCursoSilaboPdf(silabo)
                ))
                .toList();
    }

    @Override
    public Optional<CursoSilaboDownload> obtenerSilaboDescarga(Long silaboId) {
        SilaboEntity silabo = silaboRepository.findById(silaboId);
        if (silabo == null || silabo.pdfAssetId == null) {
            return Optional.empty();
        }

        SilaboPdfAssetEntity asset = silaboPdfAssetRepository.findById(silabo.pdfAssetId);
        if (asset == null) {
            return Optional.empty();
        }

        return Optional.of(new CursoSilaboDownload(
                silaboId,
                asset.storageProvider,
                asset.storageKey,
                asset.originalFilename,
                asset.mimeType
        ));
    }

    private Curso toDomain(CursoEntity entity, Integer cicloReferencial) {
        return new Curso(
                entity.id,
                entity.publicId,
                entity.codigo,
                entity.nombre,
                entity.creditos,
                entity.horasSemanales,
                entity.modalidad,
                cicloReferencial
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

    private CursoSilaboPdf toCursoSilaboPdf(SilaboEntity silabo) {
        if (silabo.pdfAssetId != null) {
            SilaboPdfAssetEntity asset = silaboPdfAssetRepository.findById(silabo.pdfAssetId);
            if (asset != null) {
                return new CursoSilaboPdf(
                        asset.id,
                        asset.originalFilename,
                        silabo.fuentePdf,
                        asset.mimeType,
                        asset.sizeBytes,
                        asset.sha256,
                        asset.storageProvider,
                        true
                );
            }
        }

        if (silabo.fuentePdf == null && silabo.hashPdf == null) {
            return null;
        }

        return new CursoSilaboPdf(
                null,
                null,
                silabo.fuentePdf,
                "application/pdf",
                null,
                silabo.hashPdf,
                null,
                false
        );
    }
}
