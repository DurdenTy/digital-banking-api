package com.adilsonTI.digital_banking_api.dtos.response;

import com.adilsonTI.digital_banking_api.entities.ContaEntity;

import java.math.BigDecimal;

public record ContaResponseDTO(
        Long id,
        String nome,
        BigDecimal saldo
) {
    public static ContaResponseDTO fromEntity(ContaEntity conta) {
        return new ContaResponseDTO(
                conta.getId(),
                conta.getNome(),
                conta.getSaldo()
        );
    }
}