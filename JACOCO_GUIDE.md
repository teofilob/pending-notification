# JaCoCo Code Coverage Guide

## 📊 Visão Geral

Este projeto está configurado com **JaCoCo** (Java Code Coverage) para medir e validar a cobertura de código dos testes.

---

## 🛠️ Configuração Atual

### Metas de Cobertura

| Camada | Tipo | Objetivo | Limite Mínimo |
|--------|------|----------|--------------|
| **Geral** | LINE | Linhas cobertas | 70% |
| **Geral** | BRANCH | Branches cobertos | 60% |
| **Domain** | LINE | Lógica de negócio | 85% |
| **Application (Use Cases)** | LINE | Casos de uso | 80% |

---

## 🚀 Como Usar

### 1. Executar Testes com Cobertura

```bash
# Compilar, rodar testes e gerar relatório
./mvnw clean verify

# Ou apenas teste sem verificação de limites
./mvnw clean test
```

### 2. Visualizar Relatório HTML

Após executar `mvn verify`, o relatório será gerado em:

```
target/coverage-reports/jacoco-ut/index.html
```

Abra este arquivo no navegador para ver:
- 📈 Cobertura por pacote
- 📊 Cobertura por classe
- 🎯 Linhas cobertas/não cobertas
- 🔀 Branches cobertos

### 3. Verificação de Limites

Durante o `mvn verify`, o JaCoCo automaticamente:
- ✅ Verifica se as metas de cobertura foram atingidas
- ❌ Falha a build se os limites não forem atendidos
- 📝 Exibe relatório detalhado de falhas

**Exemplo de falha esperada:**
```
Rule violated for package br.com.teofilob.pendingnotification.domain:
Expected line coverage ratio at least 0.85, but was 0.75
```

---

## 📁 Estrutura de Relatórios

```
target/coverage-reports/
├── jacoco-ut.exec                    # Arquivo de dados binário do JaCoCo
└── jacoco-ut/
    ├── index.html                    # Relatório interativo
    ├── status.svg                    # Badge de cobertura
    └── [pacotes e classes].html     # Relatório por classe
```

---

## 🎯 Melhores Práticas

### 1. Cobertura de Domínio (85%)
- Testes unitários de entidades (`Pendency.java`, `Recipient.java`)
- Testes de valores (`NotificationStatus.java`)
- Testes de exceções de domínio

**Exemplo:**
```java
@Test
void shouldCreatePendencyWithValidData() {
    Pendency pendency = Pendency.create("Invoice", LocalDateTime.now().plusDays(5), "email@test.com", "1199999999");
    assertThat(pendency).isNotNull();
    assertThat(pendency.status()).isEqualTo(NotificationStatus.PENDING);
}
```

### 2. Cobertura de Use Cases (80%)
- Testes de `CreatePendencyService` com sucesso e conflito
- Testes de `ProcessNotificationService`
- Testes com mocks das portas/adapters

**Exemplo:**
```java
@Test
void shouldPublishToQueueWhenFirstTimeCreating() {
    // Arrange
    when(cachePort.exists(anyString())).thenReturn(false);
    
    // Act
    PendencyCreatedResponse response = service.execute(command);
    
    // Assert
    verify(publisherPort).publishPendencyCreated(any());
    assertThat(response.id()).isNotNull();
}
```

### 3. Cobertura REST (70%)
- Testes de controller com `@MockMvc`
- Testes de DTOs e validações
- Testes de códigos HTTP (200, 202, 409, etc)

### 4. O que NÃO Precisa de Teste?
- Getters/Setters simplistas (se refletir apenas em estado)
- Anotações do Spring (`@Entity`, `@Service`, etc)
- Configurações de infraestrutura (beans, etc) - testadas por integração
- Código gerado automaticamente

---

## 📊 Interpretando o Relatório

### Verde 🟢
- **Cobertura alta:** Linhas/branches foram executadas pelos testes
- **Ideal:** > 80% de cobertura

### Amarelo 🟡
- **Cobertura média:** Some linhas não foram testadas
- **Aceitável:** 60-80% de cobertura

### Vermelho 🔴
- **Cobertura baixa:** Muitas linhas sem teste
- **Problema:** < 60% de cobertura

---

## 🔧 Customizando as Metas

Para alterar os limites de cobertura, edite `pom.xml` na seção do JaCoCo:

```xml
<minimum>0.75</minimum>  <!-- Altere este valor (0.70 = 70%) -->
```

---

## 💡 Dicas de Otimização

### 1. Aumentar Cobertura Rapidamente
```bash
# Rodar apenas testes rápidos (sem Testcontainers)
./mvnw test -Dgroups="!integration"
```

### 2. Gerar Relatório Sem Falhar
```bash
# Gera relatório mas não falha build
./mvnw clean test jacoco:report
```

### 3. Debugar Linhas Não Cobertas
1. Abra `target/coverage-reports/jacoco-ut/index.html`
2. Clique na classe
3. Procure por linhas vermelhas (não cobertas)
4. Crie teste para cobri-las

---

## 🔄 Integração com CI/CD

Para usar em pipelines (GitHub Actions, GitLab CI, etc):

```yaml
- name: Run tests with coverage
  run: ./mvnw clean verify

- name: Upload coverage to SonarQube (opcional)
  run: ./mvnw sonar:sonar -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

---

## 📚 Referências

- [JaCoCo Official](https://www.jacoco.org/jacoco/)
- [Maven JaCoCo Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Code Coverage Best Practices](https://www.baeldung.com/jacoco)

---

## ❓ FAQ

**P: Manual do código de cobertura é requisito?**
R: Não é obrigatório, mas ajuda a garantir qualidade. Manter acima de 70% é uma boa prática.

**P: Posso ignorar certas classes?**
R: Sim, use a tag `@GeneratedCode` ou configure exclusões no `pom.xml`:
```xml
<excludes>
    <exclude>br/com/teofilob/pendingnotification/infrastructure/config/*</exclude>
</excludes>
```

**P: Como integrar com IDE?**
R: IntelliJ IDEA, Eclipse e VS Code têm plugins JaCoCo nativos. Configure para usar `target/coverage-reports/jacoco-ut.exec`.


