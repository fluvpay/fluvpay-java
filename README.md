# FluvPay SDK para Java

SDK oficial da FluvPay para Java. Oferece acesso tipado a cobranças PIX, saques, transferências internas, extrato e verificação de webhooks. A biblioteca usa o `java.net.http.HttpClient` nativo do JDK e Jackson para serialização JSON, sem dependências de runtime pesadas. A interface é projetada para integrações humanas e para agentes de IA que consomem a documentação para integrar.

## Instalação

Requisitos: Java 11 ou superior e Maven 3.6 ou superior.

### Maven Central

Maven (`pom.xml`):

```xml
<dependency>
  <groupId>com.fluvpay</groupId>
  <artifactId>fluvpay</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.fluvpay:fluvpay:1.0.0'
```

### Via JitPack

O JitPack compila o SDK diretamente a partir de uma tag do GitHub. A coordenada do JitPack deriva do repositório (`groupId` `com.github.fluvpay`, `artifactId` `fluvpay-java`) e difere da coordenada definitiva do Maven Central.

Maven (`pom.xml`):

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.fluvpay</groupId>
    <artifactId>fluvpay-java</artifactId>
    <version>v1.0.0</version>
  </dependency>
</dependencies>
```

Gradle (Groovy DSL, `build.gradle`):

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.fluvpay:fluvpay-java:v1.0.0'
}
```

Gradle (Kotlin DSL, `build.gradle.kts`):

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.fluvpay:fluvpay-java:v1.0.0")
}
```

A tag `v1.0.0` precisa existir no repositório para o JitPack compilar. Antes da publicação da tag, a versão `main-SNAPSHOT` aponta para o HEAD da branch principal, que o JitPack compila sob demanda.

### A partir do código-fonte

A instalação a partir do código-fonte publica o artefato no repositório Maven local (`~/.m2`), sem depender de registry:

```bash
git clone https://github.com/fluvpay/fluvpay-java.git
cd fluvpay-java
mvn -q install -DskipTests
```

O artefato local fica disponível sob a coordenada `com.fluvpay:fluvpay:1.0.0` e pode ser declarado em qualquer projeto da mesma máquina:

```xml
<dependency>
  <groupId>com.fluvpay</groupId>
  <artifactId>fluvpay</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Inicio rápido

O cliente é construído por um builder. A API Key é o único parâmetro obrigatório. Os demais parâmetros têm valores padrão.

```java
import com.fluvpay.FluvPay;

FluvPay fluvpay = FluvPay.builder()
    .apiKey(System.getenv("FLUVPAY_API_KEY"))
    // .baseUrl("https://api.fluvpay.com/api/v1") // padrão
    // .timeout(java.time.Duration.ofSeconds(30)) // padrão
    // .maxRetries(2)                              // padrão (0 desliga)
    .build();

System.out.println(fluvpay.isTestKey()); // true quando a chave usa o prefixo fluv_test_
```

## Autenticação

A autenticação usa a API Key enviada pelo SDK no header de autorização. O ambiente é determinado pelo prefixo da chave: `fluv_live_` seleciona produção e `fluv_test_` seleciona o sandbox. O método `isTestKey()` reporta o ambiente em uso.

## Cobranças PIX

A criação de cobrança aceita apenas os campos do contrato. Os campos `currency` e `method` não devem ser enviados: a moeda e o método (PIX) são implícitos, e campos extras resultam em erro de validação.

```java
import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargeCreateParams;
import com.fluvpay.models.Customer;
import java.util.Map;

Charge charge = fluvpay.charges().create(new ChargeCreateParams()
    .amountCents(2500) // R$ 25,00 (mín 100, máx 100000)
    .description("Pedido #1042")
    .customer(new Customer().setName("Cliente Exemplo").setEmail("cliente@exemplo.com"))
    .passFeeToPayer(true)
    .metadata(Map.of("pedido_id", "1042")));

System.out.println(charge.getId());
System.out.println(charge.getStatus());        // pending | paid | expired | cancelled | refunded
System.out.println(charge.getPixCopyPaste());  // código copia-e-cola
System.out.println(charge.getPixQrCode());     // imagem do QR em base64
```

A `Idempotency-Key` é gerada automaticamente (UUIDv4) quando não informada. Para definir a chave de forma explícita, passe o valor no segundo argumento:

```java
Charge charge = fluvpay.charges().create(
    new ChargeCreateParams().amountCents(2500),
    "pedido-1042-tentativa-1");
```

### Recuperar e listar cobranças

A listagem de cobranças usa paginação por `page` e `per_page`.

```java
import com.fluvpay.FluvPay;
import com.fluvpay.models.Charge;
import com.fluvpay.models.ChargesPage;
import java.util.Map;

Charge found = fluvpay.charges().retrieve("chg_...");

Map<String, Object> params = FluvPay.query();
params.put("status", "paid");
params.put("page", 1);
params.put("per_page", 20);
params.put("sort", "-created_at");

ChargesPage page = fluvpay.charges().list(params);

System.out.println(page.getData());     // List<ChargeListItem>
System.out.println(page.getHasNext());  // paginação por page/per_page
```

## Saques e transferências internas

Saques e transferências internas são operações live-only. Chaves `fluv_test_` recebem 403. A listagem de saques usa paginação por `limit` e `offset`.

