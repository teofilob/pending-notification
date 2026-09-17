# pending-notification

API para cadastrar pendências, como faturas a vencer e documentos pendentes, com uma estrutura voltada ao processamento assíncrono de notificações.

O projeto está em desenvolvimento e utiliza arquitetura hexagonal (Ports & Adapters), separando regras de domínio, casos de uso e integrações externas.

## Tecnologias

- Java 17 e Maven
- Spring Boot 3.2.5: Web, Validation, Actuator, Data JPA, AMQP e Redis
- PostgreSQL, RabbitMQ e Redis disponíveis via Docker Compose
- JUnit 5, Mockito e AssertJ para testes; JaCoCo para cobertura

## Estrutura

```text
src/main/java/br/com/teofilob/
├── Main.java
└── pendingnotification/
    ├── domain/          # Modelos e exceções de negócio
    ├── application/     # Portas de entrada/saída e casos de uso
    └── infrastructure/  # Controlador REST, adaptadores e configurações
src/main/resources/     # Configurações e perfis da aplicação
src/test/java/          # Testes automatizados
```

## Executando localmente

Requisitos: JDK 17, Maven instalado e Docker com Docker Compose. O repositório ainda não inclui Maven Wrapper.

Na raiz do projeto, inicie os serviços de infraestrutura:

```sh
docker compose up -d
```

Após os serviços estarem disponíveis, execute a aplicação:

```sh
mvn spring-boot:run
```

A API utiliza a porta `8080`. Os serviços locais são:

| Serviço | Endereço/porta | Credenciais locais |
| --- | --- | --- |
| PostgreSQL | `localhost:5432`, banco `pending_db` | `postgres` / `postgres` |
| RabbitMQ | `localhost:5672` | `guest` / `guest` |
| Painel RabbitMQ | [localhost:15672](http://localhost:15672) | `guest` / `guest` |
| Redis | `localhost:6379` | Sem autenticação configurada |

As configurações ficam em [application.yml](src/main/resources/application.yml). Os perfis `dev` e `memory` selecionam as propriedades de cache e mensageria. Para selecionar um perfil:

```sh
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

O perfil `memory` mantém a configuração de datasource herdada; ele não desativa a dependência de PostgreSQL na inicialização.

## Criando uma pendência

Envie uma requisição `POST` para `http://localhost:8080/api/v1/pendencies`, com o cabeçalho `Content-Type: application/json` e o corpo:

```json
{
  "title": "Pagamento de fatura",
  "dueDate": "2026-12-20T10:00:00",
  "email": "cliente@example.com",
  "phone": "+5511999999999"
}
```

Todos os campos são obrigatórios. O e-mail deve ser válido e `dueDate` deve seguir o formato de data/hora local do exemplo, sem fuso horário.

Quando aceita, a requisição retorna `202 Accepted`, com identificador, título, status `PENDING`, vencimento, e-mail e mensagem. Uma pendência com o mesmo e-mail e título já presente no cache retorna `409 Conflict`. Campos vazios e e-mail inválido são rejeitados com `400 Bad Request`.

O endpoint de saúde está disponível em [localhost:8080/actuator/health](http://localhost:8080/actuator/health).

## Estado atual e limitações

- O cadastro REST e os casos de uso estão implementados, mas o fluxo completo de notificação ainda não está concluído.
- Os adaptadores em memória de cache e publicação estão marcados com `@Primary` e prevalecem mesmo quando Redis e RabbitMQ são habilitados. A publicação atual apenas registra uma mensagem no log; o retorno `202` não comprova envio à fila.
- A persistência utiliza memória e perde os registros ao reiniciar a aplicação. O adaptador PostgreSQL e o consumidor RabbitMQ ainda não foram implementados, assim como o envio efetivo de e-mail/SMS.
- O cache em memória não aplica expiração (TTL), e a verificação de duplicidade ainda não é atômica para requisições concorrentes.
- O tratamento de datas malformadas ainda precisa ser ajustado para retornar `400` de forma consistente.

## Testes e cobertura

Execute os testes unitários:

```sh
mvn test
```

Para executar também a verificação dos limites de cobertura configurados no projeto:

```sh
mvn verify
```

O relatório HTML do JaCoCo é gerado em `target/coverage-reports/jacoco-ut/index.html`.
