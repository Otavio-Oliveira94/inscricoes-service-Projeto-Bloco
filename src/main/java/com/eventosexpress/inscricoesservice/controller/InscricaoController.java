package com.eventosexpress.inscricoesservice.controller;

import com.eventosexpress.inscricoesservice.dto.InscricaoRequestDTO;
import com.eventosexpress.inscricoesservice.dto.InscricaoResponseDTO;
import com.eventosexpress.inscricoesservice.service.InscricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscricoes")
@CrossOrigin(origins = "http://localhost:5173")
public class InscricaoController {
    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    @PostMapping
    public ResponseEntity<InscricaoResponseDTO> criar(@RequestBody InscricaoRequestDTO dto) {
        InscricaoResponseDTO inscricaoCriada = inscricaoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inscricaoCriada);
    }

    @GetMapping
    public ResponseEntity<List<InscricaoResponseDTO>> buscarTodas() {
        return ResponseEntity.ok(inscricaoService.buscarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inscricaoService.buscarPorId(id));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<InscricaoResponseDTO>> buscarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(inscricaoService.buscarPorEvento(eventoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InscricaoResponseDTO> editar(@PathVariable Long id, @RequestBody InscricaoRequestDTO dto) {
        return ResponseEntity.ok(inscricaoService.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        inscricaoService.remover(id);

        return ResponseEntity.noContent().build();
    }
}
