    package com.adilsonTI.digital_banking_api.service;

    import com.adilsonTI.digital_banking_api.dtos.ContaDTO;
    import com.adilsonTI.digital_banking_api.entities.ContaEntity;
    import com.adilsonTI.digital_banking_api.events.ContaCriadaEvent;
    import com.adilsonTI.digital_banking_api.persistence.ContaRepository;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.context.ApplicationEventPublisher;
    import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class ContaService {

        private final ContaRepository contaRepository;
        private final ApplicationEventPublisher eventPublisher;

        public ContaEntity consultarConta(Long id) {

            return contaRepository.findById(id).orElseThrow(
                    () -> new IllegalArgumentException("Conta de destino não encontrada."));

        }

        public ContaEntity cadastrarConta(ContaDTO contaDTO) {

            ContaEntity contaEntity = contaDTO.mapToContaEntity();
            contaRepository.save(contaEntity);

            eventPublisher.publishEvent(new ContaCriadaEvent(contaEntity));

            log.info("Conta criada com sucesso. Conta ID: {}", contaEntity.getId());
            return contaEntity;

        }
    }
