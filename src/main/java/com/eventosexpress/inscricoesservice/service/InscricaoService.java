package com.eventosexpress.inscricoesservice.service;

import com.eventosexpress.inscricoesservice.auditoria.AuditoriaService;
import com.eventosexpress.inscricoesservice.client.EventoClient;
import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.EventoNaoEncontradoException;
import com.eventosexpress.inscricoesservice.exception.EventoServiceIndisponivelException;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.mapper.InscricaoMapper;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import com.eventosexpress.inscricoesservice.repository.InscricaoRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {
    private final InscricaoRepository inscricaoRepository;
    private final InscricaoMapper inscricaoMapper;
    private final EventoClient eventoClient;
    private final AuditoriaService auditoriaService;

    public InscricaoService(InscricaoRepository inscricaoRepository, InscricaoMapper inscricaoMapper, EventoClient eventoClient, AuditoriaService auditoriaService) {
        this.inscricaoRepository = inscricaoRepository;
        this.inscricaoMapper = inscricaoMapper;
        this.eventoClient = eventoClient;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public InscricaoResponseDTO criar(InscricaoRequestDTO dto) {
        validarEvento(dto.getEventoId());

        Inscricao inscricao = inscricaoMapper.paraEntidade(dto);

        inscricao.setDataInscricao(LocalDateTime.now());

        Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);

        InscricaoResponseDTO resposta = inscricaoMapper.paraResponseDTO(inscricaoSalva);
        auditoriaService.registrar("CRIACAO", inscricaoSalva.getId(), null, resposta);
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
    public InscricaoResponseDTO editar(Long id, InscricaoRequestDTO dto) {
        Inscricao inscricao = buscarEntidadePorId(id);
        validarEvento(dto.getEventoId());

        InscricaoResponseDTO antes = inscricaoMapper.paraResponseDTO(inscricao);
        inscricaoMapper.atualizarEntidade(inscricao, dto);

        Inscricao inscricaoAtualizada = inscricaoRepository.save(inscricao);

        InscricaoResponseDTO depois = inscricaoMapper.paraResponseDTO(inscricaoAtualizada);
        auditoriaService.registrar("ATUALIZACAO", id, antes, depois);
        return depois;
    }

    @Transactional
    public void remover(Long id) {
        Inscricao inscricao = buscarEntidadePorId(id);

        InscricaoResponseDTO antes = inscricaoMapper.paraResponseDTO(inscricao);
        inscricaoRepository.delete(inscricao);
        auditoriaService.registrar("EXCLUSAO", id, antes, null);
    }

    private Inscricao buscarEntidadePorId(Long id) {
        return inscricaoRepository.findById(id)
                .orElseThrow(
                        () -> new InscricaoNaoEncontradaException(id)
                );
    }

    private void validarEvento(Long eventoId) {
          try{
              eventoClient.buscarPorId(eventoId);
          } catch (FeignException.NotFound exception) {
              throw new EventoNaoEncontradoException(eventoId);
          } catch (FeignException exception) {
              throw new EventoServiceIndisponivelException();
          }
    }
}
