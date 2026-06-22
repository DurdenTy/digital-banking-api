package com.adilsonTI.digital_banking_api.service;

import com.adilsonTI.digital_banking_api.configurations.rabbitMQ.RabbitMQConfig;
import com.adilsonTI.digital_banking_api.dtos.notification.ContaCriadaNotificationDTO;
import com.adilsonTI.digital_banking_api.dtos.notification.TransferenciaRealizadaNotificationDTO;
import com.adilsonTI.digital_banking_api.service.rabbitMQ.NotificacaoProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificacaoProducer notificacaoProducer;

    @Test
    void devePublicarEventoDeContaCriada() {
        ContaCriadaNotificationDTO notificacao = new ContaCriadaNotificationDTO(
                1L,
                "Adilson",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        notificacaoProducer.publicarContaCriada(notificacao);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NOTIFICACOES,
                RabbitMQConfig.ROUTING_KEY_CONTA_CRIADA,
                notificacao
        );
    }

    @Test
    void devePublicarEventoDeTransferenciaRealizada() {
        TransferenciaRealizadaNotificationDTO notificacao = new TransferenciaRealizadaNotificationDTO(
                10L,
                1L,
                2L,
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        notificacaoProducer.publicarTransferenciaRealizada(notificacao);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NOTIFICACOES,
                RabbitMQConfig.ROUTING_KEY_TRANSFERENCIA_REALIZADA,
                notificacao
        );
    }
}