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
POST /HikeList/user/{userID}   ➔ Inserts user data
GET /HikeList/user/{userID}    ➔ Retrieves user data
```

**Sample Payload:**

```json
{
  "userID": "123",
  "name": "John Doe",
  "lastModifiedTimeStamp": {
    "$date": "2025-03-22T10:00:00Z"
  }
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

## 🚀 Build and Run

### 1. Build All Modules

```sh
mvn clean install
```

First-time setup:

```sh
mvn install -N
```

### 2. Run Modules Locally

```sh
# Platform Module
mvn spring-boot:run -pl platform

# Ingestor Service Module
mvn spring-boot:run -pl ingestor-service

# Integrator Service Module
mvn spring-boot:run -pl integrator-service
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

