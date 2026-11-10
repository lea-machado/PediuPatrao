# Pediu Patrão

Esse é um sistema de gerenciamento de uma lanchonete e foi desenvolvido a partir de um projeto base disponibilizado na matéria de Análise e projeto de Software. 
A aplicação foi desenvolvida utilizando:
- Java 21
- Spring Boot 3.4.4
- Spring Security
- Thymeleaf
- Mongo DB

## Mudanças do projeto inicial
- Foram removidos os perfis "USER" e "ADMIN" e passaram a existir os perfis "ADMIN", "GERENTE", "ATENDENTE".
- Para cada perfil foram criadas permissões do que é possível acessar e alterar, sendo elas:

| Perfil | Principais ações |
|---|---|
| **ADMIN** | Gerenciar usuários e perfis, consultar pedidos e auditoria, excluir clientes, cadastrar entregadores. |
| **GERENTE** | Consultar clientes, gerenciar andamento de pedidos, autorizar descontos e cancelamentos, consultar auditoria, atribuir entregadores. |
| **ATENDENTE** | Cadastrar e editar clientes, registrar e acompanhar pedidos, solicitar descontos e cancelamentos, atribuir entregadores. |

- Alteração do formato dos pedidos, adicionando itens, quantidade, preço, desconto, subtotal/total, responsável e data.
- Acompanhamento dos pedidos por status `RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA / RETIRADO → FINALIZADO`.
- Adição das funcionalidades e fluxo de cancelamento e aplicação de desconto (atendente solicita e gerente insere suas credenciais para aprovar)
- Mudança do desconto para porcentagem, com limitação a 20%.
- Auditoria de logs, sendo visível no front-end para gerentes e administradores.
- Cadastro e atribuição de entregadores.
- Listagem de funcionários.
- Conexão do MongoDB alterada para variável de ambiente.
- Testes unitários e de segurança.
- Adição do Dashboard Administrativo utilizando AdminLTE, com indicadores de vendas, gráficos e consulta aos dados por meio da API REST.
- Adição de filtros por período e situação dos pedidos, com visualização de vendas por dia, semana ou mês e dos produtos mais pedidos no período.

## Dashboard Administrativo

Foi desenvolvido um Dashboard Administrativo para auxiliar no acompanhamento das vendas e dos pedidos da lanchonete. O painel utiliza AdminLTE e Chart.js e consome os dados disponibilizados pelo endpoint `GET /api/dashboard`.

O Dashboard apresenta:
- Total de pedidos, faturamento estimado, ticket médio e quantidade de pedidos cancelados.
- Gráfico de vendas agrupadas por dia, semana ou mês.
- Gráfico da situação atual dos pedidos.
- Gráfico dos produtos mais pedidos no período selecionado.
- Filtros por data inicial, data final e situação dos pedidos.
- Atualização automática dos dados a cada 60 segundos enquanto a página estiver visível.
- Lista dos pedidos recentes no período consultado.

O acesso é destinado aos perfis **ADMIN** e **GERENTE**, mediante as permissões configuradas no Spring Security. Após o login, o painel pode ser acessado pelo menu **Dashboard** ou pelo endereço `http://localhost:8080/admin`.

## Como executar
É necessário que o usuário possua instalados:
- JDK 21.
- Maven.
- Conta e cluster no MongoDB Atlas.

### Configure o MongoDB Atlas
1. Crie um cluster.
2. Cadastre um usuário para o banco de dados.
3. Clique em Connect, defina a linguagem para Java e Client Libray como SpringData MangoDB.
4. Copie a URI de conexão. Substitua `<db_password>` pela senha do usuário do banco (caso haja caracteres especiais na senha, eles devem ser codificados para uma URL).
5. Abra um terminal na pasta raiz do projeto e configure o MangoDB com o seguinte comando:

**Windows**:

```powershell
$env:MONGODB_URI = "mongodb+srv://USUARIO:SENHA@SEU-CLUSTER.mongodb.net/"
```

**Linux ou macOS**:
```bash
export MONGODB_URI='mongodb+srv://USUARIO:SENHA@SEU-CLUSTER.mongodb.net/'
```

> Substitua o exemplo pela URI real obtida no Atlas (não esqueça de inserir sua senha na URI copiada).

### Crie o primeiro usuário (admin)
1. Crie o primeiro usuário admin através dos comandos abaixo (substitua 'SENHA' por uma senha que desejar):

**Windows**:

```powershell
$env:admin_username = "admin"
$env:admin_password = "SENHA"
```

**Linux ou macOS**:

```bash
export admin_username='admin'
export admin_password='SENHA'
```

> O administrador é criado apenas se o usuário ainda não existir. Modificar essas variáveis não altera a senha de uma conta já cadastrada.

### Compile e execute a aplicação
1. No mesmo terminal execute:
```bash
mvn clean compile
mvn spring-boot:run
```

### Acesse a aplicação
1. No navegador Web, acesse http://localhost:8080.
2. Entre com a conta de administrador que você criou nos passos anteriores.
3. Utilize a área **Usuários** para cadastrar contas com os perfis `gerente` e `atendente`.
4. Caso deseje trocar de perfil, encerre a sessão pelo botão **Sair**.

> **Observações:** o Maven deve ser executado na pasta que contém `pom.xml`.
>
> É necessário ter acesso à internet para baixar dependências e acessar o MongoDB Atlas.
>
> É necessário configurar as variáveis de ambiente toda vez antes de iniciar a aplicação.

## Testes automatizados
O projeto possui 30 testes automatizados, distribuídos em quatro classes, abrangendo testes unitários, testes da camada web e verificações de segurança e autorização.

Para executá-los, em um terminal aberto na pasta raiz do projeto, rode:
```bash
mvn clean test
```

Também foram adicionadas classes de testes automatizados para o Dashboard Administrativo, utilizando JUnit 5, Mockito e MockMvc, contemplando cálculos dos indicadores, agrupamento de vendas, filtros, dados da API e controle de acesso.

Para executar apenas os testes do Dashboard:

```bash
mvn -Dtest=DashboardServiceTest,DashboardControllerTest test
```

### Evidência da execução dos testes

![Evidência da execução dos testes automatizados](docs/testes-executados.png)

## Requisitos do Dashboard

A documentação contém os requisitos funcionais e não funcionais do Dashboard Administrativo, incluindo descrição, regras de negócio e critérios de aceite.

**[Clique aqui para acessar o arquivo de requisitos](docs/requisitos.md)**

## Documentação dos endpoints

A documentação da API apresenta o endpoint desenvolvido, seus parâmetros, exemplos de requisição e resposta, possíveis erros e regras de negócio.

**[Clique aqui para acessar a documentação dos endpoints](docs/endpoints_api.md)**
