package com.adilsonTI.digital_banking_api.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferenciaDTO {

    @NotNull
    private Long origemId;
    @NotNull
    private Long destinoId;
    @NotNull(message = "Valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "Valor da transferência deve ser maior que zero.")
    private BigDecimal saldo;

}
