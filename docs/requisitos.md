# Requisitos - Dashboard Administrativo Pediu Patrão

## 1. Contexto e escopo

O Dashboard Administrativo permite que administrador e gerente acompanhem indicadores e tendências dos pedidos da lanchonete/pizzaria. A interface utiliza AdminLTE e Chart.js; o backend utiliza Java, Spring Boot e Spring Data MongoDB. As consultas são feitas por uma API REST e não alteram pedidos.

## 2. Requisitos funcionais

### RF01 - Visualizar indicadores gerais de vendas

**Descrição:** O sistema deverá apresentar total de pedidos, faturamento estimado, ticket médio e quantidade de pedidos cancelados.

**Atores:** Administrador, Gerente.

**Pré-condições:** Usuário deve ter sido autenticado e autorizado e a API deve estar disponível.

**Fluxo principal:**
1. O usuário acessa o Dashboard.
2. O frontend consulta a API.
3. O serviço calcula os indicadores sobre os pedidos encontrados.
4. O sistema apresenta os resultados.

**Regras de negócio:**
- O total de pedidos inclui os pedidos filtrados independentemente da situação.
- O faturamento estimado soma `valorTotal` dos pedidos, exceto `CANCELADO` e `CANCELAMENTO_SOLICITADO`.
- O ticket médio divide o faturamento pela quantidade de pedidos não excluídos do cálculo; se não houver nenhum, é zero.
- Cancelados contabiliza apenas a situação `CANCELADO`.

**Pós-condições:** Indicadores exibidos sem alterar os dados.

**Critérios de aceite:**
- Dado um período com pedidos, quando o painel carregar, então os quatro indicadores deverão ser apresentados.
- Dado um pedido cancelado, quando o faturamento for calculado, então seu valor não deverá ser somado.
- Dado um período sem pedidos, quando consultar, então os indicadores deverão ser zerados.

### RF02 - Analisar vendas por dia, semana ou mês

**Descrição:** O sistema deverá apresentar gráfico de vendas por período com agrupamento diário, semanal ou mensal.

**Atores:** Administrador, Gerente.

**Pré-condições:** Usuário autorizado e acesso à API.

**Fluxo principal:**
1. O usuário escolhe o agrupamento (`dia`, `semana` ou `mês`).
2. O Dashboard envia o parâmetro à API.
3. O serviço agrupa os valores dos pedidos filtrados.
4. O gráfico é atualizado.

**Regras de negócio:**
- As semanas seguem o calendário ISO, identificado por ano e número da semana.
- Os valores de vendas desconsideram pedidos cancelados ou com cancelamento solicitado.
- O agrupamento selecionado não altera o intervalo de datas consultado.

**Pós-condições:** Gráfico de vendas atualizado.

**Critérios de aceite:**
- Dadas vendas em datas diferentes, quando agrupar por dia, então devem surgir agrupamentos diários.
- Dadas vendas na mesma semana ISO, quando agrupar por semana, então seus valores deverão ser somados.
- Dadas vendas em meses diferentes, quando agrupar por mês, então os valores deverão aparecer em meses distintos.

### RF03 - Visualizar situações dos pedidos

**Descrição:** O sistema deverá apresentar em gráfico de rosca a quantidade de pedidos em cada situação.

**Atores:** Administrador, Gerente.

**Pré-condições:** Usuário autorizado e pedidos consultáveis.

**Fluxo principal:**
1. O usuário acessa o Dashboard ou aplica um filtro.
2. A API consulta os pedidos.
3. O serviço contabiliza as situações atuais.
4. O gráfico mostra a distribuição encontrada.

**Regras de negócio:**
- Cada pedido é contado uma vez em sua situação atual.
- `CANCELAMENTO_SOLICITADO` e `CANCELADO` são situações distintas.
- O gráfico respeita o intervalo e o status filtrados.

**Pós-condições:** Distribuição por situação exibida.

