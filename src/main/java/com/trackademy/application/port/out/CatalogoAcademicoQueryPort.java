package com.trackademy.application.port.out;

import com.trackademy.domain.model.catalogo.CampusCatalogo;
import com.trackademy.domain.model.catalogo.CarreraCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoCatalogo;
import com.trackademy.domain.model.catalogo.PeriodoEventoCatalogo;

import java.util.List;

public interface CatalogoAcademicoQueryPort {
    List<CampusCatalogo> listarCampuses(Long universidadId);

    List<CarreraCatalogo> listarCarreras(Long universidadId);

    List<PeriodoCatalogo> listarPeriodos(Long universidadId);

    List<PeriodoEventoCatalogo> listarEventosPeriodo(Long periodoId);
}
