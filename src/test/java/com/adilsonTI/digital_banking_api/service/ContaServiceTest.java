package com.adilsonTI.digital_banking_api.service;

import com.adilsonTI.digital_banking_api.dtos.ContaDTO;
import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.events.ContaCriadaEvent;
import com.adilsonTI.digital_banking_api.persistence.ContaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ContaService contaService;

    @Test
    void deveConsultarContaComSucesso() {
        ContaEntity conta = criarConta(1L, "Adilson", "100.00");

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        ContaEntity resultado = contaService.consultarConta(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Adilson", resultado.getNome());
        assertEquals(0, resultado.getSaldo().compareTo(new BigDecimal("100.00")));

        verify(contaRepository).findById(1L);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveLancarErroQuandoContaNaoForEncontrada() {
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contaService.consultarConta(99L)
        );

        assertEquals("Conta de destino não encontrada.", exception.getMessage());

        verify(contaRepository).findById(99L);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveCadastrarContaComSucesso() {
        ContaDTO contaDTO = new ContaDTO("Adilson", new BigDecimal("250.00"));

        when(contaRepository.save(any(ContaEntity.class))).thenAnswer(invocation -> {
            ContaEntity conta = invocation.getArgument(0);
            conta.setId(1L);
            return conta;
        });

        ContaEntity resultado = contaService.cadastrarConta(contaDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Adilson", resultado.getNome());
        assertEquals(0, resultado.getSaldo().compareTo(new BigDecimal("250.00")));

        ArgumentCaptor<ContaEntity> contaCaptor = ArgumentCaptor.forClass(ContaEntity.class);

        verify(contaRepository).save(contaCaptor.capture());

        ContaEntity contaSalva = contaCaptor.getValue();

        assertEquals("Adilson", contaSalva.getNome());
        assertEquals(0, contaSalva.getSaldo().compareTo(new BigDecimal("250.00")));

        ArgumentCaptor<ContaCriadaEvent> eventCaptor = ArgumentCaptor.forClass(ContaCriadaEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ContaCriadaEvent evento = eventCaptor.getValue();

        assertNotNull(evento);
        assertEquals(1L, evento.conta().getId());
        assertEquals("Adilson", evento.conta().getNome());
        assertEquals(0, evento.conta().getSaldo().compareTo(new BigDecimal("250.00")));
    }

    @Test
    void naoDevePublicarEventoQuandoCadastroFalhar() {
        ContaDTO contaDTO = new ContaDTO("Adilson", new BigDecimal("250.00"));

        when(contaRepository.save(any(ContaEntity.class)))
                .thenThrow(new RuntimeException("Erro ao salvar conta."));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> contaService.cadastrarConta(contaDTO)
        );

        assertEquals("Erro ao salvar conta.", exception.getMessage());

        verify(contaRepository).save(any(ContaEntity.class));
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