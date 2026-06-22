package com.adilsonTI.digital_banking_api.dtos.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaRealizadaNotificationDTO(
        Long transferenciaId,
        Long origemId,
        Long destinoId,
        BigDecimal valor,
        LocalDateTime dataHora
) {
}