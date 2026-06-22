package com.adilsonTI.digital_banking_api.persistence;

import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<ContaEntity, Long> {

    // ESSA LINHA RESOLVE A ALTA CONCORRÊNCIA!
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaEntity c WHERE c.id = :id")
    Optional<ContaEntity> findByIdWithLock(Long id);

}
