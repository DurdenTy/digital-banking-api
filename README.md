# Digital Banking API

API REST simplificada para um banco digital, desenvolvida como teste técnico Java.
O projeto permite cadastro de contas, consulta de contas, transferência de valores entre contas, consulta de movimentações financeiras e publicação de notificações via RabbitMQ após operações concluídas com sucesso.

## Tecnologias utilizadas

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* H2 Database
* RabbitMQ
* Spring AMQP
* Bean Validation
* Maven
* JUnit 5
* Mockito
* Swagger / OpenAPI

## Funcionalidades

* Cadastro de contas
* Consulta de conta por ID
* Transferência de valores entre contas
* Consulta de movimentações financeiras
* Validação de payloads de entrada
* Tratamento global de erros
* Notificação após criação de conta
* Notificação após transferência realizada com sucesso
* Documentação via Swagger/OpenAPI
* Testes unitários e teste de concorrência

## Como rodar o projeto

### Pré-requisitos

* Java 21
* Maven
* Docker e Docker Compose opcionais, apenas para subir o RabbitMQ localmente

### Rodando somente a aplicação

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Console H2:

```text
http://localhost:8080/h2-console
```

Configuração do H2:

```text
JDBC URL: jdbc:h2:mem:bankingdb
User: sa
Password:
```

### Dados iniciais

A aplicação possui uma carga inicial de contas para facilitar os testes:

```text
Conta 1: Adilson - Saldo: R$ 1000.00
Conta 2: Maria   - Saldo: R$ 1000.00
```

### Rodando com RabbitMQ via Docker

Para subir o RabbitMQ localmente:

```bash
docker compose up -d
```

RabbitMQ Management:

```text
http://localhost:15672
```

Credenciais:

```text
guest / guest
```

### E se eu não tiver Docker?

A API ainda pode ser executada normalmente com:

```bash
mvn spring-boot:run
```

Caso o RabbitMQ não esteja disponível, as operações principais de cadastro de conta e transferência continuam funcionando. A publicação da notificação será registrada em log como falha, sem impedir a conclusão da operação bancária.

Essa decisão foi adotada para evitar que uma indisponibilidade no mecanismo de notificação impeça a execução da regra principal do sistema.

## Rodando os testes

```bash
mvn test
```

O projeto possui testes cobrindo:

* Cadastro de conta
* Consulta de conta
* Transferência com sucesso
* Saldo insuficiente
* Conta inexistente
* Origem e destino iguais
* Listagem de movimentações
* Publicação de eventos de notificação
* Teste de concorrência para validar consistência dos saldos

## Endpoints principais

### Cadastrar conta

```http
POST /v1/banco/contas
```

Payload:

```json
{
  "nome": "Adilson",
  "saldo": 1000.00
}
```

### Consultar conta

```http
GET /v1/banco/contas/{id}
```

### Realizar transferência

```http
POST /v1/banco/transferencias
```

Payload:

```json
{
  "origemId": 1,
  "destinoId": 2,
  "saldo": 50.00
}
```

### Consultar movimentações

```http
GET /v1/banco/contas/{id}/movimentacoes
```

## Decisões de design e arquitetura

### Java 21

Usei Java 21 por ser uma versão LTS moderna, bastante adotada em aplicações corporativas e cloud. O projeto também utiliza `record` em alguns pontos para representar estruturas simples de dados de forma mais objetiva.

### Spring Boot

Escolhi Spring Boot pela produtividade e pela integração com recursos importantes para o desafio, como criação de APIs REST, validação de dados, persistência com JPA, testes automatizados e mensageria com RabbitMQ.

### Arquitetura em camadas

A aplicação foi organizada em camadas, separando responsabilidades entre controller, service, repository, DTOs e configurações.

Essa escolha mantém o projeto simples, fácil de entender e adequado ao escopo do teste. Para um cenário maior, uma evolução possível seria migrar para Clean Architecture, separando melhor domínio, casos de uso e infraestrutura.

### Banco H2

Usei H2 para facilitar a execução local e a avaliação do projeto sem exigir instalação de banco externo. A aplicação já inicia com dados básicos para permitir testes rápidos pelo Swagger.

### Consistência transacional

A transferência é executada com `@Transactional`, garantindo que débito, crédito e registro da movimentação aconteçam como uma única operação. Se ocorrer erro durante o processo, a transação é revertida.

### Concorrência

Como o desafio menciona alta concorrência, usei lock pessimista com `PESSIMISTIC_WRITE` ao buscar as contas envolvidas na transferência.

Também bloqueei as contas sempre em ordem crescente de ID, reduzindo o risco de deadlock em transferências cruzadas, como conta 1 para conta 2 e conta 2 para conta 1 ao mesmo tempo.

Além disso, foi criado um teste de concorrência para validar que o saldo permanece consistente mesmo com múltiplas transferências simultâneas.

### RabbitMQ para notificações

Usei RabbitMQ porque o requisito de notificação se aproxima de um cenário real em que a operação principal não deveria ficar totalmente acoplada ao envio de notificações.

Após a criação de conta ou transferência concluída com sucesso, a aplicação publica um evento de notificação. O envio ocorre após a operação principal ser concluída, evitando notificações para ações que falharam.

Caso o RabbitMQ esteja indisponível, a falha é registrada em log, mas a operação bancária principal continua funcionando.

### Swagger/OpenAPI

A documentação foi feita em OpenAPI usando um arquivo YAML, descrevendo endpoints, payloads, status codes e respostas de erro. O Swagger UI está disponível em:

```text
http://localhost:8080/swagger-ui.html
```

### Testes

Foram criados testes unitários e de concorrência cobrindo os principais fluxos da aplicação, incluindo cadastro, consulta, transferência, saldo insuficiente, conta inexistente, publicação de eventos e consistência em transferências simultâneas.

### Possíveis evoluções

Algumas melhorias possíveis para um cenário produtivo seriam:

* PostgreSQL no lugar do H2
* autenticação e autorização
* idempotência em transferências
* padrão Transactional Outbox para maior segurança na publicação de eventos
* separação das notificações em um microserviço próprio
* evolução para Clean Architecture
