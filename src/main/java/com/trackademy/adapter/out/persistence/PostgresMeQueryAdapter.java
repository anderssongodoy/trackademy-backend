package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoCursoEntity;
import com.trackademy.adapter.out.persistence.entity.UsuarioPeriodoEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoCursoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.UsuarioPeriodoPanacheRepository;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PostgresMeQueryAdapter implements MeQueryPort {

    private final UsuarioPanacheRepository usuarioRepository;
    private final UsuarioPeriodoPanacheRepository usuarioPeriodoRepository;
    private final UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository;
    private final CursoPanacheRepository cursoRepository;

    public PostgresMeQueryAdapter(
            UsuarioPanacheRepository usuarioRepository,
            UsuarioPeriodoPanacheRepository usuarioPeriodoRepository,
            UsuarioPeriodoCursoPanacheRepository usuarioPeriodoCursoRepository,
            CursoPanacheRepository cursoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPeriodoRepository = usuarioPeriodoRepository;
        this.usuarioPeriodoCursoRepository = usuarioPeriodoCursoRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    public Optional<MiPeriodoActual> obtenerPeriodoActual(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<UsuarioPeriodoEntity> usuarioPeriodoOpt = usuarioPeriodoRepository.buscarUltimoPorUsuario(usuarioOpt.get().id);
        return usuarioPeriodoOpt.map(up -> new MiPeriodoActual(
                up.usuarioId,
                up.id,
                up.periodoId,
                up.campusId,
                up.carreraId,
                up.cicloActual,
                up.onboardingEstado,
                up.onboardingCompletadoAt,
                up.metaPromedioCiclo,
                up.horasEstudioSemanaObjetivo
        ));
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
}
