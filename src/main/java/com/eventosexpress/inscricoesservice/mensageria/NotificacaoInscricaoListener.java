package com.eventosexpress.inscricoesservice.mensageria;

import com.eventosexpress.contratos.InscricaoConfirmada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.eventosexpress.contratos.MensageriaConstants.FILA_NOTIFICACAO_INSCRICAO_CONFIRMADA;

@Component
public class NotificacaoInscricaoListener {
    private static final Logger log = LoggerFactory.getLogger(NotificacaoInscricaoListener.class);

    @RabbitListener(
            queues = FILA_NOTIFICACAO_INSCRICAO_CONFIRMADA,
            concurrency = "2",
            ackMode = "AUTO"
    )
    public void consumir(
            InscricaoConfirmada evento
    ) {
        String worker =
                Thread.currentThread().getName();

        log.info(
                "[NOTIFICACAO SIMULADA] Worker: {} | "
                        + "Inscricao: {} | Participante: {} | Email: {}",
                worker,
                evento.inscricaoId(),
                evento.nomeParticipante(),
                evento.emailParticipante()
        );
    }
}
