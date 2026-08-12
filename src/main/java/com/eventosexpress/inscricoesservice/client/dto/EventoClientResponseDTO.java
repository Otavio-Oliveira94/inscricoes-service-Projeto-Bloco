package com.eventosexpress.inscricoesservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoClientResponseDTO {
    private Long id;
    private String titulo;
    private String subTitulo;
    private String tipoEvento;
    private EnderecoClientResponseDTO endereco;
    private LocalDateTime inicioEvento;
    private LocalDateTime terminoEvento;
}
