package com.eventosexpress.inscricoesservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InscricaoNaoEncontradaException extends RuntimeException {

    public InscricaoNaoEncontradaException(Long id) {
        super("Inscrição não encontrada para o ID: " + id);
    }

}
