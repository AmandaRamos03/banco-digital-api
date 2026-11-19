# Banco Digital API

![CI](https://github.com/SEU-USUARIO/banco-digital-api/actions/workflows/ci.yml/badge.svg)

API REST de um banco digital simples, feita com Java e Spring Boot. Dá para criar conta, fazer login, depositar, sacar, ver o extrato e transferir dinheiro entre contas no estilo Pix, usando CPF ou e-mail como chave.

Fiz esse projeto para praticar o que mais aparece nas vagas de desenvolvedora Java: API REST, banco de dados relacional, autenticação com JWT, testes e Docker.

## Funcionalidades

- Cadastro de cliente (a conta é aberta automaticamente, com saldo zero)
- Login com token JWT
- Consulta de saldo
- Depósito e saque
- Transferência Pix por CPF ou e-mail
- Extrato paginado, das movimentações mais recentes para as mais antigas

Regras de negócio:

- O saldo nunca fica negativo
- Não é possível fazer Pix para a própria conta
- Valores precisam ser maiores que zero e ter no máximo 2 casas decimais
- A transferência acontece numa única transação: se algo der errado, nada é debitado
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

## Como rodar

Pré-requisitos: Java 21 e Docker.

1. Suba o banco de dados:

```bash
docker compose up -d
```

O PostgreSQL sobe na porta **15432**, para não conflitar com um PostgreSQL que já esteja instalado na máquina.

2. Rode a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, no PowerShell, use `.\mvnw.cmd spring-boot:run` (no CMD, sem o `.\`).

A API fica em `http://localhost:8080`.

## Documentação (Swagger)

Com a aplicação rodando, acesse:

**http://localhost:8080/swagger-ui.html**

Para testar as rotas protegidas: faça o cadastro, depois o login, copie o `token` da resposta, clique em **Authorize** e cole o token.

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

## Exemplo de uso

```bash
# cadastro
curl -X POST http://localhost:8080/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","cpf":"123.456.789-09","email":"ana@email.com","senha":"senha123"}'

# login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@email.com","senha":"senha123"}'

# deposito (troque TOKEN pelo token do login)
curl -X POST http://localhost:8080/contas/deposito \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"valor":150.00}'

# pix
curl -X POST http://localhost:8080/pix/transferencias \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"chave":"bruno@email.com","valor":50.00}'
```

Exemplo de erro:

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

Os testes cobrem as regras de negócio dos services (cadastro, depósito, saque e Pix), a geração e validação do token JWT e o tratamento de erros. Eles não precisam de banco de dados nem de Docker, e rodam automaticamente no GitHub Actions a cada push.

## Estrutura

```
src/main/java/com/amanda/bancodigital
├── config        segurança e Swagger
├── controller    endpoints
├── dto           objetos de entrada e saída
├── entity        Cliente, Conta e Transacao
├── exception     exceções e tratamento global de erros
├── repository    acesso ao banco
├── security      JWT
├── service       regras de negócio
└── util          normalização de CPF/e-mail e validação de valores
```

## Algumas decisões

- **BigDecimal para dinheiro**, porque `double` tem problemas de precisão em contas com centavos.
- **Senha com BCrypt**, nunca salva em texto puro.
- **Autenticação stateless com JWT**: a API não guarda sessão, e o token vale por 2 horas.
- **DTOs com records**, para não expor as entidades diretamente na API.
- **Tratamento global de erros** com `@RestControllerAdvice`, para todas as respostas de erro terem o mesmo formato.

## Próximos passos

- Testes de integração com Testcontainers
- Controle de concorrência em transferências simultâneas
- Migrations com Flyway
- Deploy em nuvem
