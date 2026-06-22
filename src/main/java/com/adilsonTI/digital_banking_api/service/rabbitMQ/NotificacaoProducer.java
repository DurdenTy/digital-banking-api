package com.adilsonTI.digital_banking_api.service.rabbitMQ;

import com.adilsonTI.digital_banking_api.configurations.rabbitMQ.RabbitMQConfig;
import com.adilsonTI.digital_banking_api.dtos.notification.ContaCriadaNotificationDTO;
import com.adilsonTI.digital_banking_api.dtos.notification.TransferenciaRealizadaNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificacaoProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publicarContaCriada(ContaCriadaNotificationDTO notificacao){

        try {


            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NOTIFICACOES,
                    RabbitMQConfig.ROUTING_KEY_CONTA_CRIADA,
                    notificacao);

            log.info(
                    "Notificação de conta criada publicada. Conta ID: {}",
                    notificacao.contaId()
            );
        }catch (AmqpException e){
            log.warn(
                    "Não foi possível publicar evento de conta criada. Conta ID: {}. Motivo: {}",
                    notificacao.contaId(),
                    e.getMessage()
            );
        }

    }

    public void publicarTransferenciaRealizada(TransferenciaRealizadaNotificationDTO notificacao) {
       try {
                rabbitTemplate.convertAndSend(
                       RabbitMQConfig.EXCHANGE_NOTIFICACOES,
                       RabbitMQConfig.ROUTING_KEY_TRANSFERENCIA_REALIZADA,
                       notificacao);

                log.info(
                        "Notificação de transferência publicada. Transferência ID: {}",
                        notificacao.transferenciaId()
                );

            } catch (AmqpException e) {
                log.warn(
                        "Não foi possível publicar evento de transferência realizada. Transferência ID: {}. Motivo: {}",
                        notificacao.transferenciaId(),
                        e.getMessage()
                );
            }
    }
}
