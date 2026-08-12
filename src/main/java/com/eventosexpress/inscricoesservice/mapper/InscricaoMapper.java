package com.eventosexpress.inscricoesservice.mapper;

import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import org.springframework.stereotype.Component;

@Component
public class InscricaoMapper {

    public Inscricao paraEntidade(InscricaoRequestDTO dto) {
        Inscricao inscricao = new Inscricao();

        atualizarEntidade(inscricao, dto);

        return inscricao;
    }

    public void atualizarEntidade(Inscricao inscricao, InscricaoRequestDTO dto) {
        inscricao.setEventoId(dto.getEventoId());
        inscricao.setNomeParticipante(
                dto.getNomeParticipante()
        );
        inscricao.setEmailParticipante(
                dto.getEmailParticipante()
        );
    }

    public InscricaoResponseDTO paraResponseDTO(Inscricao inscricao) {
        return new InscricaoResponseDTO(
                inscricao.getId(),
                inscricao.getEventoId(),
                inscricao.getNomeParticipante(),
                inscricao.getEmailParticipante(),
                inscricao.getDataInscricao()
        );
    }
}
