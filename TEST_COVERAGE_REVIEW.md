# Revisão da cobertura unitária — 17/09/2026

## Resultado

Comparação com os relatórios JaCoCo e Surefire encontrados em `target` antes das alterações:

| Métrica | Antes | Depois |
| --- | ---: | ---: |
| Testes executados | 69 | 101 |
| Falhas / erros / ignorados | 0 / 0 / 0 | 0 / 0 / 0 |
| Linhas cobertas | 86/200 (43%) | 197/200 (98,5%) |
| Ramificações cobertas | 6/8 (75%) | 8/8 (100%) |

O relatório final foi gerado com `jacoco.append=false`, sem acumular cobertura de execuções anteriores. As fontes de produção, dependências e regras de cobertura não foram alteradas.

## Análise e testes adicionados

O projeto separa domínio, casos de uso e adaptadores por portas. Os testes anteriores já cobriam as linhas do domínio e da criação de pendências, mas não exercitavam o processamento e os adaptadores externos.

- `ProcessNotificationServiceTest`: persistência antes da atualização do cache; chave exata de idempotência; falhas de desserialização, persistência e cache; preservação da causa; dependências obrigatórias. O ObjectMapper é simulado para isolar a orquestração; estes testes não comprovam a desserialização real de Pendency.
- `RedisPendencyCacheAdapterTest`: existência com true, false e null; TTL de gravação; marcação PROCESSED com cinco minutos; chave ausente; leitura, remoção e propagação de falha. Usa mocks de StringRedisTemplate e ValueOperations.
- `RabbitMQPublisherAdapterTest`: serialização real com Jackson nos cenários de sucesso; dados dos eventos; exchange, routing key, persistência e expiração; publicação genérica; falhas de serialização e envio. RabbitTemplate é simulado.
- `RabbitMQConfigTest` e `RedisConfigTest`: filas, exchanges, bindings, durabilidade, conexão configurada e serializadores, sem abrir conexões.
- `InMemoryPendencyRepositoryTest`: contrato de retorno da entidade salva e repetição da gravação.
- Testes existentes ampliados: resposta HTTP 400 do tratador de validação e registro da publicação genérica em memória, com remoção do appender após o teste.

Processamento, Redis, RabbitMQ, configurações, persistência em memória e controlador passaram a ter 100% das linhas cobertas. A cobertura de ramificações representa apenas oito alternativas instrumentadas; não significa que todos os comportamentos possíveis estejam testados.

## Validação e limitações

Executado no ambiente local com Maven 3.9.16 e Java 25:

```powershell
& 'D:\tools\maven\maven-3.9.16\bin\mvn.cmd' -o `
  '-Dmaven.repo.local=C:\Users\User\.m2\repository' `
  '-Dnet.bytebuddy.experimental=true' '-Djacoco.append=false' -e verify
```

Todos os 101 testes passaram. O `verify` chegou à verificação JaCoCo e falhou exclusivamente na regra de linhas do pacote `br.com.teofilob`: a classe de inicialização `Main` tem três linhas sem cobertura. A regra denominada geral no POM usa `PACKAGE`, portanto exige 70% em cada pacote, mesmo com 98,5% no conjunto. Não foram reduzidos limites nem excluídas classes para fazer a verificação passar.

O POM declara Java 17. O Java 25 instalado produziu avisos de instrumentação (`Unsupported class file major version 69`) com JaCoCo 0.8.10 em classes do JDK e mocks gerados. Os relatórios das 16 classes da aplicação, compiladas com release 17, foram gerados. A execução local precisou da propriedade experimental do Byte Buddy; a validação com o JDK 17 declarado no projeto ainda não foi realizada.

Arquivos de evidência:

- `target/coverage-reports/jacoco-ut/index.html`
- `target/coverage-reports/jacoco-ut/jacoco.csv`
- `target/coverage-reports/jacoco-ut/jacoco.xml`
- `target/surefire-reports/`
- `target/coverage-verify-run.log`
