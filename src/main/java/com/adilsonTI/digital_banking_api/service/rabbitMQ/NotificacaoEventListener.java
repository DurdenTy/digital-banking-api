package com.adilsonTI.digital_banking_api.service.rabbitMQ;

import com.adilsonTI.digital_banking_api.dtos.notification.ContaCriadaNotificationDTO;
import com.adilsonTI.digital_banking_api.dtos.notification.TransferenciaRealizadaNotificationDTO;
import com.adilsonTI.digital_banking_api.events.ContaCriadaEvent;
import com.adilsonTI.digital_banking_api.events.TransferenciaRealizadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoEventListener {

    private final NotificacaoProducer notificacaoProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarConta(ContaCriadaEvent event) {
        var conta = event.conta();

        var notificacao = new ContaCriadaNotificationDTO(
                conta.getId(),
                conta.getNome(),
                conta.getSaldo(),
                LocalDateTime.now()
        );

        notificacaoProducer.publicarContaCriada(notificacao);

        log.info(
                "Evento de conta criada processado após commit. Conta ID: {}",
                conta.getId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRealizarTransferencia(TransferenciaRealizadaEvent event) {
        var transferencia = event.transferencia();

        var notificacao = new TransferenciaRealizadaNotificationDTO(
                transferencia.getId(),
                transferencia.getContaOrigemId(),
                transferencia.getContaDestinoId(),
                transferencia.getValor(),
                transferencia.getDataHora()
        );

        notificacaoProducer.publicarTransferenciaRealizada(notificacao);

        log.info(
                "Evento de transferência processado após commit. Transferência ID: {}",
                transferencia.getId()
        );
    }
}