# Truck Fleet Management
Gerenciamento de Frota de Caminhões

## Backend (Java + Spring Boot)
- **Banco**: PostgreSQL (`application.yml`)
- **Migrações**: Flyway (schema versionado)
- **Ferramenta de DB**: DBeaver (testes de conexão/consultas)
- **Entidade**: `TruckEntity`
  - Campos: `license_plate`, `brand`, `model`, `manufacturing_year`, `fipe_price` + timestamps
  - **Lombok**: reduz boilerplate (getters/setters/constructors)
  - **Bean Validation**: regras (formatos de placa) + normalização (maiúsculo)
- **Camadas**
  - **Controller** (`TruckController`): expõe CRUD + endpoints de apoio à FIPE
  - **Service** (`TruckService`): regras de negócio (unicidade de placa, integração FIPE, conversão de preço PT-BR → `BigDecimal`)
  - **Repository** (`TruckRepository` – JPA): persistência e checagem de existência de placa
  - **Cliente FIPE** (`FipeApiClient`): isola chamadas externas; mapeia DTOs via `@JsonProperty`
    - **Fluxo**: **marca → modelos → anos → preço**

## Frontend - Angular
### truck-list
- Responsável pela listagem responsiva dos caminhões;  
- Botões de **Cadastro de Novo Caminhão** e **Edição** de algum já existente;  
### truck-form
- Responsável por **Cadastro e Edição**;  
- Reactive Forms com encadeamento:  
  - marca → carrega modelos → carrega anos → consulta preço;  
- Possui também a validação de placa indicando formatos aceitos;  


## Testes
Implementados com **JUnit + Mockito** para `TruckService`:
- Criação e atualização  
- Regex da placa  
- Listagem  
- Duplicação/ausência  

   
### Endpoints (CRUD)
```http
GET  /api/trucks
GET  /api/trucks/{id}
POST /api/trucks
PUT  /api/trucks/{id}
```
### Endpoints (FIPE)
```http
GET /api/trucks/fipe/brands
GET /api/trucks/fipe/brands/{brandCode}/models
GET /api/trucks/fipe/brands/{brandCode}/models/{modelCode}/years
GET /api/trucks/fipe/brands/{brandCode}/models/{modelCode}/years/{yearCode}
```
