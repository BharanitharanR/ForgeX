# ForgeX

Crafting Services from Metadata, in Real-Time

ForgeX is licensed under the AGPL-3.0 for open-source use. For proprietary/commercial use, a separate commercial license is required. Contact [bharani3ran@gmail.com](mailto\:bharani3ran@gmail.com) for commercial licensing inquiries.

---

## 🚀 ForgeX Platform - Overview

ForgeX is a modular, opinionated Spring Boot platform designed to generate, compile, and deploy microservices from metadata. It provides seamless integration with MongoDB, OpenAPI, and Docker for rapid prototyping and deployment.

### 🔧 Modules

#### 1. **Platform Module**

- Reusable utilities and database operations.
- MongoDB handler classes and beans for dependency injection.

#### 2. **Ingestor Service Module**

- Consumes and processes incoming data.
- Leverages the Platform Module for database operations.

#### 3. **Integrator Service Module**

- Orchestrates communication between services and external systems.
- Dockerized and built using Spring Boot.

---

## 📁 Project Structure

```
/forgex
├── framework                # Database utilities
│   ├── src/main/java/com/batty/forgex/framework/datastore
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── ingestor                  # Parse the Metadata and creates the Openapi spec
│   ├── src/main/java/com/batty/forgex/ingestor
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── entityBuilder             # Use the openapi spec to generate Microservices app and deploy them 
│   ├── src/main/java/com/batty/forgex/entityBuilder
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── docker-compose.yml      # Multi-service configuration
├── .env                    # Environment variables
├── README.md               # Main documentation
├── .gitignore
└── pom.xml                 # Maven parent configuration
```

---

## ⚙️ Technology Stack

- **Backend**: Spring Boot (Java)
- **Database**: MongoDB
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven
- **API Specification**: OpenAPI (Swagger)

---

## 🔥 Module Details

### 1. ⚖️ Framework Module

Reusable MongoDB utilities exposed as injectable Spring beans.

**Features:**

- MongoDB connection and CRUD
- Utility classes and helpers

**Components:**

- `DatabaseHandler`: Encapsulates MongoDB logic
- `application.properties`: MongoDB configs

---

### 2. 🔥 Ingestor 

Consumes and processes external metadata and converts it into openapi spec

**Endpoints:**

```
  /graph/process:
    post:
      summary: Process a graph input
      description: Accepts a graph-based input (nodes and edges) and generates microservices, REST endpoints, and OpenAPI specs.
      
  /graph/{id}:
    get:
      summary: Retrieve status of the deployment
```

**Sample Payload:**
- To generate a Simple APplication that maintains purchase orders
```json
{
  "nodes": [
    {
      "type": "ENTITY",
      "name": "PurchaseOrder",
      "fields": [
        {"name": "poId", "type": "string", "required": true},
        {"name": "date", "type": "date", "required": true},
        {"name": "supplierId", "type": "string", "required": true},
        {"name": "status", "type": "string", "required": false, "default": "Pending"},
        {"name": "totalAmount", "type": "number", "required": true}
      ]
    },
    {
      "type": "ENTITY",
      "name": "Supplier",
      "fields": [
        {"name": "supplierId", "type": "string", "required": true},
        {"name": "name", "type": "string", "required": true},
        {"name": "contact", "type": "string", "required": false},
        {"name": "address",:
          "string", "required": false}
      ]
    },
    {
      "type": "ENTITY",
      "name": "PurchaseOrderLineItem",
      "fields": [
        {"name": "lineItemId", "type": "string", "required": true},
        {"name": "poId", "type": "string", "required": true},
        {"name": "productId", "type": "string", "required": true},
        {"name": "quantity", "type": "number", "required": true},
        {"name": "unitPrice", "type": "number", "required": true},
        {"name": "totalPrice", "type": "number", "required": true}
      ]
    },
    {
      "type": "ENTITY",
      "name": "Product",
      "fields": [
        {"name": "productId", "type": "string", "required": true},
        {"name": "name", "type": "string", "required": true},
        {"name": "description", "type": "string", "required": false},
        {"name": "price", "type": "number", "required": true}
      ]
    }
  ],
  "edges": [
    {
      "source": "PurchaseOrder",
      "target": "Supplier",
      "relationship": "RELATED_TO"
    },
    {
      "source": "PurchaseOrder",
      "target": "PurchaseOrderLineItem",
      "relationship": "HAS_LINE_ITEMS"
    },
    {
      "source": "PurchaseOrderLineItem",
      "target": "Product",
      "relationship": "REFERS_TO"
    }
  ]
}
```

---

### 3. 🔗 Integrator Service

Coordinates between services and integrates data across endpoints.

**Features:**

- Service-to-service communication
- Retry/error handling logic
- Data aggregation layer

---

## 🛣️ Docker Setup

### Docker Compose

Run all services in a shared network:

```sh
docker-compose up -d
docker-compose down
```