**Critérios de aceite:**
- Dados dois pedidos recebidos e um cancelado, quando consultar, então o gráfico deverá indicar dois recebidos e um cancelado.
- Dado um filtro de status, quando aplicar, então outras situações não deverão ser contabilizadas.

### RF04 - Visualizar produtos mais pedidos no período

**Descrição:** O sistema deverá apresentar um gráfico de barras com os produtos de maior quantidade pedida no período selecionado.

**Atores:** Administrador, Gerente.

**Pré-condições:** Usuário autorizado e pedidos com itens cadastrados.

**Fluxo principal:**
1. O usuário seleciona o período.
2. O frontend consulta a API.
3. O serviço soma as quantidades dos itens por produto.
4. O serviço ordena os produtos por quantidade decrescente.
5. O Dashboard apresenta até sete produtos no gráfico.

**Regras de negócio:**
- Itens do mesmo produto são somados, ainda que pertençam a pedidos diferentes.
- Pedidos `CANCELADO` e `CANCELAMENTO_SOLICITADO` não participam do cálculo.
- O gráfico mostra até sete pedidos.
- A ordenação considera a quantidade de unidades, não o valor de venda.

**Pós-condições:** Gráfico atualizado com os produtos do intervalo.

**Critérios de aceite:**
- Dado um produto registrado em dois pedidos, quando consultar, então a quantidade apresentada deverá ser a soma das duas ocorrências.
- Dado um período sem itens, quando consultar, então o frontend deverá informar ausência de produtos.

### RF05 - Filtrar informações

**Descrição:** O usuário deverá selecionar períodos predefinidos ou personalizados, filtrar por situação e definir o agrupamento das vendas.

**Atores:** Administrador, Gerente.

**Pré-condições:** Dashboard aberto.

**Fluxo principal:**
1. O usuário seleciona hoje, esta semana, este mês ou intervalo personalizado.
2. Opcionalmente, seleciona a situação dos pedidos e o agrupamento das vendas.
3. Clica em Aplicar filtros.
4. O Dashboard consulta a API com os parâmetros.
5. Todos os componentes são atualizados.

**Regras de negócio:**
- Datas são transmitidas no formato `AAAA-MM-DD` e seus limites são inclusivos.
- A data inicial não pode ser posterior à data final.
- Filtros omitidos não restringem a consulta
- O agrupamento padrão da API é `dia`.
- Limpar filtros restaura a seleção inicial do formulário.

**Pós-condições:** Indicadores e gráficos refletem os filtros.

**Critérios de aceite:**
- Dado um intervalo de um dia, quando consultar, então somente pedidos daquela data devem compor os resultados.
- Dada uma data inicial posterior à final, quando consultar, então a API deverá retornar erro HTTP 400.
- Dado um status selecionado, quando consultar, então somente pedidos com esse status devem ser considerados.

### RF06 i Consultar dados pela API

**Descrição:** O sistema deverá disponibilizar dados dos indicadores e gráficos por meio de `GET /api/dashboard` em JSON.

**Atores:** Administrador, Gerente.

**Pré-condições:** Usuário autenticado e autorizado e banco acessível.

**Fluxo principal:**
1. O frontend envia uma requisição GET, com parâmetros opcionais.
2. O `DashboardController` recebe e valida os tipos dos parâmetros.
3. O `DashboardService` consulta o `PedidoRepository`.
4. O serviço calcula os dados e os retorna em JSON.
5. O frontend atualiza a interface.

**Regras de negócio:**
- O endpoint aceita `inicio`, `fim`, `status` e `agrupamento`.
- Agrupamentos válidos: `dia`, `semana` e `mes`.
- O endpoint deve exigir perfil `admin` ou `gerente`.
- Erros de regras de entrada devem retornar HTTP 400.

**Pós-condições:** Resposta JSON entregue, ou erro informado.

