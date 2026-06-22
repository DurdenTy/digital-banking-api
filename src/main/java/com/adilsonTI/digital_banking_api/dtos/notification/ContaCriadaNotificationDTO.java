package
com.adilsonTI.digital_banking_api.dtos.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContaCriadaNotificationDTO(
        Long contaId,
        String nome,
        BigDecimal saldo,
        LocalDateTime dataHora
) {
}