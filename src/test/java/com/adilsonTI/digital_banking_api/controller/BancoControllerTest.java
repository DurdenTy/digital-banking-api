package com.adilsonTI.digital_banking_api.controller;

import com.adilsonTI.digital_banking_api.dtos.ContaDTO;
import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.service.ContaService;
import com.adilsonTI.digital_banking_api.service.TransferenciaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BancoController.class)
class BancoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private TransferenciaService transferenciaService;

    @Test
    void deveCadastrarContaERetornarStatusCreated() throws Exception {
        ContaEntity conta = criarConta(1L, "Adilson", "100.00");

        Mockito.when(contaService.cadastrarConta(any(ContaDTO.class))).thenReturn(conta);

        String json = """
                {
                  "nome": "Adilson",
                  "saldo": 100.00
                }
                """;

        mockMvc.perform(post("/v1/banco/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Adilson"))
                .andExpect(jsonPath("$.saldo").value(100.00));

        verify(contaService).cadastrarConta(any(ContaDTO.class));
        verifyNoInteractions(transferenciaService);
    }

    @Test
    void deveRetornarErro422QuandoCadastroForInvalido() throws Exception {
        String json = """
                {
                  "nome": "",
                  "saldo": -10.00
                }
                """;

        mockMvc.perform(post("/v1/banco/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Erro validação"))
                .andExpect(jsonPath("$.erros").isArray());

        verifyNoInteractions(contaService);
        verifyNoInteractions(transferenciaService);
    }

    @Test
    void deveConsultarContaComSucesso() throws Exception {
        ContaEntity conta = criarConta(1L, "Adilson", "100.00");

        Mockito.when(contaService.consultarConta(1L)).thenReturn(conta);

        mockMvc.perform(get("/v1/banco/contas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Adilson"))
                .andExpect(jsonPath("$.saldo").value(100.00));

        verify(contaService).consultarConta(1L);
        verifyNoInteractions(transferenciaService);
    }

    @Test
    void deveTransferirComSucesso() throws Exception {
        String json = """
                {
                  "origemId": 1,
                  "destinoId": 2,
                  "saldo": 25.00
                }
                """;

        mockMvc.perform(post("/v1/banco/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferência realizada com sucesso."));

        verify(transferenciaService).transferir(
                1L,
                2L,
                new BigDecimal("25.00")
        );

        verifyNoInteractions(contaService);
    }

    @Test
    void deveRetornarErro422QuandoTransferenciaForInvalida() throws Exception {
        String json = """
                {
                  "origemId": 1,
                  "destinoId": 2,
                  "saldo": 0.00
                }
                """;

        mockMvc.perform(post("/v1/banco/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Erro validação"))
                .andExpect(jsonPath("$.erros").isArray());

        verifyNoInteractions(transferenciaService);
        verifyNoInteractions(contaService);
    }

    private ContaEntity criarConta(Long id, String nome, String saldo) {
        ContaEntity conta = new ContaEntity();
        conta.setId(id);
        conta.setNome(nome);
        conta.setSaldo(new BigDecimal(saldo));
        return conta;
    }
}