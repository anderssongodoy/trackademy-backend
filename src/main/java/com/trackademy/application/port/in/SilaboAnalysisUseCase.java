package com.trackademy.application.port.in;

import com.trackademy.domain.model.SilaboAnalysis;

public interface SilaboAnalysisUseCase {

    SilaboAnalysis analizarSilabo(String email, Long usuarioPeriodoCursoId);
}
