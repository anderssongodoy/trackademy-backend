package com.trackademy.domain.model;

import java.util.List;

public record SilaboParaAnalisis(
        Long silaboId,
        String hashPdf,
        String nombreCurso,
        String sumilla,
        String fundamentacion,
        String metodologia,
        String logroGeneral,
        List<String> unidades
) {
}
