package com.eventosexpress.inscricoesservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EventoServiceIndisponivelException extends RuntimeException {
    public EventoServiceIndisponivelException() {

      super("O serviço de eventos está indisponível. Tente novamente mais tarde.");
    }
}
