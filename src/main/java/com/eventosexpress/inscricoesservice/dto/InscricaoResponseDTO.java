package com.eventosexpress.inscricoesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoResponseDTO {

    private Long id;
    private Long eventoId;
    private String nomeParticipante;
    private String emailParticipante;
    private LocalDateTime dataInscricao;
}
