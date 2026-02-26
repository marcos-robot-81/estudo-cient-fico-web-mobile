package dto;

import java.time.LocalDateTime;

public record DtoDados(
    String url,
    String duracao,
    String cpuUso,
    String cpuDelta,
    String temperatura,
    String ram,
    String ramDelta,
    String data

) {
} 
