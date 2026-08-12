package com.eventosexpress.inscricoesservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
