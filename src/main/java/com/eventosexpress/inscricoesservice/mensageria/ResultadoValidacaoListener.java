package com.eventosexpress.inscricoesservice.mensageria;

import com.eventosexpress.contratos.EventoValidadoParaInscricao;
import com.eventosexpress.inscricoesservice.service.InscricaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.eventosexpress.contratos.MensageriaConstants.FILA_RESULTADO_VALIDACAO;

@Component
public class ResultadoValidacaoListener {

    private final InscricaoService inscricaoService;

    public ResultadoValidacaoListener(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    @RabbitListener(queues = FILA_RESULTADO_VALIDACAO, ackMode = "AUTO")
    public void consumir(
            EventoValidadoParaInscricao resultado
    ) {
        inscricaoService.aplicarResultadoValidacao(resultado);
    }
}