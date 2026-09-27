package com.eventosexpress.inscricoesservice.service;

import com.eventosexpress.contratos.EventoValidadoParaInscricao;
import com.eventosexpress.inscricoesservice.auditoria.AuditoriaService;
import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.mapper.InscricaoMapper;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import com.eventosexpress.inscricoesservice.repository.InscricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.eventosexpress.contratos.ValidarEventoParaInscricao;
import com.eventosexpress.inscricoesservice.model.StatusInscricao;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class InscricaoServiceTest {
    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private InscricaoMapper inscricaoMapper;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private InscricaoService inscricaoService;

    private InscricaoRequestDTO requestDTO;
    private Inscricao inscricao;
    private Inscricao inscricaoSalva;
    private InscricaoResponseDTO responseDTO;

    @BeforeEach
    void prepararDados() {
        requestDTO = new InscricaoRequestDTO(
                1L,
                "Otavio Oliveira",
                "otavio@email.com"
        );

        inscricao = new Inscricao(
                null,
                1L,
                "Otavio Oliveira",
                "otavio@email.com",
                null
        );

        LocalDateTime dataInscricao =
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        10,
                        30
                );

        inscricaoSalva = new Inscricao(
                10L,
                1L,
                "Otavio Oliveira",
                "otavio@email.com",
                dataInscricao
        );

        responseDTO = new InscricaoResponseDTO(
                10L,
                1L,
                "Otavio Oliveira",
                "otavio@email.com",
                dataInscricao
        );
    }

    @Test
    @DisplayName("Deve criar inscrição pendente e solicitar validação")
    void deveCriarInscricaoPendenteESolicitarValidacao() {
        when(inscricaoMapper.paraEntidade(requestDTO))
                .thenReturn(inscricao);

        when(inscricaoRepository.save(inscricao))
                .thenAnswer(invocation -> {
                    Inscricao entidade = invocation.getArgument(0);
                    entidade.setId(10L);
                    return entidade;
                });

        when(inscricaoMapper.paraResponseDTO(inscricao))
                .thenAnswer(invocation ->
                        new InscricaoMapper().paraResponseDTO(
                                invocation.getArgument(0)
                        )
                );

        InscricaoResponseDTO resultado =
                inscricaoService.criar(requestDTO);

        ArgumentCaptor<ValidarEventoParaInscricao> captor =
                ArgumentCaptor.forClass(
                        ValidarEventoParaInscricao.class
                );

        verify(applicationEventPublisher)
                .publishEvent(captor.capture());

        ValidarEventoParaInscricao mensagem =
                captor.getValue();

        assertAll(
                () -> assertEquals(10L, resultado.getId()),
                () -> assertEquals(
                        StatusInscricao.PENDENTE,
                        resultado.getStatus()
                ),
                () -> assertNotNull(
                        resultado.getSolicitacaoId()
                ),
                () -> assertNotNull(
                        resultado.getDataInscricao()
                ),
                () -> assertNotNull(
                        mensagem.mensagemId()
                ),
                () -> assertEquals(
                        1,
                        mensagem.versaoMensagem()
                ),
                () -> assertEquals(
                        10L,
                        mensagem.inscricaoId()
                ),
                () -> assertEquals(
                        1L,
                        mensagem.eventoId()
                ),
                () -> assertEquals(
                        resultado.getSolicitacaoId(),
                        mensagem.solicitacaoId()
                )
        );

        verify(auditoriaService).registrar(
                "CRIACAO",
                10L,
                null,
                resultado
        );
    }

    @Test
    @DisplayName("Deve rejeitar eventoId ausente, zero ou negativo")
    void deveRejeitarEventoIdInvalido() {
        for (Long eventoId : new Long[]{null, 0L, -1L}) {
            InscricaoRequestDTO dto =
                    new InscricaoRequestDTO(
                            eventoId,
                            "Participante",
                            "participante@email.com"
                    );

            ResponseStatusException erro =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> inscricaoService.criar(dto)
                    );

            assertEquals(
                    400,
                    erro.getStatusCode().value()
            );
        }

        verifyNoInteractions(
                inscricaoMapper,
                inscricaoRepository,
                auditoriaService,
                applicationEventPublisher
        );
    }

    @Test
    @DisplayName("Deve listar inscrições por evento")
    void deveListarInscricoesPorEvento() {
        when(inscricaoRepository.findByEventoId(1L))
                .thenReturn(List.of(inscricaoSalva));

        when(inscricaoMapper.paraResponseDTO(inscricaoSalva))
                .thenReturn(responseDTO);

        List<InscricaoResponseDTO> resultados =
                inscricaoService.buscarPorEvento(1L);

        assertEquals(1, resultados.size());
        assertEquals(
                10L,
                resultados.getFirst().getId()
        );

        verify(inscricaoRepository)
                .findByEventoId(1L);
    }

    @Test
    @DisplayName("Deve solicitar nova validação ao alterar o evento")
    void deveSolicitarNovaValidacaoAoAlterarEvento() {
        LocalDateTime dataInscricao =
                LocalDateTime.of(
                        2026,
                        9,
                        27,
                        19,
                        30
                );

        Inscricao existente = new Inscricao(
                10L,
                1L,
                "Nome Antigo",
                "antigo@email.com",
                dataInscricao
        );

        InscricaoRequestDTO dto =
                new InscricaoRequestDTO(
                        2L,
                        "Novo Nome",
                        "novo@email.com"
                );

        InscricaoMapper mapperReal =
                new InscricaoMapper();

        when(
                inscricaoRepository
                        .buscarPorIdComBloqueio(10L)
        ).thenReturn(Optional.of(existente));

        when(
                inscricaoMapper.paraResponseDTO(
                        any(Inscricao.class)
                )
        ).thenAnswer(invocation -> {
            Inscricao entidade =
                    invocation.getArgument(0);

            return mapperReal.paraResponseDTO(entidade);
        });

        doAnswer(invocation -> {
            mapperReal.atualizarEntidade(existente, dto);
            return null;
        }).when(inscricaoMapper)
                .atualizarEntidade(existente, dto);

        when(inscricaoRepository.save(existente))
                .thenReturn(existente);

        InscricaoResponseDTO resposta =
                inscricaoService.editar(10L, dto);

        assertAll(
                () -> assertEquals(
                        10L,
                        resposta.getId()
                ),
                () -> assertEquals(
                        2L,
                        resposta.getEventoId()
                ),
                () -> assertEquals(
                        "Novo Nome",
                        resposta.getNomeParticipante()
                ),
                () -> assertEquals(
                        "novo@email.com",
                        resposta.getEmailParticipante()
                ),
                () -> assertEquals(
                        dataInscricao,
                        resposta.getDataInscricao()
                ),
                () -> assertEquals(
                        StatusInscricao.PENDENTE,
                        resposta.getStatus()
                ),
                () -> assertNotNull(
                        resposta.getSolicitacaoId()
                ),
                () -> assertNull(
                        resposta.getMotivoRejeicao()
                )
        );

        ArgumentCaptor<ValidarEventoParaInscricao> captor =
                ArgumentCaptor.forClass(
                        ValidarEventoParaInscricao.class
                );

        verify(applicationEventPublisher)
                .publishEvent(captor.capture());

        ValidarEventoParaInscricao mensagem =
                captor.getValue();

        assertAll(
                () -> assertNotNull(
                        mensagem.mensagemId()
                ),
                () -> assertEquals(
                        1,
                        mensagem.versaoMensagem()
                ),
                () -> assertEquals(
                        10L,
                        mensagem.inscricaoId()
                ),
                () -> assertEquals(
                        2L,
                        mensagem.eventoId()
                ),
                () -> assertEquals(
                        resposta.getSolicitacaoId(),
                        mensagem.solicitacaoId()
                )
        );

        verify(inscricaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(inscricaoMapper)
                .atualizarEntidade(existente, dto);

        verify(inscricaoRepository)
                .save(existente);

        verify(auditoriaService).registrar(
                eq("ATUALIZACAO"),
                eq(10L),
                any(InscricaoResponseDTO.class),
                any(InscricaoResponseDTO.class)
        );

    }

    @Test
    @DisplayName("Deve excluir uma inscrição existente")
    void deveExcluirInscricaoExistente() {
        when(
                inscricaoRepository
                        .buscarPorIdComBloqueio(10L)
        ).thenReturn(Optional.of(inscricaoSalva));

        when(
                inscricaoMapper
                        .paraResponseDTO(inscricaoSalva)
        ).thenReturn(responseDTO);

        inscricaoService.remover(10L);

        verify(inscricaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(inscricaoRepository)
                .delete(inscricaoSalva);

        verify(auditoriaService).registrar(
                "EXCLUSAO",
                10L,
                responseDTO,
                null
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando a inscrição não existir")
    void deveLancarExcecaoQuandoInscricaoNaoExistir() {
        when(inscricaoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                InscricaoNaoEncontradaException.class,
                () -> inscricaoService.buscarPorId(999L)
        );

        verify(inscricaoRepository)
                .findById(999L);

        verifyNoInteractions(inscricaoMapper);
    }

    @Test
    @DisplayName("Deve confirmar inscrição quando o evento existir")
    void deveConfirmarInscricaoQuandoEventoExistir() {
        UUID solicitacaoId =
                UUID.randomUUID();

        Inscricao inscricaoPendente =
                new Inscricao(
                        10L,
                        1L,
                        "Otavio Oliveira",
                        "otavio@email.com",
                        LocalDateTime.of(
                                2026,
                                9,
                                27,
                                19,
                                30
                        ),
                        StatusInscricao.PENDENTE,
                        solicitacaoId,
                        null
                );

        EventoValidadoParaInscricao resultado =
                new EventoValidadoParaInscricao(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        solicitacaoId,
                        10L,
                        1L,
                        true,
                        null
                );

        InscricaoMapper mapperReal =
                new InscricaoMapper();

        when(
                inscricaoRepository
                        .buscarPorIdComBloqueio(10L)
        ).thenReturn(
                Optional.of(inscricaoPendente)
        );

        when(
                inscricaoMapper.paraResponseDTO(
                        any(Inscricao.class)
                )
        ).thenAnswer(invocation -> {
            Inscricao entidade =
                    invocation.getArgument(0);

            return mapperReal.paraResponseDTO(entidade);
        });

        when(
                inscricaoRepository.save(
                        inscricaoPendente
                )
        ).thenReturn(inscricaoPendente);

        inscricaoService.aplicarResultadoValidacao(
                resultado
        );

        assertAll(
                () -> assertEquals(
                        StatusInscricao.CONFIRMADA,
                        inscricaoPendente.getStatus()
                ),
                () -> assertNull(
                        inscricaoPendente
                                .getMotivoRejeicao()
                )
        );

        verify(inscricaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(inscricaoRepository)
                .save(inscricaoPendente);

        verify(auditoriaService).registrar(
                eq("ATUALIZACAO"),
                eq(10L),
                any(InscricaoResponseDTO.class),
                any(InscricaoResponseDTO.class)
        );
    }

    @Test
    @DisplayName("Deve rejeitar inscrição quando o evento não existir")
    void deveRejeitarInscricaoQuandoEventoNaoExistir() {
        UUID solicitacaoId =
                UUID.randomUUID();

        Inscricao inscricaoPendente =
                new Inscricao(
                        10L,
                        999999L,
                        "Otavio Oliveira",
                        "otavio@email.com",
                        LocalDateTime.of(
                                2026,
                                9,
                                27,
                                19,
                                30
                        ),
                        StatusInscricao.PENDENTE,
                        solicitacaoId,
                        null
                );

        EventoValidadoParaInscricao resultado =
                new EventoValidadoParaInscricao(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        solicitacaoId,
                        10L,
                        999999L,
                        false,
                        "Evento nao encontrado."
                );

        InscricaoMapper mapperReal =
                new InscricaoMapper();

        when(
                inscricaoRepository
                        .buscarPorIdComBloqueio(10L)
        ).thenReturn(
                Optional.of(inscricaoPendente)
        );

        when(
                inscricaoMapper.paraResponseDTO(
                        any(Inscricao.class)
                )
        ).thenAnswer(invocation -> {
            Inscricao entidade =
                    invocation.getArgument(0);

            return mapperReal.paraResponseDTO(entidade);
        });

        when(
                inscricaoRepository.save(
                        inscricaoPendente
                )
        ).thenReturn(inscricaoPendente);

        inscricaoService.aplicarResultadoValidacao(
                resultado
        );

        assertAll(
                () -> assertEquals(
                        StatusInscricao.REJEITADA,
                        inscricaoPendente.getStatus()
                ),
                () -> assertEquals(
                        "Evento nao encontrado.",
                        inscricaoPendente
                                .getMotivoRejeicao()
                )
        );

        verify(inscricaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(inscricaoRepository)
                .save(inscricaoPendente);

        verify(auditoriaService).registrar(
                eq("ATUALIZACAO"),
                eq(10L),
                any(InscricaoResponseDTO.class),
                any(InscricaoResponseDTO.class)
        );
    }

    @Test
    @DisplayName("Deve ignorar resposta de uma solicitação antiga")
    void deveIgnorarRespostaDeSolicitacaoAntiga() {
        UUID solicitacaoAtual =
                UUID.randomUUID();

        UUID solicitacaoAntiga =
                UUID.randomUUID();

        Inscricao inscricaoPendente =
                new Inscricao(
                        10L,
                        2L,
                        "Otavio Oliveira",
                        "otavio@email.com",
                        LocalDateTime.of(
                                2026,
                                9,
                                27,
                                19,
                                30
                        ),
                        StatusInscricao.PENDENTE,
                        solicitacaoAtual,
                        null
                );

        EventoValidadoParaInscricao respostaAntiga =
                new EventoValidadoParaInscricao(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        solicitacaoAntiga,
                        10L,
                        1L,
                        true,
                        null
                );

        when(
                inscricaoRepository
                        .buscarPorIdComBloqueio(10L)
        ).thenReturn(
                Optional.of(inscricaoPendente)
        );

        inscricaoService.aplicarResultadoValidacao(
                respostaAntiga
        );

        assertAll(
                () -> assertEquals(
                        StatusInscricao.PENDENTE,
                        inscricaoPendente.getStatus()
                ),
                () -> assertEquals(
                        solicitacaoAtual,
                        inscricaoPendente
                                .getSolicitacaoId()
                ),
                () -> assertNull(
                        inscricaoPendente
                                .getMotivoRejeicao()
                )
        );

        verify(inscricaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(inscricaoRepository, never())
                .save(any(Inscricao.class));

        verifyNoInteractions(
                inscricaoMapper,
                auditoriaService
        );
    }
}