**Critérios de aceite:**
- Dado usuário autorizado, quando enviar GET válido, então a API deverá retornar HTTP 200 e os campos previstos.
- Dado agrupamento inválido, quando consultar, então a API deverá retornar HTTP 400.
- Dado atendente autenticado, quando consultar, então o acesso deverá ser negado.

### RF07 - Atualizar e tratar dados do Dashboard

**Descrição:** O Dashboard deverá consultar dados periodicamente e indicar erros de comunicação ou ausência de resultados.

**Atores:** Administrador, Gerente.

**Pré-condições:** Dashboard aberto no navegador.

**Fluxo principal:**
1. O Dashboard consulta a API ao abrir a página.
2. Enquanto a aba estiver visível, inicia novas consultas a cada 60 segundos.
3. O sistema atualiza gráficos, indicadores e horário da última atualização bem-sucedida.
4. Se ocorrer uma falha, exibe a mensagem de erro.

**Regras de negócio:**
- O frontend cancela a consulta anterior se uma nova consulta for iniciada.
- Os dados devem vir da API, sem números fictícios.
- Tabelas sem registros devem apresentar mensagem de ausência de dados.

**Pós-condições:** Dados recentes apresentados ou mensagem de falha exibida.

**Critérios de aceite:**
- Dada uma aba visível, quando transcorrerem 60 segundos, então deverá ocorrer nova consulta.
- Dada uma falha na API, quando atualizar, então o usuário deverá receber uma mensagem de erro.
- Dada uma consulta sem pedidos, quando atualizar, então devem ser exibidos zeros e componentes vazios.

### RF08 - Controlar acesso ao Dashboard

**Descrição:** O sistema deverá permitir o acesso ao Dashboard e à API apenas aos perfis de administrador e gerente.

**Atores:** Administrador, Gerente, Atendente.

**Pré-condições:** Existência de usuários com papéis definidos.

**Fluxo principal:**
1. O usuário faz login.
2. O sistema identifica seu papel.
3. O menu exibe o link Dashboard somente para administrador e gerente.
4. Ao abrir `/admin`, o Spring Security confirma a permissão.

**Fluxo alternativo:** Se um atendente tentar acessar `/admin` ou `/api/dashboard`, o sistema nega acesso.

**Regras de negócio:**
- A proteção deve existir no backend, independentemente da exibição do botão no menu.
- A página é atendida por `/admin` e a API por `/api/dashboard`.
- Usuários sem sessão devem ser encaminhados para autenticação.

**Pós-condições:** Acesso concedido ou negado conforme o papel.

**Critérios de aceite:**
- Dado administrador autenticado, quando abrir `/admin`, então a página deverá ser apresentada.
- Dado gerente autenticado, quando abrir `/admin`, então a página deverá ser apresentada.
- Dado atendente autenticado, quando abrir `/admin`, então deverá receber HTTP 403.

## 3. Requisitos não funcionais

| ID | Requisito | Especificação verificável |
|---|---|---|
| RNF01 | Segurança | O backend deve proteger `/admin` e `GET /api/dashboard` por perfil usando Spring Security. |
| RNF02 | Usabilidade | A página deve separar indicadores, gráficos, filtros e pedidos recentes em áreas identificadas. |
| RNF03 | Responsividade | O layout do AdminLTE deve reorganizar cartões e gráficos em telas menores. |
| RNF04 | Interoperabilidade | O frontend deve consumir dados via HTTP GET e JSON. |
| RNF05 | Manutenibilidade | Regras de cálculo devem permanecer no `DashboardService`, separadas da camada Controller. |
| RNF06 | Atualização | Uma aba visível deve tentar atualizar o dashboard automaticamente a cada 60 segundos. |
| RNF07 | Confiabilidade | Erros de consulta devem ser comunicados sem substituir os dados por valores inventados. |
| RNF08 | Compatibilidade | A interface deve funcionar em navegador moderno com suporte a JavaScript e acesso às bibliotecas CDN utilizadas. |