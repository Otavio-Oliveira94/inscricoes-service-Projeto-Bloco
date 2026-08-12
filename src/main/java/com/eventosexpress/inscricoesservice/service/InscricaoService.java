package com.eventosexpress.inscricoesservice.service;

import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.exception.InscricaoNaoEncontradaException;
import com.eventosexpress.inscricoesservice.mapper.InscricaoMapper;
import com.eventosexpress.inscricoesservice.model.Inscricao;
import com.eventosexpress.inscricoesservice.repository.InscricaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {
    private final InscricaoRepository inscricaoRepository;
    private final InscricaoMapper inscricaoMapper;

    public InscricaoService(InscricaoRepository inscricaoRepository, InscricaoMapper inscricaoMapper) {
        this.inscricaoRepository = inscricaoRepository;
        this.inscricaoMapper = inscricaoMapper;
    }

    public InscricaoResponseDTO criar(InscricaoRequestDTO dto) {
        Inscricao inscricao = inscricaoMapper.paraEntidade(dto);

        inscricao.setDataInscricao(LocalDateTime.now());

        Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);

        return inscricaoMapper.paraResponseDTO(inscricaoSalva);
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

    public InscricaoResponseDTO editar(Long id, InscricaoRequestDTO dto) {
        Inscricao inscricao = buscarEntidadePorId(id);

        inscricaoMapper.atualizarEntidade(inscricao, dto);

        Inscricao inscricaoAtualizada = inscricaoRepository.save(inscricao);

        return inscricaoMapper.paraResponseDTO(inscricaoAtualizada);
    }

    public void remover(Long id) {
        Inscricao inscricao = buscarEntidadePorId(id);

        inscricaoRepository.delete(inscricao);
    }

    private Inscricao buscarEntidadePorId(Long id) {
        return inscricaoRepository.findById(id)
                .orElseThrow(
                        () -> new InscricaoNaoEncontradaException(id)
                );
    }
}
