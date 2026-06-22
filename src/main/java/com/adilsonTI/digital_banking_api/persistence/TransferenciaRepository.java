package com.adilsonTI.digital_banking_api.persistence;

import com.adilsonTI.digital_banking_api.entities.TransferenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaRepository extends JpaRepository<TransferenciaEntity, Long> {

    List<TransferenciaEntity> findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(
            Long contaOrigemId,
            Long contaDestinoId
    );

}
