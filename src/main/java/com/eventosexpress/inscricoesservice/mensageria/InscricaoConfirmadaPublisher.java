package com.eventosexpress.inscricoesservice.mensageria;

import com.eventosexpress.contratos.InscricaoConfirmada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.eventosexpress.contratos.MensageriaConstants.ROUTING_INSCRICAO_CONFIRMADA;

@Component
public class InscricaoConfirmadaPublisher {

    private static final Logger log = LoggerFactory.getLogger(InscricaoConfirmadaPublisher.class);

    private final PublicadorRabbitConfiavel publicadorRabbit;

    public InscricaoConfirmadaPublisher(
            PublicadorRabbitConfiavel publicadorRabbit
    ) {
        this.publicadorRabbit = publicadorRabbit;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void publicar(
            InscricaoConfirmada evento
    ) {
        try {
            publicadorRabbit.publicar(
                    ROUTING_INSCRICAO_CONFIRMADA,
                    evento,
                    evento.mensagemId(),
                    evento.solicitacaoId()
            );

            log.info(
                    "Evento InscricaoConfirmada publicado. "
                            + "Inscricao: {}, evento: {}, solicitacao: {}",
                    evento.inscricaoId(),
                    evento.eventoId(),
                    evento.solicitacaoId()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao publicar InscricaoConfirmada "
                            + "para a inscricao {}.",
                    evento.inscricaoId(),
                    exception
            );
        }
    }
}