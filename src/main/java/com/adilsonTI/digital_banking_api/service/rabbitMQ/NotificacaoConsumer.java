package com.adilsonTI.digital_banking_api.service.rabbitMQ;

import com.adilsonTI.digital_banking_api.configurations.rabbitMQ.RabbitMQConfig;
import com.adilsonTI.digital_banking_api.dtos.notification.ContaCriadaNotificationDTO;
import com.adilsonTI.digital_banking_api.dtos.notification.TransferenciaRealizadaNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificacaoConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CONTA_CRIADA)
    public void consumirContaCriada(ContaCriadaNotificationDTO notificacao) {
        log.info(
                "[CONSUMER] Notificação recebida: conta criada. Conta ID: {}, Nome: {}, Saldo inicial: R$ {}",
                notificacao.contaId(),
                notificacao.nome(),
                notificacao.saldo()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSFERENCIA_REALIZADA)
    public void consumirTransferenciaRealizada(TransferenciaRealizadaNotificationDTO notificacao) {
        log.info(
                "[CONSUMER] Notificação recebida: transferência realizada. Transferência ID: {}, Origem: {}, Destino: {}, Valor: R$ {}",
                notificacao.transferenciaId(),
                notificacao.origemId(),
                notificacao.destinoId(),
                notificacao.valor()
        );
    }
}