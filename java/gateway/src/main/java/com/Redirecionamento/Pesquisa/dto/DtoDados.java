package com.Redirecionamento.Pesquisa.dto;

import java.time.LocalDateTime;

public record DtoDados(
    String url,
    String duracao,
    String cpuUso,
    String cpuDelta,
    String ram,
    String ramDelta,
    LocalDateTime data

) {
} 
