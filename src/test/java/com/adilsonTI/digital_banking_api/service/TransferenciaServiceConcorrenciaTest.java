package com.adilsonTI.digital_banking_api.service;

import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.persistence.ContaRepository;
import com.adilsonTI.digital_banking_api.persistence.TransferenciaRepository;
import com.adilsonTI.digital_banking_api.service.rabbitMQ.NotificacaoProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferenciaServiceConcorrenciaTest {

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private ContaRepository contaRepository;

    @MockBean
    private NotificacaoProducer notificacaoProducer;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @BeforeEach
    void limparBase() {
        transferenciaRepository.deleteAll();
        contaRepository.deleteAll();
    }

    @Test
    void deveProcessarTransferenciasConcorrentesSemPerderConsistencia() throws InterruptedException {
        ContaEntity contaOrigem = new ContaEntity();
        contaOrigem.setNome("Conta Origem");
        contaOrigem.setSaldo(new BigDecimal("1000.00"));

        ContaEntity contaDestino = new ContaEntity();
        contaDestino.setNome("Conta Destino");
        contaDestino.setSaldo(new BigDecimal("0.00"));

        contaOrigem = contaRepository.save(contaOrigem);
        contaDestino = contaRepository.save(contaDestino);

        Long origemId = contaOrigem.getId();
        Long destinoId = contaDestino.getId();

        int quantidadeTransferencias = 100;
        BigDecimal valorTransferencia = new BigDecimal("10.00");

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);
        CountDownLatch fimExecucoes = new CountDownLatch(quantidadeTransferencias);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        for (int i = 0; i < quantidadeTransferencias; i++) {
            executorService.submit(() -> {
                try {
                    inicioSimultaneo.await();

                    transferenciaService.transferir(
                            origemId,
                            destinoId,
                            valorTransferencia
                    );

                    sucessos.incrementAndGet();

                } catch (Exception e) {
                    falhas.incrementAndGet();

                } finally {
                    fimExecucoes.countDown();
                }
            });
        }

        inicioSimultaneo.countDown();

        boolean terminou = fimExecucoes.await(30, TimeUnit.SECONDS);

        executorService.shutdown();

        assertTrue(terminou, "As transferências não terminaram dentro do tempo esperado.");

        ContaEntity origemAtualizada = contaRepository.findById(origemId).orElseThrow();
        ContaEntity destinoAtualizada = contaRepository.findById(destinoId).orElseThrow();

        assertEquals(0, origemAtualizada.getSaldo().compareTo(new BigDecimal("0.00")));
        assertEquals(0, destinoAtualizada.getSaldo().compareTo(new BigDecimal("1000.00")));

        assertEquals(100, sucessos.get());
        assertEquals(0, falhas.get());

        long totalTransferenciasRegistradas = transferenciaRepository.count();
        assertEquals(100, totalTransferenciasRegistradas);
    }
}