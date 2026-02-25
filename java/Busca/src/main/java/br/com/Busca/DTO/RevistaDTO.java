package br.com.Busca.DTO;

import java.util.List;

public record RevistaDTO(
    int numero,
    String data,
    List<MarcaDTO> marca
) {
} 
