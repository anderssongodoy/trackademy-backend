package com.trackademy.application.port.out;

import com.trackademy.domain.model.SilaboAnalysis;
import com.trackademy.domain.model.SilaboParaAnalisis;

import java.util.Optional;

public interface SilaboAnalysisPort {

    Optional<SilaboParaAnalisis> buscarSilaboPorUsuarioPeriodoCursoId(String email, Long usuarioPeriodoCursoId);

    Optional<SilaboAnalysis> buscarAnalisisCacheado(String hashPdf);

    SilaboAnalysis guardarAnalisis(SilaboAnalysis analisis);
}
