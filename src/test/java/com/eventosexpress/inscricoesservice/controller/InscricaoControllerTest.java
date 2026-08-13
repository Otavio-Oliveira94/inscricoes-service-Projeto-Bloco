package com.eventosexpress.inscricoesservice.controller;


import com.eventosexpress.inscricoesservice.client.EventoClient;
import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.EventoServiceIndisponivelException;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.service.InscricaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InscricaoController.class)
public class InscricaoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InscricaoService inscricaoService;

    @MockitoBean
    private EventoClient eventoClient;

    private InscricaoResponseDTO criarResposta() {
        return new InscricaoResponseDTO(
                10L,
                1L,
                "Otavio Oliveira",
                "otavio@email.com",
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        10,
                        30
                )
        );
    }

    @Test
    @DisplayName("POST deve retornar 201")
    void deveCriarInscricao() throws Exception {
        when(
                inscricaoService.criar(
                        any(InscricaoRequestDTO.class)
                )
        ).thenReturn(criarResposta());

        mockMvc.perform(
                        post("/inscricoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "eventoId": 1,
                                          "nomeParticipante":
                                            "Otavio Oliveira",
                                          "emailParticipante":
                                            "otavio@email.com"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(
                        jsonPath("$.eventoId").value(1)
                )
                .andExpect(
                        jsonPath("$.nomeParticipante")
                                .value("Otavio Oliveira")
                )
                .andExpect(
                        jsonPath("$.emailParticipante")
                                .value("otavio@email.com")
                );
    }

    @Test
    @DisplayName("GET deve listar todas")
    void deveListarTodasAsInscricoes()
            throws Exception {
        when(inscricaoService.buscarTodas())
                .thenReturn(
                        List.of(criarResposta())
                );

        mockMvc.perform(get("/inscricoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(
                        jsonPath("$[0].nomeParticipante")
                                .value("Otavio Oliveira")
                );
    }

    @Test
    @DisplayName("GET deve buscar pelo ID")
    void deveBuscarInscricaoPorId()
            throws Exception {
        when(inscricaoService.buscarPorId(10L))
                .thenReturn(criarResposta());

        mockMvc.perform(get("/inscricoes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET deve listar por evento")
    void deveListarInscricoesPorEvento()
            throws Exception {
        when(
                inscricaoService.buscarPorEvento(1L)
        ).thenReturn(List.of(criarResposta()));

        mockMvc.perform(
                        get("/inscricoes/evento/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[0].eventoId")
                                .value(1)
                );
    }

    @Test
    @DisplayName("PUT deve atualizar inscrição")
    void deveAtualizarInscricao()
            throws Exception {
        InscricaoResponseDTO respostaAtualizada =
                new InscricaoResponseDTO(
                        10L,
                        1L,
                        "Nome Atualizado",
                        "atualizado@email.com",
                        LocalDateTime.of(
                                2026,
                                8,
                                13,
                                10,
                                30
                        )
                );

        when(
                inscricaoService.editar(
                        eq(10L),
                        any(InscricaoRequestDTO.class)
                )
        ).thenReturn(respostaAtualizada);

        mockMvc.perform(
                        put("/inscricoes/10")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "eventoId": 1,
                                          "nomeParticipante":
                                            "Nome Atualizado",
                                          "emailParticipante":
                                            "atualizado@email.com"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nomeParticipante")
                                .value("Nome Atualizado")
                )
                .andExpect(
                        jsonPath("$.emailParticipante")
                                .value(
                                        "atualizado@email.com"
                                )
                );
    }

    @Test
    @DisplayName("DELETE deve retornar 204")
    void deveExcluirInscricao()
            throws Exception {
        mockMvc.perform(
                        delete("/inscricoes/10")
                )
                .andExpect(status().isNoContent());

        verify(inscricaoService).remover(10L);
    }

    @Test
    @DisplayName(
            "GET inexistente deve retornar 404"
    )
    void deveRetornar404ParaInscricaoInexistente()
            throws Exception {
        when(inscricaoService.buscarPorId(999L))
                .thenThrow(
                        new InscricaoNaoEncontradaException(
                                999L
                        )
                );

        mockMvc.perform(get("/inscricoes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
            "POST deve retornar 503 quando eventos estiver indisponível"
    )
    void deveRetornar503QuandoServicoIndisponivel()
            throws Exception {
        when(
                inscricaoService.criar(
                        any(InscricaoRequestDTO.class)
                )
        ).thenThrow(
                new EventoServiceIndisponivelException()
        );

        mockMvc.perform(
                        post("/inscricoes")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "eventoId": 1,
                                          "nomeParticipante":
                                            "Participante",
                                          "emailParticipante":
                                            "participante@email.com"
                                        }
                                        """)
                )
                .andExpect(
                        status().isServiceUnavailable()
                );
    }

}
