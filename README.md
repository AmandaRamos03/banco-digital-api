# Banco Digital API

![CI](https://github.com/AmandaRamos03/banco-digital-api/actions/workflows/ci.yml/badge.svg)

API REST de um banco digital, feita em Java com Spring Boot. Nela dá para abrir uma conta, fazer login, depositar, sacar, consultar o extrato e transferir dinheiro entre contas no estilo Pix, usando CPF ou e-mail como chave.

## Por que um banco?

O Pix faz parte do meu dia a dia, e eu sempre tive curiosidade sobre o que acontece por trás de uma transferência: como o sistema garante que o dinheiro sai de uma conta e chega na outra sem se perder no meio do caminho, ou que ninguém consegue gastar mais do que tem.

Escolhi construir um banco porque é um domínio em que os detalhes importam. Um centavo arredondado errado ou uma transferência pela metade são problemas reais. Isso me obrigou a pensar com cuidado em regras de negócio, transações, segurança e testes, em vez de fazer só um CRUD.

## O que a API faz

- Cadastro de cliente, com abertura automática da conta
- Login com token JWT
- Consulta de saldo
- Depósito e saque
- Transferência Pix por CPF ou e-mail
- Extrato paginado, do mais recente para o mais antigo

### Regras que eu quis garantir

- O saldo nunca fica negativo
- Não dá para fazer Pix para a própria conta
- Valores precisam ser maiores que zero e ter no máximo 2 casas decimais
- A transferência é atômica: ou debita e credita, ou não faz nada
- O CPF é validado pelos dígitos verificadores (CPFs como `000.000.000-00` são recusados)
- O CPF pode ser digitado com ou sem pontuação, e o e-mail não diferencia maiúsculas de minúsculas

## Tecnologias

- Java 21
- Spring Boot 4 (Web, Data JPA, Validation, Security)
- PostgreSQL
- JWT (biblioteca jjwt)
- JUnit 5 e Mockito
- Swagger / OpenAPI (springdoc)
- Docker Compose
- GitHub Actions

## Decisões que tomei, e por quê

- **`BigDecimal` para dinheiro.** Com `double`, `0.1 + 0.2` dá `0.30000000000000004`. Em um banco, isso não pode acontecer.
- **`@Transactional` na transferência.** Se qualquer passo do Pix falhar, o banco desfaz tudo. Não existe débito sem crédito.
- **As regras moram perto dos dados.** É a própria `Conta` que impede o saldo de ficar negativo no método `debitar`, então não tem como esquecer essa verificação em algum lugar do código.
- **Senha com BCrypt.** A senha nunca é salva em texto puro, nem consegue ser recuperada a partir do banco.
- **JWT e API stateless.** O servidor não guarda sessão: cada requisição traz o próprio token, que vale 2 horas.
- **DTOs com `record`.** A API nunca devolve a entidade direto. Assim, por exemplo, a senha do cliente não tem como vazar numa resposta.
- **Erros padronizados.** Um `@RestControllerAdvice` transforma cada tipo de erro no código HTTP certo (400, 401, 404, 409, 422), sempre com uma mensagem em português e no mesmo formato.

## O que eu aprendi

- A diferença entre validar o **formato** de um dado (`@Positive`, `@Email` no DTO, que gera 400) e validar uma **regra de negócio** (saldo insuficiente, Pix para si mesma, que gera 422).
- Como funciona o fluxo de autenticação com JWT de ponta a ponta: login, geração do token, e o filtro que valida o token a cada requisição.
- Escrever o teste antes do código. Na validação do CPF, primeiro escrevi os casos (CPF válido, dígito errado, todos os dígitos iguais) e vi os testes falharem, depois implementei o cálculo até tudo passar.
- Resolver problema de ambiente: o PostgreSQL do Docker não conectava porque eu já tinha outro PostgreSQL instalado usando a porta 5432. Por isso o banco do projeto usa a porta `15432`.

## Como rodar

Pré-requisitos: **Java 21** e **Docker**.

1. Suba o banco de dados:

```bash
docker compose up -d
```

2. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, pelo PowerShell, use `.\mvnw.cmd spring-boot:run`.

Quando a aplicação terminar de subir, o console mostra os links da API e do Swagger.

## Testando pelo Swagger

Com a aplicação rodando, acesse **http://localhost:8080/swagger-ui.html**

1. Crie uma conta em `POST /auth/cadastro`
2. Faça login em `POST /auth/login` e copie o `token`
3. Clique em **Authorize** e cole o token
4. Agora as rotas de conta e Pix estão liberadas

## Endpoints

| Método | Rota | Precisa de token | Descrição |
|---|---|---|---|
| POST | `/auth/cadastro` | não | Cadastra o cliente e abre a conta |
| POST | `/auth/login` | não | Retorna o token JWT |
| GET | `/contas/minha` | sim | Dados e saldo da conta |
| POST | `/contas/deposito` | sim | Deposita um valor |
| POST | `/contas/saque` | sim | Saca um valor |
| POST | `/pix/transferencias` | sim | Transfere para uma chave Pix (CPF ou e-mail) |
| GET | `/contas/extrato?page=0&size=20` | sim | Extrato paginado |

### Exemplo com curl

```bash
# cadastro
curl -X POST http://localhost:8080/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","cpf":"529.982.247-25","email":"ana@email.com","senha":"senha123"}'

# login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@email.com","senha":"senha123"}'

# depósito (troque TOKEN pelo token do login)
curl -X POST http://localhost:8080/contas/deposito \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"valor":150.00}'
```

Quando algo dá errado, a resposta sempre segue o mesmo formato:

```json
{
  "status": 422,
  "mensagem": "Saldo insuficiente",
  "timestamp": "2026-11-18T22:37:44.70"
}
```

## Testes

```bash
./mvnw test
```

São 45 testes unitários cobrindo as regras de negócio (cadastro, depósito, saque e Pix), a validação de CPF, a geração e validação do token JWT e o tratamento de erros. Eles usam Mockito no lugar do banco, então rodam em segundos e sem Docker. O GitHub Actions roda todos eles a cada push.

## Estrutura

```
src/main/java/com/amanda/bancodigital
├── config        segurança, Swagger e mensagem de inicialização
├── controller    endpoints
├── dto           objetos de entrada e saída
├── entity        Cliente, Conta e Transacao
├── exception     exceções e tratamento global de erros
├── repository    acesso ao banco
├── security      JWT
├── service       regras de negócio
└── util          validação de CPF, e-mail e valores
```

## O que eu ainda quero melhorar

- **Concorrência:** hoje, duas transferências saindo da mesma conta exatamente ao mesmo tempo poderiam ler o mesmo saldo. Quero resolver isso com lock no banco.
- **Testes de integração** com Testcontainers, rodando contra um PostgreSQL de verdade.
- **Migrations com Flyway**, em vez de deixar o Hibernate criar as tabelas.
- **Deploy**, para dar para testar a API sem precisar rodar localmente.
