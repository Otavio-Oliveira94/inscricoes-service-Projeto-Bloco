package com.eventosexpress.inscricoesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoRequestDTO {
    private Long eventoId;
    private String nomeParticipante;
    private String emailParticipante;
}
