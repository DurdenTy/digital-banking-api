package com.adilsonTI.digital_banking_api.dtos.response;

import com.adilsonTI.digital_banking_api.entities.TransferenciaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferenciaResponseDTO {

    private Long id;
    private Long origemId;
    private Long destinoId;
    private BigDecimal valor;
    private LocalDateTime dataHora;

    public static TransferenciaResponseDTO fromEntity(TransferenciaEntity transferenciaEntity) {
        return new TransferenciaResponseDTO(
                transferenciaEntity.getId(),
                transferenciaEntity.getContaOrigemId(),
                transferenciaEntity.getContaDestinoId(),
                transferenciaEntity.getValor(),
                transferenciaEntity.getDataHora()
        );
    }
}