### Sample docker-compose.yml

```yaml
services:
  mongo:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - forgex-integrator

  ingestor-service:
    build:
      context: ./ingestor-service
    ports:
      - "8081:8081"
    environment:
      - MONGO_HOST=mongo
    networks:
      - forgex-integrator

  integrator-service:
    build:
      context: ./integrator-service
    ports:
      - "8082:8082"
    networks:
      - forgex-integrator

networks:
  forgex-integrator:

volumes:
  mongodb_data:
```

---
## 🚀 Tech Stack & Badges

![Java](https://img.shields.io/badge/Java-17-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7+-brightgreen?logo=spring)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green?logo=mongodb)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue?logo=docker)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-orange?logo=swagger)
![LLM](https://img.shields.io/badge/LLM-Ready-purple?logo=openai)
![NGINX](https://img.shields.io/badge/NGINX-Reverse--Proxy-lightgrey?logo=nginx)
![CI/CD](https://img.shields.io/badge/GitHub_Actions-Ready-blue?logo=githubactions)

## Architectural flow

                +-------------------+
                |   OpenWeb UI      |
                | (ChatGPT-like UI) |
                +--------+----------+
                         |
                         v
              +------------------------+
              |   MCP Server (Python)  |
              |  (Model Context Layer) |
              +-----+------------------+
                           |
            +--------------+
            |                              
            v                              
        +------------+           +------------------+
        | Ingestor   |           | Entity Builder   |
        | Service    |---------> | (Microservice Gen)|
        +------------+           +------------------+
                 \                         /
                  \                       /
                   v                     v
                    +-----------------+
                    |   MongoDB       |
                    | (Data Store)    |
                    +-----------------+

           +-----------------+
           | Integrator Svc  |
           | (Aggregation &  |
           | Orchestration)  |
           +-----------------+

           +-----------------+
           |   NGINX Proxy   |
           +-----------------+

           +-----------------+
           | Eureka Registry |
           +-----------------+

## 🚀 Build and Run

### 1. Build All Modules and brings the DOcker images up and running

```sh
mvn clean install -DskipTests
```

First-time setup:

```sh
mvn install -N
```
- Following are the list of docker containers that are run when you run mvn clean install -Dskiptests 
```sh
| Container Name        | Image Name                               | Description                                                                                                |
| --------------------- | ---------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `entitybuilder-1`     | `forgex/entitybuilder:0.0.1-SNAPSHOT`    | Dynamically builds microservices and entities from graph-based metadata.                                   |
| `ingestor-service-1`  | `forgex/ingestor-service:0.0.1-SNAPSHOT` | Handles ingestion of user-defined metadata (nodes/edges) and persists them into MongoDB.                   |
| `forgex-mcp-server-1` | `forgex/forgex-mcp-server:0.0.1`         | Model Context Protocol (MCP) server. Acts as a gateway between LLMs and ForgeX services like the Ingestor. |
| `registry-service-1`  | `registry-service:0.0.1-SNAPSHOT`        | Spring Cloud Eureka service registry for microservice discovery and health checks.                         |
| `nginx-1`             | `nginx:latest`                           | Reverse proxy that routes requests to appropriate internal services.                                       |
| `mongo-1`             | `mongo:latest`                           | MongoDB instance backing metadata and graph storage.                                                       |
| `open-webui-1`        | `ghcr.io/open-webui/open-webui:main`     | ChatGPT-like interface that connects to the LLM and facilitates communication with the MCP server.         |
| `integrator-1`        | `forgex/integrator:0.0.1-SNAPSHOT`       | Orchestrates cross-service interactions, aggregates data, and performs external API integration.           |

```


---

## 📘️ API Documentation

### Swagger UI

```sh
http://localhost:8081/swagger-ui.html
```

### Sample API Calls

```sh
# Insert User Data
curl -X POST "http://localhost:8081/HikeList/user/123" \
  -H "Content-Type: application/json" \
  -d '{"userID": "123", "name": "John Doe"}'

# Get User Data
curl -X GET "http://localhost:8081/HikeList/user/123"
```

---

## 🔎 Environment Variables

- `MONGO_HOST`: MongoDB container name
- `MONGO_PORT`: Default 27017
- `SERVER_PORT`: Exposed service port (e.g. 8081)
- `JAVA_OPTS`: JVM arguments

---

## ✅ Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push and open a pull request

---

## ⚠️ Troubleshooting

- Mongo connection issues: Ensure Docker containers are networked
- Data persistence: Check volume mounting
- Debug logs: `docker logs <container-id>`

---

## 🎯 Roadmap / Future Enhancements

- Kafka for event-driven messaging
- JWT Authentication
- GitHub Actions CI/CD pipeline

---

## 💪 Author

**Bharani Tharan Ragunathan**\
Developer & Architect\
[Email](mailto\:bharani3ran@gmail.com)

