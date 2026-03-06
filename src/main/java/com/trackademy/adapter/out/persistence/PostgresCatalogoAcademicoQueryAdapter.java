package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CampusEntity;
import com.trackademy.adapter.out.persistence.entity.CarreraEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEntity;
import com.trackademy.adapter.out.persistence.entity.PeriodoEventoEntity;
import com.trackademy.adapter.out.persistence.repository.CampusPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.CarreraPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoEventoPanacheRepository;
import com.trackademy.adapter.out.persistence.repository.PeriodoPanacheRepository;
import com.trackademy.application.port.out.CatalogoAcademicoQueryPort;
import com.trackademy.domain.model.catalogo.CampusCatalogo;
import com.trackademy.domain.model.catalogo.CarreraCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoEventoCatalogo;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PostgresCatalogoAcademicoQueryAdapter implements CatalogoAcademicoQueryPort {

    private final CampusPanacheRepository campusRepository;
    private final CarreraPanacheRepository carreraRepository;
    private final PeriodoPanacheRepository periodoRepository;
    private final PeriodoEventoPanacheRepository periodoEventoRepository;

    public PostgresCatalogoAcademicoQueryAdapter(
            CampusPanacheRepository campusRepository,
            CarreraPanacheRepository carreraRepository,
            PeriodoPanacheRepository periodoRepository,
            PeriodoEventoPanacheRepository periodoEventoRepository
    ) {
        this.campusRepository = campusRepository;
        this.carreraRepository = carreraRepository;
        this.periodoRepository = periodoRepository;
        this.periodoEventoRepository = periodoEventoRepository;
    }

    @Override
    public List<CampusCatalogo> listarCampuses(Long universidadId) {
        List<CampusEntity> entities = universidadId == null
                ? campusRepository.listarOrdenados()
                : campusRepository.listarPorUniversidadOrdenados(universidadId);
        return entities.stream().map(this::toCampus).toList();
    }

    @Override
    public List<CarreraCatalogo> listarCarreras(Long universidadId) {
        List<CarreraEntity> entities = universidadId == null
                ? carreraRepository.listarOrdenadas()
                : carreraRepository.listarPorUniversidadOrdenadas(universidadId);
        return entities.stream().map(this::toCarrera).toList();
    }

    @Override
    public List<PeriodoCatalogo> listarPeriodos(Long universidadId) {
        List<PeriodoEntity> entities = universidadId == null
                ? periodoRepository.listarOrdenados()
                : periodoRepository.listarPorUniversidadOrdenados(universidadId);
        return entities.stream().map(this::toPeriodo).toList();
    }

    @Override
    public List<PeriodoEventoCatalogo> listarEventosPeriodo(Long periodoId) {
        return periodoEventoRepository.listarPorPeriodo(periodoId).stream().map(this::toEvento).toList();
    }

    private CampusCatalogo toCampus(CampusEntity x) {
        return new CampusCatalogo(x.id, x.universidadId, x.nombre, x.timezone);
    }

    private CarreraCatalogo toCarrera(CarreraEntity x) {
        return new CarreraCatalogo(x.id, x.universidadId, x.nombre);
    }

    private PeriodoCatalogo toPeriodo(PeriodoEntity x) {
        return new PeriodoCatalogo(x.id, x.universidadId, x.etiqueta, x.fechaInicio, x.fechaFin, x.estado);
    }

    private PeriodoEventoCatalogo toEvento(PeriodoEventoEntity x) {
        return new PeriodoEventoCatalogo(x.id, x.periodoId, x.tipo, x.titulo, x.fechaInicio, x.fechaFin, x.descripcion);
    }
}
