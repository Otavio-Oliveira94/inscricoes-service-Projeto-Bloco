package com.eventosexpress.inscricoesservice.service;

import com.eventosexpress.inscricoesservice.auditoria.AuditoriaService;
import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.mapper.InscricaoMapper;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import com.eventosexpress.inscricoesservice.repository.InscricaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.eventosexpress.contratos.InscricaoConfirmada;

import com.eventosexpress.contratos.ValidarEventoParaInscricao;
import com.eventosexpress.inscricoesservice.model.StatusInscricao;
import org.springframework.web.server.ResponseStatusException;

import com.eventosexpress.contratos.EventoValidadoParaInscricao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {
    private final InscricaoRepository inscricaoRepository;
    private final InscricaoMapper inscricaoMapper;
    private final AuditoriaService auditoriaService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final Logger log = LoggerFactory.getLogger(InscricaoService.class);

    public InscricaoService(InscricaoRepository inscricaoRepository, InscricaoMapper inscricaoMapper, AuditoriaService auditoriaService, ApplicationEventPublisher applicationEventPublisher) {
        this.inscricaoRepository = inscricaoRepository;
        this.inscricaoMapper = inscricaoMapper;
        this.auditoriaService = auditoriaService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public InscricaoResponseDTO criar(InscricaoRequestDTO dto) {
        if (dto.getEventoId() == null || dto.getEventoId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um eventoId maior que zero."
            );
        }

        Inscricao inscricao = inscricaoMapper.paraEntidade(dto);

        inscricao.setDataInscricao(LocalDateTime.now());
        inscricao.setStatus(StatusInscricao.PENDENTE);
        inscricao.setSolicitacaoId(UUID.randomUUID());
        inscricao.setMotivoRejeicao(null);

        Inscricao inscricaoSalva =
                inscricaoRepository.save(inscricao);

        InscricaoResponseDTO resposta =
                inscricaoMapper.paraResponseDTO(inscricaoSalva);

        auditoriaService.registrar(
                "CRIACAO",
                inscricaoSalva.getId(),
                null,
                resposta
        );

        ValidarEventoParaInscricao solicitacao =
                new ValidarEventoParaInscricao(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        inscricaoSalva.getSolicitacaoId(),
                        inscricaoSalva.getId(),
                        inscricaoSalva.getEventoId()
                );

        applicationEventPublisher.publishEvent(solicitacao);

        return resposta;
    }

    public List<InscricaoResponseDTO> buscarTodas() {
        return inscricaoRepository.findAll()
                .stream()
                .map(inscricaoMapper::paraResponseDTO)
                .toList();
    }

    public InscricaoResponseDTO buscarPorId(Long id) {
        Inscricao inscricao = buscarEntidadePorId(id);

        return inscricaoMapper.paraResponseDTO(inscricao);
    }

    public List<InscricaoResponseDTO> buscarPorEvento(Long eventoId) {
        return inscricaoRepository
                .findByEventoId(eventoId)
                .stream()
                .map(inscricaoMapper::paraResponseDTO)
                .toList();
    }

    @Transactional
    public InscricaoResponseDTO editar(
            Long id,
            InscricaoRequestDTO dto
    ) {
        if (dto.getEventoId() == null || dto.getEventoId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um eventoId maior que zero."
            );
        }

        Inscricao inscricao =
                inscricaoRepository.buscarPorIdComBloqueio(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Inscricao nao encontrada."
                                )
                        );

        InscricaoResponseDTO antes =
                inscricaoMapper.paraResponseDTO(inscricao);

        boolean eventoAlterado = !Objects.equals(
                inscricao.getEventoId(),
                dto.getEventoId()
        );

        inscricaoMapper.atualizarEntidade(inscricao, dto);

        if (eventoAlterado) {
            inscricao.setStatus(StatusInscricao.PENDENTE);
            inscricao.setSolicitacaoId(UUID.randomUUID());
            inscricao.setMotivoRejeicao(null);
        }

        Inscricao salva = inscricaoRepository.save(inscricao);

        InscricaoResponseDTO depois = inscricaoMapper.paraResponseDTO(salva);

        auditoriaService.registrar(
                "ATUALIZACAO",
                salva.getId(),
                antes,
                depois
        );

        if (eventoAlterado) {
            ValidarEventoParaInscricao solicitacao =
                    new ValidarEventoParaInscricao(
                            UUID.randomUUID(),
                            1,
                            Instant.now(),
                            salva.getSolicitacaoId(),
                            salva.getId(),
                            salva.getEventoId()
                    );

            applicationEventPublisher.publishEvent(solicitacao);
        }

        return depois;
    }

    @Transactional
    public void remover(Long id) {
        Inscricao inscricao =
                inscricaoRepository.buscarPorIdComBloqueio(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Inscricao nao encontrada."
                                )
                        );

        InscricaoResponseDTO antes =
                inscricaoMapper.paraResponseDTO(inscricao);

        inscricaoRepository.delete(inscricao);

        auditoriaService.registrar(
                "EXCLUSAO",
                inscricao.getId(),
                antes,
                null
        );
    }

    private Inscricao buscarEntidadePorId(Long id) {
        return inscricaoRepository.findById(id)
                .orElseThrow(
                        () -> new InscricaoNaoEncontradaException(id)
                );
    }

    @Transactional
    public void aplicarResultadoValidacao(
            EventoValidadoParaInscricao resultado
    ) {
        if (resultado == null) {
            log.warn("Resultado de validacao nulo ignorado.");
            return;
        }

        boolean contratoInvalido =
                resultado.versaoMensagem() != 1
                        || resultado.mensagemId() == null
                        || resultado.solicitacaoId() == null
                        || resultado.inscricaoId() == null
                        || resultado.eventoId() == null;

        if (contratoInvalido) {
            log.warn(
                    "Resultado de validacao ignorado por contrato invalido. "
                            + "Mensagem: {}",
                    resultado.mensagemId()
            );
            return;
        }

        var inscricaoEncontrada =
                inscricaoRepository.buscarPorIdComBloqueio(
                        resultado.inscricaoId()
                );

        if (inscricaoEncontrada.isEmpty()) {
            log.info(
                    "Resultado ignorado porque a inscricao {} nao existe.",
                    resultado.inscricaoId()
            );
            return;
        }

        Inscricao inscricao = inscricaoEncontrada.get();

        boolean mesmaSolicitacao = Objects.equals(
                inscricao.getSolicitacaoId(),
                resultado.solicitacaoId()
        );

        boolean mesmoEvento = Objects.equals(
                inscricao.getEventoId(),
                resultado.eventoId()
        );

        if (!mesmaSolicitacao || !mesmoEvento) {
            log.info(
                    "Resposta antiga ignorada. Inscricao: {}, "
                            + "evento recebido: {}, solicitacao recebida: {}",
                    resultado.inscricaoId(),
                    resultado.eventoId(),
                    resultado.solicitacaoId()
            );
            return;
        }

        if (inscricao.getStatus() != StatusInscricao.PENDENTE) {
            log.info(
                    "Resultado repetido ignorado. Inscricao: {}, "
                            + "status atual: {}",
                    inscricao.getId(),
                    inscricao.getStatus()
            );
            return;
        }

        InscricaoResponseDTO antes =
                inscricaoMapper.paraResponseDTO(inscricao);

        boolean inscricaoConfirmada =
                resultado.eventoExiste();

        if (inscricaoConfirmada) {
            inscricao.setStatus(
                    StatusInscricao.CONFIRMADA
            );

            inscricao.setMotivoRejeicao(null);
        } else {
            inscricao.setStatus(
                    StatusInscricao.REJEITADA
            );

            String motivoRecebido =
                    resultado.motivo();

            String motivoFinal =
                    motivoRecebido == null
                            || motivoRecebido.isBlank()
                            ? "Evento nao encontrado."
                            : motivoRecebido;

            inscricao.setMotivoRejeicao(
                    motivoFinal
            );
        }

        Inscricao salva =
                inscricaoRepository.save(inscricao);

        InscricaoResponseDTO depois =
                inscricaoMapper.paraResponseDTO(salva);

        auditoriaService.registrar(
                "ATUALIZACAO",
                salva.getId(),
                antes,
                depois
        );

        if (inscricaoConfirmada) {
            InscricaoConfirmada eventoConfirmado =
                    new InscricaoConfirmada(
                            UUID.randomUUID(),
                            1,
                            Instant.now(),
                            salva.getSolicitacaoId(),
                            salva.getId(),
                            salva.getEventoId(),
                            salva.getNomeParticipante(),
                            salva.getEmailParticipante()
                    );

            applicationEventPublisher.publishEvent(
                    eventoConfirmado
            );
        }

        log.info(
                "Validacao aplicada. Inscricao: {}, "
                        + "status: {}, solicitacao: {}",
                salva.getId(),
                salva.getStatus(),
                salva.getSolicitacaoId()
        );
    }
}
