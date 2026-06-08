# FluvPay SDK para Java

SDK oficial da FluvPay para Java. Cobranças PIX, saques, transferências internas
e verificação de webhooks, com tipagem forte e zero dependência de runtime
pesada (usa o `java.net.http.HttpClient` nativo do JDK e Jackson para JSON).

## Instalação

Requisitos: Java 11 ou superior e Maven 3.6+.

### Hoje: via JitPack (a partir do código no GitHub)

Enquanto o SDK não está no Maven Central, o JitPack compila direto da tag do
GitHub. Adicione o repositório do JitPack e a dependência. Note que a coordenada
do JitPack deriva do repositório (`groupId` `com.github.fluvpay`, `artifactId`
`fluvpay-java`), diferente da coordenada final do Maven Central.

Maven (no seu `pom.xml`):

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

A tag `v1.0.0` precisa existir no repositório para o JitPack compilar. Antes de a
tag ser publicada, dá para apontar para o último commit da branch principal
trocando a versão por `main-SNAPSHOT` (JitPack compila o HEAD da branch sob
demanda).

### Instalar a partir do código-fonte (sem registry, funciona agora)

Clonar o repositório e instalar no seu repositório Maven local (`~/.m2`):

```bash
git clone https://github.com/fluvpay/fluvpay-java.git
cd fluvpay-java
mvn -q install -DskipTests
```

Isso publica o artefato localmente sob a coordenada `com.fluvpay:fluvpay:1.0.0`,
que então pode ser declarada normalmente em qualquer projeto da mesma máquina:

```xml
<dependency>
  <groupId>com.fluvpay</groupId>
  <artifactId>fluvpay</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Em breve: Maven Central

Quando publicado no Maven Central, a coordenada final será `com.fluvpay:fluvpay`
(sem o repositório extra do JitPack):

```xml
<!-- em breve, quando publicado no Maven Central -->
<dependency>
  <groupId>com.fluvpay</groupId>
  <artifactId>fluvpay</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Configuração

A API Key define o modo de operação pelo prefixo: `fluv_live_` para produção e
`fluv_test_` para o sandbox. Você só precisa passar a chave; o SDK cuida do
resto.

```java
import com.fluvpay.FluvPay;

FluvPay fluvpay = FluvPay.builder()
    .apiKey(System.getenv("FLUVPAY_API_KEY"))
    // .baseUrl("https://api.fluvpay.com/api/v1") // padrão
    // .timeout(java.time.Duration.ofSeconds(30)) // padrão
    // .maxRetries(2)                              // padrão (0 desliga)
    .build();

System.out.println(fluvpay.isTestKey()); // true se a chave for fluv_test_
```

## Criar uma cobrança PIX

A criação de cobrança aceita apenas os campos do contrato. Não envie `currency`
nem `method`: a moeda e o método (PIX) são implícitos, e a API rejeita campos
extras com erro de validação.

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

A `Idempotency-Key` é gerada automaticamente (UUIDv4) se você não informar uma.
Para controlar a chave (por exemplo, reusar entre tentativas do seu lado), passe
no segundo argumento:

```java
Charge charge = fluvpay.charges().create(
    new ChargeCreateParams().amountCents(2500),
    "pedido-1042-tentativa-1");
```

## Recuperar e listar

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

Estas operações são live-only: chaves `fluv_test_` recebem 403.

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

## Extrato (transactions)

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

Disponível apenas com chave `fluv_test_`.

```java
import com.fluvpay.models.SandboxScenarios;
import com.fluvpay.models.SandboxReset;

SandboxScenarios scenarios = fluvpay.sandbox().scenarios();
SandboxReset reset = fluvpay.sandbox().reset();
```

## Verificação de webhooks

A FluvPay assina cada entrega. Verifique a assinatura usando o corpo CRU da
requisição (nunca re-serialize o JSON, pois isso muda os bytes e invalida a
assinatura). O cálculo é `HMAC_SHA256(secret, timestamp + "." + rawBody)` em
hexadecimal, e o header `X-FluvPay-Signature` vem no formato `v1=<hex>`.

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

Eventos disponíveis: `charge.created`, `charge.paid`, `charge.expired`,
`charge.cancelled`, `charge.refunded`, `payout.created`, `payout.completed` e
`payout.failed`.

## Tratamento de erros

Cada falha vira uma exceção tipada. Todas herdam de `FluvPayError` e carregam
`code`, `message`, `details`, `traceId` e `statusCode`.

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

Mapeamento: 400/422 para `FluvPayValidationError`, 401 para
`FluvPayAuthenticationError`, 403 para `FluvPayPermissionError`, 404 para
`FluvPayNotFoundError`, 409 para `FluvPayConflictError` (inclui
`IDEMPOTENCY_CONFLICT`), 429 para `FluvPayRateLimitError` (lê `Retry-After`),
5xx para `FluvPayServerError`, e falha de rede ou tempo limite para
`FluvPayConnectionError`.

## Retentativas

O SDK retenta automaticamente (padrão 2 tentativas, backoff exponencial com
jitter) apenas em situações seguras: requisições GET e POSTs que carregam
`Idempotency-Key`, nos casos de 429 e 5xx ou falha de conexão. O header
`Retry-After` é respeitado. Para desligar, use `maxRetries(0)` no builder.

## Desenvolvimento

```bash
mvn -q test   # unit + webhook (sem rede)
```

Com Docker, sem instalar a toolchain localmente:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-17 mvn -q test
```

O smoke no sandbox roda somente se a variável `FLUVPAY_TEST_KEY` (prefixo
`fluv_test_`) estiver presente; caso contrário, é ignorado.

## Licença

MIT.
