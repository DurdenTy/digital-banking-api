package com.adilsonTI.digital_banking_api.dtos;

import com.adilsonTI.digital_banking_api.entities.ContaEntity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaDTO {

    @NotBlank(message = "campo obrigatório")
    @Size(min = 2, max = 50, message = "Campo fora do tamanho padrão")
    private String nome;
    @NotNull(message = "Saldo é obrigatório.")
    @DecimalMin(value = "0.00", message = "não é permitido valor abaixo de 0.00")
    private BigDecimal saldo;

    public ContaEntity mapToContaEntity(){
        ContaEntity contaEntity = new ContaEntity();
        contaEntity.setNome(this.nome);
        contaEntity.setSaldo(this.saldo);
        return contaEntity;
    }

}
