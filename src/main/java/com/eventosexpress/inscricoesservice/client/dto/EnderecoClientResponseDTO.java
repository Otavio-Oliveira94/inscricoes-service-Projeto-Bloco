package com.eventosexpress.inscricoesservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoClientResponseDTO {
    private String rua;
    private String numero;
    private String complemento;
    private String cep;
    private String cidade;
    private String estado;
}
