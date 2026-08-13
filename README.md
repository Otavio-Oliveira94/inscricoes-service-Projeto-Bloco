# Microsserviço de Inscrições

Microsserviço responsável pelo gerenciamento das inscrições dos
participantes nos eventos.

O serviço utiliza OpenFeign para verificar no microsserviço de eventos
se o evento informado existe antes de cadastrar ou editar uma inscrição.

## Tecnologias

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Cloud OpenFeign
- Maven
- H2 Database

## Portas e integração

- Inscrições Service: http://localhost:8081
- Eventos Service: http://localhost:8080

## Endpoints

- POST /inscricoes
- GET /inscricoes
- GET /inscricoes/{id}
- GET /inscricoes/evento/{eventoId}
- PUT /inscricoes/{id}
- DELETE /inscricoes/{id}

## Executando o projeto

Primeiro, inicie o microsserviço de eventos.

Depois execute:

mvn clean test
mvn spring-boot:run

## Banco H2

Console: http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:inscricoesdb
Usuário: sa
Senha: vazia