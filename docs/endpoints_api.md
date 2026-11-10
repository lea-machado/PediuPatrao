# Documentação endpoints API (Dashboard Administrativo)

## 1. Visão geral

O Dashboard do Pediu Patrão utiliza uma API REST implementada com Spring Boot para consultar indicadores e gráficos derivados da coleção `pedidos` do MongoDB. O endpoint é somente de leitura e exige autenticação com papel de administrador ou gerente.

**URL base local:** `http://localhost:8080`

**Formato de resposta:** `application/json`

## 2. Endpoint GET /api/dashboard

**Finalidade:** consultar indicadores, situações dos pedidos, vendas por período, produtos mais pedidos e pedidos recentes.

**Método HTTP:** `GET`

**Autorização:** usuário autenticado com `ROLE_admin` ou `ROLE_gerente`.

### Parâmetros de consulta

| Nome | Tipo | Obrigatório | Valor padrão | Descrição |
|---|---|---|---|---|
| `inicio` | Data ISO (`AAAA-MM-DD`) | Não | Sem limite inferior | Considera pedidos a partir desta data, inclusive. |
| `fim` | Data ISO (`AAAA-MM-DD`) | Não | Sem limite superior | Considera pedidos até esta data, inclusive. |
| `status` | String | Não | Todas as situações | Filtra pela situação atual; comparação sem distinção de maiúsculas/minúsculas. |
| `agrupamento` | String | Não | `dia` | Valores aceitos: `dia`, `semana`, `mes`. |

### Exemplo 1 - Consulta sem filtros

```http
GET /api/dashboard HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Exemplo 2 - Vendas por semana

```http
GET /api/dashboard?inicio=2026-11-01&fim=2026-11-30&agrupamento=semana HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Exemplo 3 - Apenas pedidos finalizados

```http
GET /api/dashboard?inicio=2026-11-01&fim=2026-11-30&status=FINALIZADO&agrupamento=dia HTTP/1.1
Host: localhost:8080
Accept: application/json
```

### Exemplo de resposta de sucesso (HTTP 200)

```json
{
  "totalPedidos": 3,
  "pedidosValidos": 2,
  "faturamento": 150.00,
  "ticketMedio": 75.00,
  "cancelados": 1,
  "situacoes": {
    "CANCELADO": 1,
    "FINALIZADO": 1,
    "RECEBIDO": 1
  },
  "vendasPorPeriodo": [
    { "periodo": "2026-S46", "vendas": 150.00, "pedidos": 3 }
  ],
  "produtosMaisVendidos": [
    { "nome": "Pizza Calabresa", "quantidade": 3, "total": 120.00 },
    { "nome": "Refrigerante", "quantidade": 2, "total": 20.00 }
  ],
  "pedidosRecentes": [
    { "id": "pedido003", "data": "2026-11-09T14:00", "status": "CANCELADO", "total": 50.00 },
    { "id": "pedido002", "data": "2026-11-09T13:00", "status": "FINALIZADO", "total": 100.00 },
    { "id": "pedido001", "data": "2026-11-09T12:00", "status": "RECEBIDO", "total": 50.00 }
  ]
}
```

### Significado dos campos

| Campo | Tipo | Descrição |
|---|---|---|
| `totalPedidos` | Inteiro | Quantidade de pedidos encontrados após os filtros. |
| `pedidosValidos` | Inteiro | Pedidos não cancelados e sem cancelamento solicitado, usados no cálculo financeiro. |
| `faturamento` | Decimal | Soma de `valorTotal` dos pedidos válidos. É estimado, não significa necessariamente pagamento confirmado. |
| `ticketMedio` | Decimal | `faturamento / pedidosValidos`, com duas casas decimais. |
| `cancelados` | Inteiro | Quantidade de pedidos com status `CANCELADO`. |
| `situacoes` | Objeto | Mapa com a quantidade por status atual. |
| `vendasPorPeriodo` | Lista | Período, valor de vendas válidas e quantidade total de pedidos do período. |
| `produtosMaisVendidos` | Lista | Até dez produtos ordenados por unidades; o gráfico do frontend utiliza os sete primeiros. |
| `pedidosRecentes` | Lista | Até dez pedidos encontrados, ordenados por data de entrada decrescente. |

### Regras de cálculo

1. Apenas pedidos com `dataEntrada` preenchida participam das consultas.
2. Pedidos `CANCELADO` e `CANCELAMENTO_SOLICITADO` não contribuem para o faturamento e a contagem de produtos.
3. Quantidades de itens são somadas por `produtoId`; se o identificador estiver ausente, utiliza-se o nome.
4. Agrupamento diário utiliza `AAAA-MM-DD`; mensal utiliza `AAAA-MM`; semanal utiliza `AAAA-SNN`, de acordo com a semana ISO.
5. Os filtros de data e status são aplicados antes dos cálculos.

## 3. Respostas de erro

| HTTP | Situação | Comportamento esperado |
|---|---|---|
| `400 Bad Request` | `inicio` posterior a `fim` | JSON com mensagem no campo `erro`. |
| `400 Bad Request` | Agrupamento diferente de `dia`, `semana`, `mes` | JSON com mensagem no campo `erro`. |
| `400 Bad Request` | Data enviada em formato incompatível | Erro de conversão de parâmetro tratado pelo Spring MVC. |
| `302 Found` | Sessão não autenticada em navegação convencional | Redirecionamento para `/login`. |
| `403 Forbidden` | Usuário autenticado sem perfil autorizado | Acesso bloqueado pelo Spring Security. |
| `500 Internal Server Error` | Erro não tratado, como falha interna | Resposta de erro do servidor; verificar logs. |

### Exemplo de erro por intervalo inválido

```http
GET /api/dashboard?inicio=2026-11-30&fim=2026-11-01
```

```json
{
  "erro": "A data inicial não pode ser maior que a final."
}
```

### Exemplo de erro por agrupamento inválido

```http
GET /api/dashboard?agrupamento=ano
```

```json
{
  "erro": "O agrupamento deve ser dia, semana ou mes."
}
```

**Responsabilidades:**

- `DashboardController`: recebe parâmetros, chama o serviço e devolve JSON.
- `DashboardService`: valida intervalo/agrupamento, filtra pedidos e calcula dados agregados.
- `PedidoRepository`: acesso aos documentos da coleção de pedidos.
- `SecurityConfig`: controla a autorização para a página e API.
- `dashboard.html`: aplica filtros, chama a API e atualiza os componentes visuais.
