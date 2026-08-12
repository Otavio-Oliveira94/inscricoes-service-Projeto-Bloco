package com.eventosexpress.inscricoesservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException(Long EventoId) {

        super("Não foi possível realizar a inscrição. " + "Evento não encontrado para o ID: " + EventoId);
    }
}
