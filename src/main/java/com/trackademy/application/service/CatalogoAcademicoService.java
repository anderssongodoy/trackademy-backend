package com.trackademy.application.service;

import com.trackademy.application.port.in.CatalogoAcademicoUseCase;
import com.trackademy.application.port.out.CatalogoAcademicoQueryPort;
import com.trackademy.domain.model.catalogo.CampusCatalogo;
import com.trackademy.domain.model.catalogo.CarreraCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoEventoCatalogo;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CatalogoAcademicoService implements CatalogoAcademicoUseCase {

    private final CatalogoAcademicoQueryPort queryPort;

    public CatalogoAcademicoService(CatalogoAcademicoQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public List<CampusCatalogo> listarCampuses(Long universidadId) {
        return queryPort.listarCampuses(universidadId);
    }

    @Override
    public List<CarreraCatalogo> listarCarreras(Long universidadId) {
        return queryPort.listarCarreras(universidadId);
    }

    @Override
    public List<PeriodoCatalogo> listarPeriodos(Long universidadId) {
        return queryPort.listarPeriodos(universidadId);
    }

    @Override
    public List<PeriodoEventoCatalogo> listarEventosPeriodo(Long periodoId) {
        return queryPort.listarEventosPeriodo(periodoId);
    }
}
