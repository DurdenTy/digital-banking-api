package com.adilsonTI.digital_banking_api.service;

import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.entities.TransferenciaEntity;
import com.adilsonTI.digital_banking_api.events.TransferenciaRealizadaEvent;
import com.adilsonTI.digital_banking_api.persistence.ContaRepository;
import com.adilsonTI.digital_banking_api.persistence.TransferenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Test
    void deveTransferirComSucesso() {
        ContaEntity origem = criarConta(1L, "Conta Origem", "100.00");
        ContaEntity destino = criarConta(2L, "Conta Destino", "50.00");

        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        when(transferenciaRepository.save(any(TransferenciaEntity.class))).thenAnswer(invocation -> {
            TransferenciaEntity transferencia = invocation.getArgument(0);
            transferencia.setId(10L);
            return transferencia;
        });

        transferenciaService.transferir(1L, 2L, new BigDecimal("25.00"));

        assertEquals(0, origem.getSaldo().compareTo(new BigDecimal("75.00")));
        assertEquals(0, destino.getSaldo().compareTo(new BigDecimal("75.00")));

        verify(contaRepository).findByIdWithLock(1L);
        verify(contaRepository).findByIdWithLock(2L);

        verify(contaRepository).save(origem);
        verify(contaRepository).save(destino);

        ArgumentCaptor<TransferenciaEntity> transferenciaCaptor =
                ArgumentCaptor.forClass(TransferenciaEntity.class);

        verify(transferenciaRepository).save(transferenciaCaptor.capture());

        TransferenciaEntity transferenciaSalva = transferenciaCaptor.getValue();

        assertEquals(1L, transferenciaSalva.getContaOrigemId());
        assertEquals(2L, transferenciaSalva.getContaDestinoId());
        assertEquals(0, transferenciaSalva.getValor().compareTo(new BigDecimal("25.00")));
        assertNotNull(transferenciaSalva.getDataHora());

        ArgumentCaptor<TransferenciaRealizadaEvent> eventCaptor =
                ArgumentCaptor.forClass(TransferenciaRealizadaEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TransferenciaRealizadaEvent evento = eventCaptor.getValue();

        assertNotNull(evento);
        assertEquals(10L, evento.transferencia().getId());
        assertEquals(1L, evento.transferencia().getContaOrigemId());
        assertEquals(2L, evento.transferencia().getContaDestinoId());
        assertEquals(0, evento.transferencia().getValor().compareTo(new BigDecimal("25.00")));
    }

    @Test
    void deveBloquearContasSempreEmOrdemCrescenteParaEvitarDeadlock() {
        ContaEntity conta1 = criarConta(1L, "Conta 1", "100.00");
        ContaEntity conta2 = criarConta(2L, "Conta 2", "100.00");

        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(conta1));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(conta2));

        when(transferenciaRepository.save(any(TransferenciaEntity.class))).thenAnswer(invocation -> {
            TransferenciaEntity transferencia = invocation.getArgument(0);
            transferencia.setId(20L);
            return transferencia;
        });

        transferenciaService.transferir(2L, 1L, new BigDecimal("10.00"));

        InOrder inOrder = inOrder(contaRepository);

        inOrder.verify(contaRepository).findByIdWithLock(1L);
        inOrder.verify(contaRepository).findByIdWithLock(2L);

        assertEquals(0, conta2.getSaldo().compareTo(new BigDecimal("90.00")));
        assertEquals(0, conta1.getSaldo().compareTo(new BigDecimal("110.00")));

        verify(eventPublisher).publishEvent(any(TransferenciaRealizadaEvent.class));
    }

    @Test
    void deveLancarErroQuandoOrigemEDestinoForemIguais() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(1L, 1L, new BigDecimal("10.00"))
        );

        assertEquals(
                "A conta de origem não pode ser igual à conta de destino.",
                exception.getMessage()
        );

        verifyNoInteractions(contaRepository);
        verifyNoInteractions(transferenciaRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveLancarErroQuandoSaldoForInsuficiente() {
        ContaEntity origem = criarConta(1L, "Conta Origem", "5.00");
        ContaEntity destino = criarConta(2L, "Conta Destino", "50.00");

        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> transferenciaService.transferir(1L, 2L, new BigDecimal("10.00"))
        );

        assertEquals("Saldo insuficiente.", exception.getMessage());

        assertEquals(0, origem.getSaldo().compareTo(new BigDecimal("5.00")));
        assertEquals(0, destino.getSaldo().compareTo(new BigDecimal("50.00")));

        verify(contaRepository).findByIdWithLock(1L);
        verify(contaRepository).findByIdWithLock(2L);

        verify(contaRepository, never()).save(any(ContaEntity.class));
        verify(transferenciaRepository, never()).save(any(TransferenciaEntity.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveLancarErroQuandoPrimeiraContaNaoForEncontrada() {
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(1L, 2L, new BigDecimal("10.00"))
        );

        assertEquals("Conta não encontrada: 1", exception.getMessage());

        verify(contaRepository).findByIdWithLock(1L);
        verify(contaRepository, never()).findByIdWithLock(2L);

        verify(contaRepository, never()).save(any(ContaEntity.class));
        verify(transferenciaRepository, never()).save(any(TransferenciaEntity.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveLancarErroQuandoSegundaContaNaoForEncontrada() {
        ContaEntity origem = criarConta(1L, "Conta Origem", "100.00");

        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(1L, 2L, new BigDecimal("10.00"))
        );

        assertEquals("Conta não encontrada: 2", exception.getMessage());

        verify(contaRepository).findByIdWithLock(1L);
        verify(contaRepository).findByIdWithLock(2L);

        verify(contaRepository, never()).save(any(ContaEntity.class));
        verify(transferenciaRepository, never()).save(any(TransferenciaEntity.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void naoDevePublicarEventoQuandoFalharAoSalvarTransferencia() {
        ContaEntity origem = criarConta(1L, "Conta Origem", "100.00");
        ContaEntity destino = criarConta(2L, "Conta Destino", "50.00");

        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        when(transferenciaRepository.save(any(TransferenciaEntity.class)))
                .thenThrow(new RuntimeException("Erro ao salvar transferência."));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transferenciaService.transferir(1L, 2L, new BigDecimal("25.00"))
        );

        assertEquals("Erro ao salvar transferência.", exception.getMessage());

        verify(contaRepository).save(origem);
        verify(contaRepository).save(destino);
        verify(transferenciaRepository).save(any(TransferenciaEntity.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveListarMovimentacoesDaConta() {
        TransferenciaEntity transferencia = new TransferenciaEntity();
        transferencia.setId(1L);
        transferencia.setContaOrigemId(1L);
        transferencia.setContaDestinoId(2L);
        transferencia.setValor(new BigDecimal("15.00"));

        when(transferenciaRepository.findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(1L, 1L))
                .thenReturn(List.of(transferencia));

        var resultado = transferenciaService.listarMovimentacoes(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        assertEquals(1L, resultado.get(0).getId());
        assertEquals(1L, resultado.get(0).getOrigemId());
        assertEquals(2L, resultado.get(0).getDestinoId());
        assertEquals(0, resultado.get(0).getValor().compareTo(new BigDecimal("15.00")));

        verify(transferenciaRepository)
                .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDesc(1L, 1L);

        verifyNoInteractions(eventPublisher);
    }

    private ContaEntity criarConta(Long id, String nome, String saldo) {
        ContaEntity conta = new ContaEntity();
        conta.setId(id);
        conta.setNome(nome);
        conta.setSaldo(new BigDecimal(saldo));
        return conta;
    }
}