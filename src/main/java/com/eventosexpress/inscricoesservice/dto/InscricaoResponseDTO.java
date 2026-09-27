package com.eventosexpress.inscricoesservice.dto;

import com.eventosexpress.inscricoesservice.model.StatusInscricao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscricaoResponseDTO {

    private Long id;
    private Long eventoId;
    private String nomeParticipante;
    private String emailParticipante;
    private LocalDateTime dataInscricao;

    private StatusInscricao status;
    private UUID solicitacaoId;
    private String motivoRejeicao;

    public InscricaoResponseDTO(
            Long id,
            Long eventoId,
            String nomeParticipante,
            String emailParticipante,
            LocalDateTime dataInscricao
    ) {
        this(
                id,
                eventoId,
                nomeParticipante,
                emailParticipante,
                dataInscricao,
                StatusInscricao.CONFIRMADA,
                null,
                null
        );
    }
}
