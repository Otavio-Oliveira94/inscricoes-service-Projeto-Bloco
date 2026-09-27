package com.eventosexpress.inscricoesservice.mensageria;

import com.eventosexpress.contratos.ValidarEventoParaInscricao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.eventosexpress.contratos.MensageriaConstants.ROUTING_VALIDACAO_SOLICITADA;

@Component
public class ValidacaoInscricaoPublisher {

    private static final Logger log = LoggerFactory.getLogger(ValidacaoInscricaoPublisher.class);

    private final PublicadorRabbitConfiavel publicadorRabbit;

    public ValidacaoInscricaoPublisher(
            PublicadorRabbitConfiavel publicadorRabbit
    ) {
        this.publicadorRabbit = publicadorRabbit;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void publicar(
            ValidarEventoParaInscricao solicitacao
    ) {
        try {
            publicadorRabbit.publicar(
                    ROUTING_VALIDACAO_SOLICITADA,
                    solicitacao,
                    solicitacao.mensagemId(),
                    solicitacao.solicitacaoId()
            );

            log.info(
                    "Solicitacao de validacao publicada. "
                            + "Inscricao: {}, solicitacao: {}",
                    solicitacao.inscricaoId(),
                    solicitacao.solicitacaoId()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao publicar a validacao da inscricao {}. "
                            + "A inscricao permanece pendente.",
                    solicitacao.inscricaoId(),
                    exception
            );
        }
    }
}