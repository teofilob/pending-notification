# Pending Notification Service (`pending-notification`)

Sistemas confiáveis de notificação são fundamentais para o bom funcionamento de ecossistemas corporativos. O **`pending-notification`** é uma API e worker de alta resiliência projetado para gerenciar e notificar pendências (faturas a vencer, documentos pendentes, agendamentos) utilizando **Arquitetura Hexagonal (Ports & Adapters)**, **Mensageria com RabbitMQ** e **Cache de Idempotência com Redis** no ecossistema **Spring Boot 3**.

> *"Instrui-nos de tal modo a contar os nossos dias, que alcancemos coração sábio."*
>
> — **Salmos 90:12**

---

## 🏛️ Arquitetura Hexagonal (Ports & Adapters)

O projeto adota estritamente os princípios da Arquitetura Hexagonal. O **Core de Domínio** é mantido 100% isolado de frameworks, bibliotecas de terceiros, ORM ou detalhes de infraestrutura.

```
                  ┌──────────────────────────────────────────────┐
                  │               INFRASTRUCTURE                 │
                  │                                              │
                  │   ┌──────────────────────────────────────┐   │
                  │   │                APPLICATION           │   │
                  │   │                                      │   │
                  │   │   ┌──────────────────────────────┐   │   │
                  │   │   │            DOMAIN            │   │   │
                  │   │   │                              │   │   │
  HTTP REST ─────►│───┼──►│ [Input Port]                 │   │   │
  (Controllers)   │   │   │      │                       │   │   │
                  │   │   │      ▼                       │   │   │
                  │   │   │   Entities &                 │   │   │
  RabbitMQ  ─────►│───┼──►│   Use Cases                  │   │   │
  (Consumers)     │   │   │      │                       │   │   │
                  │   │   │      ▼                       │   │   │
                  │   │   │ [Output Port]                │   │   │
                  │   │   └──────┬───────────────────────┘   │   │
                  │   └──────────┼───────────────────────────┘   │
                  │              │                               │
                  └──────────────┼───────────────────────────────┘
                                 │
           ┌─────────────────────┼─────────────────────┐
           ▼                     ▼                     ▼
   [PostgreSQL Adapter]   [RabbitMQ Publisher]   [Redis Cache Adapter]
   (Database Persistence) (Fila: notificar)      (Idempotência & Lock)
```

---

## ⚙️ Regras de Negócio e Idempotência (Redis + RabbitMQ)

Para evitar duplicidade de notificações e sobrecarga no sistema, o serviço utiliza uma estratégia baseada em **Redis** e **RabbitMQ** no caso de uso `CreatePendencyService`:

### 1. Criação de Pendência (`CreatePendencyService`)
1. A API recebe o registro de pendência.
2. É gerada uma chave de cache no Redis com o formato: `pendency:{email}:{title}`.
3. O sistema verifica se a chave já existe no Redis:
    * **Se a chave existir:** Interrompe o envio e retorna imediatamente a resposta `"Pendente de processamento"`.
    * **Se a chave NÃO existir:**
        1. Salva o registro no **Redis** com status pendente (e TTL configurado).
        2. Posta a mensagem na fila de mensageria **`notificar`** no RabbitMQ.

### 2. Consumo e Processamento (`NotificationConsumer`)
1. Um **Consumer** escuta a fila **`notificar`**.
2. Ao receber o registro:
    1. Persiste as informações do registro e notificação no banco de dados **PostgreSQL**.
    2. Atualiza o status do registro correspondente no **Redis** (ex.: atualizando o estado para `PROCESSED` ou confirmando seu agendamento).

---

## 🔄 Fluxo de Processamento detalhado

```sequenceDiagram
    autonumber
    actor Client as Cliente / API Caller
    participant API as REST Controller (Adapter)
    participant Service as CreatePendencyService (Use Case)
    participant Redis as Redis Cache (Adapter)
    participant Queue as RabbitMQ (Fila: notificar)
    participant Consumer as NotificationConsumer (Adapter)
    participant DB as PostgreSQL (Adapter)

    Client->>API: POST /api/v1/pendencies
    API->>Service: execute(CreatePendencyCommand)
    
    Service->>Redis: exists("pendency:" + email + ":" + title)
    
    alt Registro Já Existe no Redis
        Redis-->>Service: true
        Service-->>API: Status: "Pendente de processamento"
        API-->>Client: 200 OK / 409 Conflict ("Pendente de processamento")
    else Registro Não Existe
        Redis-->>Service: false
        Service->>Redis: save("pendency:" + email + ":" + title, status="PENDING")
        Service->>Queue: publishToQueue("notificar", pendencyData)
        Service-->>API: PendencyCreatedResponse
        API-->>Client: 202 Accepted / 201 Created
        
        Note over Queue, Consumer: Processamento Assíncrono
        Queue->>Consumer: consume("notificar")
        Consumer->>DB: save(PendencyEntity)
        Consumer->>Redis: update("pendency:" + email + ":" + title, status="PROCESSED")
    end
```

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia | Finalidade | 
| ----- | ----- | ----- | 
| **Linguagem** | Java 17 | Recursos modernos (Records, Pattern Matching, Sealed Classes) | 
| **Framework** | Spring Boot 3.2+ | Injeção de dependências e gerenciamento da infraestrutura | 
| **Banco de Dados** | PostgreSQL 15 | Persistência relacional de pendências e histórico de envios | 
| **Mensageria** | RabbitMQ | Fila `notificar` com estratégias de Retry e DLQ | 
| **Cache & Lock** | Redis | Validação de duplicidade (`email+title`) e chave de idempotência | 
| **Migração** | Flyway | Controle de versão de esquema do banco de dados | 
| **Testes** | JUnit 5, Mockito, Testcontainers | Testes unitários do core e integração com infra real | 
| **Conteinerização** | Docker & Docker Compose | Padronização de ambiente local e produção | 