```java
import com.fluvpay.FluvPay;
import com.fluvpay.models.Withdrawal;
import com.fluvpay.models.WithdrawalCreateParams;
import com.fluvpay.models.WithdrawalsPage;
import com.fluvpay.models.InternalTransfer;
import com.fluvpay.models.InternalTransferCreateParams;
import java.util.Map;

Withdrawal withdrawal = fluvpay.withdrawals().create(new WithdrawalCreateParams()
    .amountCents(5000)
    .pixKey("chave@exemplo.com")
    .pixKeyType("email")); // cpf | cnpj | email | phone | evp

Map<String, Object> wParams = FluvPay.query();
wParams.put("limit", 20);
wParams.put("offset", 0);
WithdrawalsPage wPage = fluvpay.withdrawals().list(wParams);
System.out.println(wPage.getTotal()); // paginação por limit/offset

InternalTransfer transfer = fluvpay.internalTransfers().create(new InternalTransferCreateParams()
    .amountCents(1000)
    .recipientEmail("destino@exemplo.com")); // ou recipientMerchantId
```

## Extrato

O extrato (transactions) usa paginação por `page` e `per_page`.

```java
import com.fluvpay.FluvPay;
import com.fluvpay.models.Transaction;
import com.fluvpay.models.TransactionsPage;
import java.util.Map;

Map<String, Object> params = FluvPay.query();
params.put("page", 1);
params.put("per_page", 50);

TransactionsPage txPage = fluvpay.transactions().list(params);
Transaction tx = fluvpay.transactions().retrieve("tx_...");
```

## Sandbox

Os recursos de sandbox estão disponíveis apenas com chave `fluv_test_`.

```java
import com.fluvpay.models.SandboxScenarios;
import com.fluvpay.models.SandboxReset;

SandboxScenarios scenarios = fluvpay.sandbox().scenarios();
SandboxReset reset = fluvpay.sandbox().reset();
```

## Webhooks

A FluvPay assina cada entrega de webhook. A verificação usa o corpo cru da requisição. Reserializar o JSON altera os bytes e invalida a assinatura. A assinatura é calculada como `HMAC_SHA256(secret, timestamp + "." + rawBody)` em hexadecimal, e o header `X-FluvPay-Signature` segue o formato `v1=<hex>`.

```java
import com.fluvpay.FluvPay;
import com.fluvpay.FluvPaySignatureVerificationError;
import com.fluvpay.Webhooks;
import com.fluvpay.models.WebhookEvent;

byte[] rawBody = /* bytes crus do corpo da requisição */;
String signature = /* header X-FluvPay-Signature */;
String timestamp = /* header X-FluvPay-Timestamp */;
String secret = System.getenv("FLUVPAY_WEBHOOK_SECRET"); // whsec_...

try {
    WebhookEvent event = FluvPay.webhooks().verifySignature(Webhooks.VerifyParams.builder()
        .payload(rawBody)
        .signatureHeader(signature)
        .timestamp(timestamp)
        .secret(secret)
        .toleranceSeconds(300)
        .build());

    switch (event.getType()) {
        case "charge.paid":
            // processar pagamento confirmado
            break;
        case "payout.completed":
            // processar saque concluído
            break;
        default:
            break;
    }
} catch (FluvPaySignatureVerificationError err) {
    // responder 400: assinatura inválida ou fora da tolerância
}
```

Eventos disponíveis:

| Evento | Descrição |
|---|---|
| `charge.created` | Cobrança criada |
| `charge.paid` | Cobrança paga |
| `charge.expired` | Cobrança expirada |
| `charge.cancelled` | Cobrança cancelada |
| `charge.refunded` | Cobrança estornada |
| `payout.created` | Saque criado |
| `payout.completed` | Saque concluído |
| `payout.failed` | Saque falhou |

## Erros

Cada falha resulta em uma exceção tipada. Todas herdam de `FluvPayError` e expõem `code`, `message`, `details`, `traceId` e `statusCode`.

```java
import com.fluvpay.FluvPayValidationError;
import com.fluvpay.FluvPayRateLimitError;
import com.fluvpay.models.ChargeCreateParams;

try {
    fluvpay.charges().create(new ChargeCreateParams().amountCents(1));
} catch (FluvPayValidationError err) {
    System.err.println(err.getCode() + " " + err.getDetails());
} catch (FluvPayRateLimitError err) {
    System.err.println("aguardar " + err.getRetryAfter() + " segundos");
}
```

Mapeamento de status HTTP para exceção:

| Status | Exceção | Observação |
|---|---|---|
| 400, 422 | `FluvPayValidationError` | |
| 401 | `FluvPayAuthenticationError` | |
| 403 | `FluvPayPermissionError` | |
| 404 | `FluvPayNotFoundError` | |
| 409 | `FluvPayConflictError` | inclui `IDEMPOTENCY_CONFLICT` |
| 429 | `FluvPayRateLimitError` | lê `Retry-After` |
| 5xx | `FluvPayServerError` | |
| falha de rede ou tempo limite | `FluvPayConnectionError` | |

## Retentativas

O SDK retenta requisições automaticamente (padrão de 2 tentativas, backoff exponencial com jitter) apenas em situações seguras: requisições GET e POSTs que carregam `Idempotency-Key`, nos casos de 429, 5xx ou falha de conexão. O header `Retry-After` é respeitado. A retentativa é desativada com `maxRetries(0)` no builder.

## Desenvolvimento

A suíte de testes cobre testes unitários e de webhook, sem acesso a rede:

```bash
mvn -q test   # unit + webhook (sem rede)
```

A suíte também roda em container, sem instalação local da toolchain:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-17 mvn -q test
```

O smoke test no sandbox roda apenas quando a variável `FLUVPAY_TEST_KEY` (prefixo `fluv_test_`) está presente. Na ausência da variável, o teste é ignorado.

## Licença

MIT.
