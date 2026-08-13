package com.eventosexpress.inscricoesservice.repository;

import com.eventosexpress.inscricoesservice.client.EventoClient;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;


@DataJpaTest
public class InscricaoRepositoryTest {
    @Autowired
    private InscricaoRepository inscricaoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private EventoClient eventoClient;

    private Inscricao criarInscricao(
            Long eventoId,
            String nome,
            String email
    ) {
        return new Inscricao(
                null,
                eventoId,
                nome,
                email,
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        10,
                        0
                )
        );
    }

    @Test
    @DisplayName("Deve salvar uma inscirção")
    void deveSalvarInscricao() {
        Inscricao inscricao = criarInscricao(
                1L,
                "Otavio Oliveira",
                "otavio@email.com"
        );

        Inscricao inscricaoSalva = inscricaoRepository.saveAndFlush(inscricao);

        Long inscricaoId = inscricaoSalva.getId();

        entityManager.clear();

        Inscricao inscricaoEncontrada = inscricaoRepository.findById(inscricaoId).orElseThrow();

        assertAll(
                () -> assertNotNull(
                        inscricaoEncontrada.getId()
                ),
                () -> assertEquals(
                        1L,
                        inscricaoEncontrada.getEventoId()
                ),
                () -> assertEquals(
                        "Otavio Oliveira",
                        inscricaoEncontrada
                                .getNomeParticipante()
                ),
                () -> assertEquals(
                        "otavio@email.com",
                        inscricaoEncontrada
                                .getEmailParticipante()
                ),
                () -> assertNotNull(
                        inscricaoEncontrada
                                .getDataInscricao()
                )
        );
    }

    @Test
    @DisplayName("Deve listar inscrições de um evento")
    void deveListarInscricoesPorEvento() {
        Inscricao primeira = criarInscricao(
                1L,
                "Otavio Oliveira",
                "otavio@email.com"
        );

        Inscricao segunda = criarInscricao(
                1L,
                "Mirela Oliveira",
                "mirela@email.com"
        );

        Inscricao outroEvento = criarInscricao(
                2L,
                "Participante Teste",
                "teste@email.com"
        );

        inscricaoRepository.saveAll(List.of(primeira, segunda, outroEvento));

        inscricaoRepository.flush();
        entityManager.clear();

        List<Inscricao> resultados = inscricaoRepository.findByEventoId(1L);

        assertEquals(2, resultados.size());

        assertTrue(
                resultados.stream().allMatch(
                        inscricao ->
                                inscricao.getEventoId()
                                        .equals(1L)
                )
        );
    }

    @Test
    @DisplayName("Deve atualizar uma inscrição")
    void deveAtualizarInscricao() {
        Inscricao inscricaoSalva =
                inscricaoRepository.saveAndFlush(
                        criarInscricao(
                                1L,
                                "Nome Antigo",
                                "antigo@email.com"
                        )
                );

        Long inscricaoId = inscricaoSalva.getId();

        inscricaoSalva.setNomeParticipante("Nome Atualizado");

        inscricaoSalva.setEmailParticipante("atualizado@email.com");

        inscricaoRepository.saveAndFlush(inscricaoSalva);

        entityManager.clear();

        Inscricao inscricaoAtualizada = inscricaoRepository.findById(inscricaoId).orElseThrow();

        assertEquals(
                "Nome Atualizado",
                inscricaoAtualizada.getNomeParticipante()
        );

        assertEquals(
                "atualizado@email.com",
                inscricaoAtualizada.getEmailParticipante()
        );
    }

    @Test
    @DisplayName("Deve excluir uma inscrição")
    void deveExcluirInscricao() {
        Inscricao inscricaoSalva =
                inscricaoRepository.saveAndFlush(
                        criarInscricao(
                                1L,
                                "Participante",
                                "participante@email.com"
                        )
                );

        Long inscricaoId = inscricaoSalva.getId();

        inscricaoRepository.deleteById(inscricaoId);
        inscricaoRepository.flush();
        entityManager.clear();

        assertFalse(
                inscricaoRepository.existsById(
                        inscricaoId
                )
        );
    }
}
