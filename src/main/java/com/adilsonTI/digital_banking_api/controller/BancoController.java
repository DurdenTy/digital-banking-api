package com.adilsonTI.digital_banking_api.controller;

import com.adilsonTI.digital_banking_api.dtos.ContaDTO;
import com.adilsonTI.digital_banking_api.dtos.TransferenciaDTO;
import com.adilsonTI.digital_banking_api.dtos.response.ContaResponseDTO;
import com.adilsonTI.digital_banking_api.dtos.response.TransferenciaResponseDTO;
import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import com.adilsonTI.digital_banking_api.service.ContaService;
import com.adilsonTI.digital_banking_api.service.TransferenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/banco")
@RequiredArgsConstructor
public class BancoController {

    private final ContaService contaService;
    private final TransferenciaService transferenciaService;

    @GetMapping("/contas/{id}")
    public ResponseEntity<ContaResponseDTO> consultarConta(@PathVariable Long id){

       ContaEntity contaEntity = contaService.consultarConta(id);
        ContaResponseDTO response = ContaResponseDTO.fromEntity(contaEntity);
       return ResponseEntity.ok(response);

    }

    @PostMapping("/contas")
    public ResponseEntity<ContaResponseDTO> cadastrarConta(@RequestBody @Valid ContaDTO contaDTO){

        ContaEntity contaEntity = contaService.cadastrarConta(contaDTO);
        ContaResponseDTO response = ContaResponseDTO.fromEntity(contaEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/transferencias")
    public ResponseEntity<String> transferirSaldo(@RequestBody @Valid TransferenciaDTO transferenciaDTO){

        transferenciaService.transferir(
                transferenciaDTO.getOrigemId(),
                transferenciaDTO.getDestinoId(),
                transferenciaDTO.getSaldo());

        return ResponseEntity.ok("Transferência realizada com sucesso.");

    }

    @GetMapping("/contas/{id}/movimentacoes")
    public ResponseEntity<List<TransferenciaResponseDTO>> listarMovimentacoes(@PathVariable Long id) {
        return ResponseEntity.ok(transferenciaService.listarMovimentacoes(id));
    }
}
