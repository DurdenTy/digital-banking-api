package com.adilsonTI.digital_banking_api.events;

import com.adilsonTI.digital_banking_api.entities.TransferenciaEntity;

public record TransferenciaRealizadaEvent(TransferenciaEntity transferencia) {
}