---

## 📂 Estrutura de Pastas (Ports & Adapters)

```
pending-notification/
├── docker/
│   ├── postgres/
│   └── rabbitmq/
├── src/
│   ├── main/
│   │   ├── java/com/dev/pendingnotification/
│   │   │   ├── domain/                         # CORE DE DOMÍNIO (Zero Spring Dependencies)
│   │   │   │   ├── model/                      # Entidades e Objetos de Valor
│   │   │   │   │   ├── Pendency.java
│   │   │   │   │   ├── NotificationStatus.java
│   │   │   │   │   └── Recipient.java
│   │   │   │   └── exception/                  # Exceções de Domínio
│   │   │   │       └── PendencyAlreadyProcessingException.java
│   │   │   │
│   │   │   ├── application/                    # CASOS DE USO E PORTAS
│   │   │   │   ├── ports/
│   │   │   │   │   ├── in/                     # Input Ports (Casos de Uso)
│   │   │   │   │   │   ├── CreatePendencyUseCase.java
│   │   │   │   │   │   └── ProcessNotificationUseCase.java
│   │   │   │   │   └── out/                    # Output Ports (Interfaces)
│   │   │   │   │       ├── PendencyRepositoryPort.java
│   │   │   │   │       ├── PendencyCachePort.java
│   │   │   │   │       └── NotificationPublisherPort.java
│   │   │   │   └── usecase/                    # Implementação dos Serviços
│   │   │   │       ├── CreatePendencyService.java
│   │   │   │       └── ProcessNotificationService.java
│   │   │   │
│   │   │   └── infrastructure/                 # ADAPTERS & CONFIGURAÇÕES
│   │   │       ├── adapters/
│   │   │       │   ├── inbound/                # Driving Adapters
│   │   │       │   │   ├── rest/               # Endpoints REST
│   │   │       │   │   │   ├── PendencyController.java
│   │   │       │   │   │   └── dto/
│   │   │       │   │   └── messaging/          # Consumidores RabbitMQ
│   │   │       │   │       └── NotificationConsumer.java (Fila: notificar)
│   │   │       │   │
│   │   │       │   └── outbound/               # Driven Adapters
│   │   │       │       ├── persistence/        # Adapter PostgreSQL JPA
│   │   │       │       │   ├── PendencyDatabaseAdapter.java
│   │   │       │       │   └── repository/
│   │   │       │       ├── cache/              # Adapter Redis
│   │   │       │       │   └── RedisPendencyCacheAdapter.java
│   │   │       │       └── messaging/          # Adapter RabbitMQ Producer
│   │   │       │           └── RabbitMQPublisherAdapter.java
│   │   │       │
│   │   │       └── config/                     # Configuradores Spring (Beans, Redis, Rabbit)
│   │   │           ├── BeanConfiguration.java
│   │   │           ├── RedisConfig.java
│   │   │           └── RabbitMQConfig.java
│   │   │
│   │   └── resources/
│   │       ├── db/migration/                   # Scripts Flyway
│   │       └── application.yml
│   │
│   └── test/                                   # Testes Unitários e Integração
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java 17 JDK** ou superior.
* **Docker** e **Docker Compose**.

### 1. Subir Contêineres (PostgreSQL, Redis e RabbitMQ)

```bash
docker-compose up -d
```

| Serviço | Host / Porta | Credenciais / Info |
| ----- | ----- | ----- |
| **PostgreSQL** | `localhost:5432` | `user: postgres` \| `pass: postgres` \| `db: pending_db` |
| **Redis** | `localhost:6379` | *Default standalone* |
| **RabbitMQ Management** | `http://localhost:15672` | `user: guest` \| `pass: guest` |

### 2. Executar a Aplicação

```bash
./mvnw clean spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📄 Licença

Este projeto está licenciado sob a Licença MIT.