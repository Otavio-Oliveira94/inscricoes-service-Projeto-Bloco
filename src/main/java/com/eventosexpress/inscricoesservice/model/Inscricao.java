package com.eventosexpress.inscricoesservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inscricoes")
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "nome_participante", nullable = false)
    private String nomeParticipante;

    @Column(name = "email_participante", nullable = false)
    private String emailParticipante;

    @Column(
            name = "data_inscricao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataInscricao;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "varchar(20) default 'CONFIRMADA'"
    )
    private StatusInscricao status = StatusInscricao.CONFIRMADA;

    private UUID solicitacaoId;

    private String motivoRejeicao;

    public Inscricao(
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
