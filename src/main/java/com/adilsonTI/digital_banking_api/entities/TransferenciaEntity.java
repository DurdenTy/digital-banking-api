package com.adilsonTI.digital_banking_api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class TransferenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "conta_origem_id", nullable = false)
    private Long contaOrigemId;
    @Column(name = "conta_destino_id", nullable = false)
    private Long contaDestinoId;
    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

}
