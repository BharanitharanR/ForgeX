# ForgeX
Crafting Services from Metadata, in Real-Time



ForgeX is licensed under the AGPL-3.0 for open-source use.  
For proprietary/commercial use, a separate commercial license is required.  
Contact bharani3ran@gmail.com for commercial licensing inquiries.


🚀 ForgeX Platform - README
🛠️ Project Overview
ForgeX is a modular platform designed for seamless data ingestion, processing, and integration. It consists of three primary modules:

Platform Module:

Contains reusable database utilities.

Includes MongoDB handler classes and other common functionalities.

Exposes beans for dependency injection across services.

Ingestor Service Module:

Responsible for consuming and processing incoming data.

Uses Spring Boot and MongoDB for storing and retrieving data.

Connects to the Platform Module for database operations.

Integrator Module:

Orchestrates interactions between multiple services.

Facilitates communication between the Ingestor and external systems.

Built with Spring Boot and Dockerized for deployment.

📁 Project Structure
bash
Copy
Edit
/forgex
├── platform                # Platform Module (Database utilities)
│      ├── src/main/java/com/batty/forgex/framework/datastore  # Database handler
│      ├── Dockerfile
│      ├── pom.xml
│      └── README.md
│
├── ingestor-service        # Ingestor Service Module
│      ├── src/main/java/com/batty/forgex/ingestor
│      ├── Dockerfile
│      ├── pom.xml
│      └── README.md
│
├── integrator-service      # Integrator Module
│      ├── src/main/java/com/batty/forgex/integrator
│      ├── Dockerfile
│      ├── pom.xml
│      └── README.md
│
├── docker-compose.yml      # Docker compose configuration
├── .env                    # Environment variables
├── README.md               # This file
├── .gitignore
├── mvnw
└── pom.xml                 # Maven parent configuration
⚙️ Technology Stack
Backend: Spring Boot (Java)

Database: MongoDB

Containerization: Docker & Docker Compose

Build Tool: Maven

API Specification: OpenAPI (Swagger)

🔥 Modules Overview
🛠️ 1. Platform Module
The Platform Module provides reusable utilities, primarily for database operations.

✅ Features:

MongoDB connection and CRUD operations.

Utility classes and helper methods.

Exposes beans for Spring Boot services.

✅ Key Components:

DatabaseHandler: Handles MongoDB operations (insert, update, delete, find).

application.properties: Contains MongoDB configurations.

🔥 2. Ingestor Service Module
The Ingestor Service is responsible for:

Consuming data from external sources.

Processing and storing it in MongoDB.

Using the platform module for database operations.

✅ Key Endpoints:

POST /HikeList/user/{userID} → Inserts user data into MongoDB.

GET /HikeList/user/{userID} → Retrieves user data.

✅ Example Payload:

json
Copy
Edit
{
"userID": "123",
"name": "John Doe",
"lastModifiedTimeStamp": {
"$date": "2025-03-22T10:00:00Z"
}
}
🔗 3. Integrator Module
The Integrator Module acts as the orchestrator, coordinating between services.

✅ Features:

Service-to-service communication.

Error handling and retry mechanisms.

Aggregates and integrates data from multiple services.

🐳 Docker Setup
1. Docker Compose
   The platform uses Docker Compose to run services in isolated containers on a common network.

✅ Run the platform:

bash
Copy
Edit
docker-compose up -d
✅ Stop the platform:

bash
Copy
Edit
docker-compose down
2. Docker Configuration
   Ensure that the MongoDB container has persistent storage:

yaml
Copy
Edit
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
🚀 Building and Running the Platform
1. Build the Maven Project
   To build all modules, run:

bash
Copy
Edit
mvn clean install
2. Running Individual Modules Locally
   To run a module individually:

bash
Copy
Edit
# Platform Module
mvn spring-boot:run -pl platform

# Ingestor Service Module
mvn spring-boot:run -pl ingestor-service

# Integrator Module
mvn spring-boot:run -pl integrator-service
🔥 API Documentation
The platform uses OpenAPI 3.0 for documenting and testing APIs.

✅ Access Swagger UI:

bash
Copy
Edit
http://localhost:8081/swagger-ui.html
✅ Sample API Calls:

bash
Copy
Edit
# Insert User Data
curl -X POST "http://localhost:8081/HikeList/user/123" -H "Content-Type: application/json" -d '{
"userID": "123",
"name": "John Doe"
}'

# Get User Data
curl -X GET "http://localhost:8081/HikeList/user/123"
🔥 Environment Variables
MONGO_HOST: MongoDB container name (for Docker network communication)

MONGO_PORT: 27017 (default port)

SERVER_PORT: Service ports (8081, 8082, etc.)

JAVA_OPTS: JVM runtime parameters

✅ Contributing
To contribute:

Fork the repository.

Create a feature branch.

Commit your changes.

Push the changes and create a pull request.

🚀 Troubleshooting
MongoDB connection issues: Ensure the services are in the same Docker network.

Data persistence: Verify MongoDB volume mapping.

Container logs: Use docker logs <container-id> to debug issues.

🎯 Future Enhancements
Add Kafka for event-driven architecture.

Implement JWT-based authentication.

Create CI/CD pipeline using GitHub Actions.

💡 Author
Bharani Tharan Ragunathan
🔗 ForgeX Developer & Architect[README.md](integrator%2FREADME.md)