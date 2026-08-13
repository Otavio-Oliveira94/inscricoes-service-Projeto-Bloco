package com.eventosexpress.inscricoesservice.service;

import com.eventosexpress.inscricoesservice.client.EventoClient;
import com.eventosexpress.inscricoesservice.client.dto.EventoClientResponseDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.EventoNaoEncontradoException;
import com.eventosexpress.inscricoesservice.exception.EventoServiceIndisponivelException;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.mapper.InscricaoMapper;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import com.eventosexpress.inscricoesservice.repository.InscricaoRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InscricaoServiceTest {
    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private InscricaoMapper inscricaoMapper;

    @Mock
    private EventoClient eventoClient;

    @InjectMocks
    private InscricaoService inscricaoService;

    private InscricaoRequestDTO requestDTO;
    private Inscricao inscricao;
    private Inscricao inscricaoSalva;
    private InscricaoResponseDTO responseDTO;
    private EventoClientResponseDTO eventoValido;

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

        eventoValido =
                new EventoClientResponseDTO();

        eventoValido.setId(1L);
        eventoValido.setTitulo("Evento Teste");
    }

    @Test
    @DisplayName("Deve criar inscrição quando o evento existir")
    void deveCriarInscricaoQuandoEventoExistir() {
        when(eventoClient.buscarPorId(1L))
                .thenReturn(eventoValido);

        when(inscricaoMapper.paraEntidade(requestDTO))
                .thenReturn(inscricao);

        when(inscricaoRepository.save(inscricao))
                .thenReturn(inscricaoSalva);

        when(inscricaoMapper.paraResponseDTO(inscricaoSalva))
                .thenReturn(responseDTO);

        InscricaoResponseDTO resultado = inscricaoService.criar(requestDTO);

        assertAll(
                () -> assertEquals(
                        10L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        1L,
                        resultado.getEventoId()
                ),
                () -> assertEquals(
                        "Otavio Oliveira",
                        resultado.getNomeParticipante()
                ),
                () -> assertNotNull(
                        inscricao.getDataInscricao()
                )
        );

        verify(eventoClient).buscarPorId(1L);
        verify(inscricaoRepository).save(inscricao);
    }

    @Test
    @DisplayName("Não deve salvar inscrição quando o evento não existir")
    void naoDeveSalvarQuandoEventoNaoExistir() {
        InscricaoRequestDTO dtoInvalido =
                new InscricaoRequestDTO(
                        999L,
                        "Participante",
                        "participante@email.com"
                );

        FeignException.NotFound erroNotFound = mock(FeignException.NotFound.class);

        when(eventoClient.buscarPorId(999L))
                .thenThrow(erroNotFound);

        assertThrows(
                EventoNaoEncontradoException.class,
                () -> inscricaoService.criar(
                        dtoInvalido
                )
        );

        verifyNoInteractions(
                inscricaoMapper,
                inscricaoRepository
        );
    }

    @Test
    @DisplayName("Não deve salvar quando o serviço de eventos estiver indisponível")
    void naoDeveSalvarQuandoServicoIndisponivel() {
        FeignException erroFeign = mock(FeignException.class);

        when(eventoClient.buscarPorId(1L))
                .thenThrow(erroFeign);

        assertThrows(
                EventoServiceIndisponivelException.class,
                () -> inscricaoService.criar(
                        requestDTO
                )
        );

        verifyNoInteractions(
                inscricaoMapper,
                inscricaoRepository
        );
    }

    @Test
    @DisplayName("Deve listar inscrições por evento")
    void deveListarInscricoesPorEvento() {
        when(inscricaoRepository.findByEventoId(1L))
                .thenReturn(List.of(inscricaoSalva));

        when(inscricaoMapper.paraResponseDTO(inscricaoSalva))
                .thenReturn(responseDTO);

        List<InscricaoResponseDTO> resultados = inscricaoService.buscarPorEvento(1L);

        assertEquals(1, resultados.size());
        assertEquals(
                10L,
                resultados.getFirst().getId()
        );

        verify(inscricaoRepository).findByEventoId(1L);
    }

    @Test
    @DisplayName("Deve editar uma inscrição")
    void deveEditarInscricao() {
        LocalDateTime dataOriginal =
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        10,
                        30
                );

        Inscricao inscricaoExistente =
                new Inscricao(
                        10L,
                        1L,
                        "Nome Antigo",
                        "antigo@email.com",
                        dataOriginal
                );

        InscricaoRequestDTO dtoEditado =
                new InscricaoRequestDTO(
                        2L,
                        "Nome Atualizado",
                        "atualizado@email.com"
                );

        Inscricao inscricaoAtualizada =
                new Inscricao(
                        10L,
                        2L,
                        "Nome Atualizado",
                        "atualizado@email.com",
                        dataOriginal
                );

        InscricaoResponseDTO respostaAtualizada =
                new InscricaoResponseDTO(
                        10L,
                        2L,
                        "Nome Atualizado",
                        "atualizado@email.com",
                        dataOriginal
                );

        EventoClientResponseDTO segundoEvento = new EventoClientResponseDTO();

        segundoEvento.setId(2L);

        when(inscricaoRepository.findById(10L))
                .thenReturn(Optional.of(inscricaoExistente));

        when(eventoClient.buscarPorId(2L))
                .thenReturn(segundoEvento);

        when(inscricaoRepository.save(inscricaoExistente))
                .thenReturn(inscricaoAtualizada);

        when(inscricaoMapper.paraResponseDTO(inscricaoAtualizada))
                .thenReturn(respostaAtualizada);

        InscricaoResponseDTO resultado =
                inscricaoService.editar(
                        10L,
                        dtoEditado
                );

        assertAll(
                () -> assertEquals(
                        "Nome Atualizado",
                        resultado.getNomeParticipante()
                ),
                () -> assertEquals(
                        2L,
                        resultado.getEventoId()
                ),
                () -> assertEquals(
                        dataOriginal,
                        resultado.getDataInscricao()
                )
        );

        verify(eventoClient).buscarPorId(2L);

        verify(inscricaoMapper).atualizarEntidade(inscricaoExistente, dtoEditado);

        verify(inscricaoRepository).save(inscricaoExistente);
    }

    @Test
    @DisplayName("Deve excluir uma inscrição existente")
    void deveExcluirInscricaoExistente() {
        when(inscricaoRepository.findById(10L))
                .thenReturn(Optional.of(inscricaoSalva));

        inscricaoService.remover(10L);

        verify(inscricaoRepository).delete(inscricaoSalva);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a inscrição não existir")
    void deveLancarExcecaoQuandoInscricaoNaoExistir() {
        when(inscricaoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                InscricaoNaoEncontradaException.class,
                () -> inscricaoService.buscarPorId(
                        999L
                )
        );

        verify(
                inscricaoRepository
        ).findById(999L);

        verifyNoInteractions(
                eventoClient,
                inscricaoMapper
        );
    }
}
