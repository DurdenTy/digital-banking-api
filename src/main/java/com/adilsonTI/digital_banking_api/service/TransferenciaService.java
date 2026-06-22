package com.adilsonTI.digital_banking_api.service;

import com.adilsonTI.digital_banking_api.events.TransferenciaRealizadaEvent;
import com.adilsonTI.digital_banking_api.dtos.response.TransferenciaResponseDTO;
import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.entities.TransferenciaEntity;
import com.adilsonTI.digital_banking_api.persistence.ContaRepository;
import com.adilsonTI.digital_banking_api.persistence.TransferenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferenciaService {

    private final ContaRepository contaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void transferir(Long origemId, Long destinoId, BigDecimal valor){

        StopWatch stopWatch = new StopWatch();

        log.info("Iniciando processo. Origem: {}, Destino: {}, Valor: R$ {}", origemId, destinoId, valor);

        try {

            if (origemId.equals(destinoId)) {
                throw new IllegalArgumentException("A conta de origem não pode ser igual à conta de destino.");
            }


            stopWatch.start();

            Long primeiroId = Math.min(origemId, destinoId);
            Long segundoId = Math.max(origemId, destinoId);

            ContaEntity primeiraConta = contaRepository.findByIdWithLock(primeiroId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + primeiroId));

            ContaEntity segundaConta = contaRepository.findByIdWithLock(segundoId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + segundoId));

            ContaEntity contaOrigem = origemId.equals(primeiroId) ? primeiraConta : segundaConta;
            ContaEntity contaDestino = destinoId.equals(primeiroId) ? primeiraConta : segundaConta;

            if (contaOrigem.getSaldo().compareTo(valor) < 0) {
                log.warn(" Abortado: Saldo insuficiente na conta de origem: {}", origemId);
                throw new IllegalStateException("Saldo insuficiente.");
            }

            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
            contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

            contaRepository.save(contaOrigem);
            contaRepository.save(contaDestino);

            TransferenciaEntity transferencia = new TransferenciaEntity();
            transferencia.setContaOrigemId(origemId);
            transferencia.setContaDestinoId(destinoId);
            transferencia.setValor(valor);
            transferencia.setDataHora(LocalDateTime.now());

            transferenciaRepository.save(transferencia);

            eventPublisher.publishEvent(new TransferenciaRealizadaEvent(transferencia));

            log.info("Registros atualizados no banco de dados com sucesso.");

        } catch (Exception e) {
            log.error("Erro catastrófico ou de validação ao transferir valores: {}", e.getMessage());
            throw e;
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            log.info("Fim da thread de execução do método. Tempo total gasto: {} ms", stopWatch.getTotalTimeMillis());
        }
    }

    public List<TransferenciaResponseDTO> listarMovimentacoes(Long contaId) {
        return transferenciaRepository
                .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(contaId, contaId).stream().map(TransferenciaResponseDTO::fromEntity).toList();
    }
}