package com.adilsonTI.digital_banking_api.configurations.rabbitMQ;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static String EXCHANGE_NOTIFICACOES = "digital_banking.notificacoes.exchange";


    public static final String QUEUE_CONTA_CRIADA = "digital-banking.conta-criada.queue";
    public static final String QUEUE_TRANSFERENCIA_REALIZADA = "digital-banking.transferencia-realizada.queue";

    public static final String ROUTING_KEY_CONTA_CRIADA = "conta.criada";
    public static final String ROUTING_KEY_TRANSFERENCIA_REALIZADA = "transferencia.realizada";

    @Bean
    public DirectExchange notificacoesExchange(){
        return new DirectExchange(EXCHANGE_NOTIFICACOES);
    }

    @Bean
    public Queue contaCriadaQueue(){
        return new Queue(QUEUE_CONTA_CRIADA, true);
    }

    @Bean
    public Queue transferenciaRealizadaQueue() {
        return new Queue(QUEUE_TRANSFERENCIA_REALIZADA, true);
    }

    @Bean
    public Binding contaCriadaBiding(){
        return BindingBuilder
                .bind(contaCriadaQueue())
                .to(notificacoesExchange())
                .with(ROUTING_KEY_CONTA_CRIADA);
    }

    @Bean
    public Binding transferenciaRealizadaBiding(){
        return BindingBuilder
                .bind(transferenciaRealizadaQueue())
                .to(notificacoesExchange())
                .with(ROUTING_KEY_TRANSFERENCIA_REALIZADA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

}
