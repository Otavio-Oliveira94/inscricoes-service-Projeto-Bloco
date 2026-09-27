# Histórico de alterações — Eventos Express

Foi acrescentado um histórico de dados em arquivo aos dois microsserviços, com uma tela de consulta no React.

## Como funciona

1. O usuário cria, edita ou exclui um evento ou uma inscrição, pela interface ou pela API existente.
2. O service obtém uma cópia dos dados anteriores e posteriores utilizando os DTOs existentes.
3. O `AuditoriaService` prepara uma linha JSON e espera a confirmação da transação no banco.
4. Se a transação for confirmada, a linha é acrescentada ao arquivo. Se ocorrer rollback, nada é gravado.
5. A tela **Histórico**, acessível pelo menu, consulta o arquivo por meio da API de cada serviço.

Operações registradas: `CRIACAO`, `ATUALIZACAO` e `EXCLUSAO`. Uma atualização que mantenha os mesmos valores não gera uma nova entrada. Consultas GET não geram entradas. Alterações anteriores à instalação desta funcionalidade não podem ser recuperadas.

Cada entrada contém um identificador próprio, a operação, o ID do registro, os dados `antes` e `depois`, o instante do log e a identificação da execução do serviço. O instante é um metadado da entrada do log; não foi adicionado nenhum campo de auditoria a `Evento` ou `Inscricao`. O campo `dataInscricao`, que já existia, continua sendo um dado da inscrição.

## Arquivos e endpoints

| Projeto | Arquivo padrão, relativo à pasta de execução | Consulta |
| --- | --- | --- |
| eventos-PB | `logs/auditoria-eventos.jsonl` | `GET http://localhost:8080/auditoria` |
| inscricoes-service | `logs/auditoria-inscricoes.jsonl` | `GET http://localhost:8081/auditoria` |

Os arquivos são criados automaticamente. JSON Lines significa que cada linha contém uma entrada JSON completa.

Exemplos de consulta no Postman:

```http
GET http://localhost:8080/auditoria
GET http://localhost:8080/auditoria?registroId=1
GET http://localhost:8081/auditoria?registroId=1&limite=20
```

- `registroId`: opcional; ID do evento ou da inscrição, conforme o serviço consultado.
- `limite`: opcional; de 1 a 500, padrão 100. A consulta retorna as últimas entradas primeiro.
- `execucao`: opcional; utilize o valor devolvido em uma entrada para restringir a consulta àquela execução do serviço.
- Sem entradas correspondentes, a resposta é `200 OK` com `[]`.
- Filtros inválidos retornam `400 Bad Request`.
- Falha de leitura ou arquivo corrompido retornam `503 Service Unavailable`.
- A consulta não exige que o evento/inscrição ainda exista, portanto o histórico permanece consultável depois de uma exclusão.

## Executar

Requisitos: Java 21 e a versão do Node compatível com o Vite já usado no projeto.

Abra cada backend separadamente no IntelliJ. Execute suas classes principais ou utilize o Maven Wrapper, em terminais separados:

```powershell
# Na pasta eventos-PB
.\mvnw.cmd spring-boot:run
```

```powershell
# Na pasta inscricoes-service
.\mvnw.cmd spring-boot:run
```

No frontend:

```powershell
npm ci
npm run dev
```

No Linux/macOS, utilize `bash mvnw` em lugar de `.\mvnw.cmd`.

Acesse `http://localhost:5173`, escolha **Histórico**, selecione Eventos ou Inscrições e clique em **Consultar / atualizar**. O filtro por ID é opcional. Abra **Ver dados anteriores e posteriores** para comparar os valores. Cada consulta mostra até as últimas 100 entradas correspondentes.

O arquivo é relativo ao diretório em que o backend foi iniciado. Execute cada backend sempre a partir da sua própria pasta para consultar o mesmo arquivo entre reinicializações.



## Testes automatizados

Em cada backend:

```powershell
.\mvnw.cmd clean test
```

Os novos testes cobrem criação/edição/exclusão, preservação dos dados anteriores, consulta após exclusão, rollback, ausência de mudança, filtros, limite, CORS, leitura por uma nova instância e arquivo inválido. O serviço de inscrições também verifica que um evento inexistente não gera histórico. O cliente Feign é simulado nos testes; não é necessário iniciar o outro serviço.

No frontend:

```powershell
npm run build
npm run lint
```

