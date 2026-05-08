package com.trackademy.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackademy.adapter.out.persistence.entity.SilaboAnalysisSnapshotEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboAnalysisSnapshotPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.SilaboUnidadPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.out.SilaboAnalysisPort;
import com.trackademy.domain.model.SilaboAnalysis;
import com.trackademy.domain.model.SilaboAnalysisRecurso;
import com.trackademy.domain.model.SilaboParaAnalisis;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgresSilaboAnalysisAdapter implements SilaboAnalysisPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository upcRepository;
    private final CursoPanacheRepository cursoRepository;
    private final SilaboPanacheRepository silaboRepository;
    private final SilaboUnidadPanacheRepository unidadRepository;
    private final SilaboAnalysisSnapshotPanacheRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    public PostgresSilaboAnalysisAdapter(
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository upcRepository,
            CursoPanacheRepository cursoRepository,
            SilaboPanacheRepository silaboRepository,
            SilaboUnidadPanacheRepository unidadRepository,
            SilaboAnalysisSnapshotPanacheRepository snapshotRepository,
            ObjectMapper objectMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.upcRepository = upcRepository;
        this.cursoRepository = cursoRepository;
        this.silaboRepository = silaboRepository;
        this.unidadRepository = unidadRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<SilaboParaAnalisis> buscarSilaboPorUsuarioPeriodoCursoId(String email, Long usuarioPeriodoCursoId) {
        var usuario = usuarioRepository.buscarPorEmail(email).orElse(null);
        if (usuario == null) return Optional.empty();

        var periodo = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuario.id).orElse(null);
        if (periodo == null) return Optional.empty();

        var upc = upcRepository.findByIdOptional(usuarioPeriodoCursoId).orElse(null);
        if (upc == null || !upc.usuarioPeriodoId.equals(periodo.id)) return Optional.empty();

        var silabo = silaboRepository.buscarVigentePorCursoId(upc.cursoId).orElse(null);
        if (silabo == null || silabo.hashPdf == null) return Optional.empty();

        var curso = cursoRepository.findByIdOptional(upc.cursoId).orElse(null);
        if (curso == null) return Optional.empty();

        var unidades = unidadRepository.listarPorSilabo(silabo.id).stream()
                .map(u -> {
                    var sb = new StringBuilder("Unidad ").append(u.nro);
                    if (u.titulo != null) sb.append(": ").append(u.titulo);
                    if (u.semanaInicio != null && u.semanaFin != null)
                        sb.append(" (semanas ").append(u.semanaInicio).append("-").append(u.semanaFin).append(")");
                    if (u.logroEspecifico != null) sb.append(" — ").append(u.logroEspecifico);
                    return sb.toString();
                })
                .toList();

        return Optional.of(new SilaboParaAnalisis(
                silabo.id,
                silabo.hashPdf,
                curso.nombre,
                silabo.sumilla,
                silabo.fundamentacion,
                silabo.metodologia,
                silabo.logroGeneral,
                unidades
        ));
    }

    @Override
    public Optional<SilaboAnalysis> buscarAnalisisCacheado(String hashPdf) {
        return snapshotRepository.buscarPorHashPdf(hashPdf).map(entity -> {
            try {
                List<String> temas = objectMapper.readValue(entity.temasJson, new TypeReference<>() {});
                List<SilaboAnalysisRecurso> recursos = objectMapper.readValue(entity.recursosJson, new TypeReference<>() {});
                List<String> paraIrMasAlla = entity.proximosPasosJson != null
                        ? objectMapper.readValue(entity.proximosPasosJson, new TypeReference<>() {})
                        : new ArrayList<>();
                return new SilaboAnalysis(entity.silaboId, entity.hashPdf, entity.resumen, temas, recursos, paraIrMasAlla, entity.promptTokens, entity.completionTokens, entity.generatedAt);
            } catch (JsonProcessingException e) {
                return null;
            }
        });
    }

    @Override
    @Transactional
    public SilaboAnalysis guardarAnalisis(SilaboAnalysis analisis) {
        try {
            snapshotRepository.delete("hashPdf = ?1", analisis.hashPdf());
            snapshotRepository.flush();

            var entity = new SilaboAnalysisSnapshotEntity();
            entity.silaboId = analisis.silaboId();
            entity.hashPdf = analisis.hashPdf();
            entity.resumen = analisis.resumen();
            entity.temasJson = objectMapper.writeValueAsString(analisis.temas());
            entity.recursosJson = objectMapper.writeValueAsString(analisis.recursos());
            entity.proximosPasosJson = objectMapper.writeValueAsString(analisis.paraIrMasAlla());
            entity.model = "claude-haiku-4-5-20251001";
            entity.promptTokens = analisis.promptTokens();
            entity.completionTokens = analisis.completionTokens();
            entity.generatedAt = analisis.generatedAt();
            entity.createdAt = OffsetDateTime.now();
            snapshotRepository.persist(entity);

            return analisis;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando el analisis", e);
        }
    }
}
