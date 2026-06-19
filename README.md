## 🚀 Como Rodar o Projeto via GitHub e Docker

Siga os passos abaixo para clonar o repositório e executar a aplicação Spring Boot e o banco de dados PostgreSQL na sua máquina.

### Prerequisitos
* [Git](https://git-scm.com)
* [Docker e Docker Compose](https://docker.com)

---

### 1. Clonar o Repositório
Abra o terminal na pasta onde deseja salvar o projeto e execute o comando abaixo:
```bash
git clone <https://github.com/Vilaronga/accenture>
```

Entre na pasta que foi criada:
```bash
cd <PASTA_DO_PROJETO>
```

### 2. Configurar as Variáveis de Ambiente
O projeto precisa das credenciais do [Google](https://aistudio.google.com/api-keys) e da [Azure](https://portal.azure.com/#home) para funcionar.

1. Na raiz do projeto, crie um arquivo com nome `.env`.
2. Abra o arquivo `.env` e preencha com as suas chaves válidas:
```ini
GOOGLE_API_KEY=sua_chave_aqui
AZURE_CLIENT_ID=seu_azure_id_aqui
AZURE_CLIENT_SECRET=seu_client_secret_aqui
AZURE_TENANT_ID=seu_tenant_id_aqui
```

### 3. Executar o Projeto com Docker
Como este projeto utiliza o recurso **Docker Compose Watch**, você pode rodá-lo de forma que qualquer alteração feita no código-fonte (`/src`) ou no `pom.xml` seja sincronizada instantaneamente com o container.

Execute o comando abaixo no terminal:
```bash
docker compose watch
```

> 💡 **Nota:** Se você preferir apenas rodar a aplicação em segundo plano, sem o modo de sincronização ativa, utilize `docker compose up -d`.

### 4. Acessar a Aplicação
Após o Docker finalizar a compilação do Spring e subir o PostgreSQL, a aplicação estará disponível em:
* **API / Web:** `http://localhost:8080`
* **Banco de Dados (PostgreSQL):** Porta externa `5433` (Usuário: `postgres` / Senha: `postgres` / Banco: `accenture`)
