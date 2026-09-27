package com.eventosexpress.inscricoesservice.repository;

import com.eventosexpress.inscricoesservice.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {
    List<Inscricao> findByEventoId(Long eventoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inscricao i where i.id = :id")
    Optional<Inscricao> buscarPorIdComBloqueio(@Param("id") Long id);
}
