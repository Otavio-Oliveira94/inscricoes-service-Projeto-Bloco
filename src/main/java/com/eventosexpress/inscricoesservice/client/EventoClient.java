package com.eventosexpress.inscricoesservice.client;

import com.eventosexpress.inscricoesservice.client.dto.EventoClientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "eventos-service", url = "${servicos.eventos.url}")
public interface EventoClient {
    @GetMapping("/eventos/{id}")
    EventoClientResponseDTO buscarPorId(@PathVariable("id") Long id);
}
