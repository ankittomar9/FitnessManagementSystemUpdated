# Fitness Management System

A microservices-based fitness management platform that provides activity tracking, AI-powered recommendations, and user management.

## 📋 Table of Contents
- [Architecture Overview](#-architecture-overview)
  - [API Gateway](#api-gateway)
  - [Config Server](#config-server)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Service Details](#-service-details)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
  - [Gateway Configuration](#gateway-configuration)
  - [Config Server Setup](#config-server-setup)
- [Development](#-development)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)

## 🏗️ Architecture Overview

The system follows a microservices architecture with the following components:

### API Gateway
- **Purpose**: Serves as the single entry point for all client requests
- **Technology Stack**:
  - Spring Cloud Gateway (WebFlux-based)
  - Eureka Client for service discovery
  - Spring Cloud Config Client for externalized configuration
- **Key Features**:
  - Request routing to appropriate microservices
  - Load balancing between service instances
  - Cross-cutting concerns handling (CORS, security, monitoring)
  - Circuit breaking and fault tolerance

### Config Server
- **Purpose**: Centralized configuration management for all microservices
- **Technology Stack**:
  - Spring Cloud Config Server
  - Git-based configuration repository
  - Spring Boot 3.5.5
- **Key Features**:
  - Externalized configuration for all environments
  - Versioned configuration management
  - Encryption/decryption of sensitive properties
  - Support for different configuration profiles

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│   User Service  │◄───►│  API Gateway    │◄───►|  Web/Mobile    │
│                 │     │                 │     │    Clients      │
└────────┬────────┘     └────────┬────────┘     └─────────────────┘
         │                       │
         │                       │
┌────────▼────────┐    ┌────────▼────────┐
│                 │    │                 │
│  Activity       │    |  AI Service     |
|  Service        |    |  (Gemini AI)    |
│                 │    |                 |
└────────┬────────┘    └────────┬────────┘
         │                      │
         │        ┌─────────────┴─────────────┐
         │        │                           │
         └───────►│   Message Queue (RabbitMQ) │
                  │                           │
         ┌───────►│   Service Discovery      │
         │        │   (Eureka)               │
         │        └───────────────────────────┘
         │                      ▲
         ▼                      │
┌─────────────────┐    ┌────────┴────────┐
│                 │    │                 │
│   MongoDB       │    |   Config Server │
│   (Persistence) |    |   (Optional)    │
│                 │    │                 │
└─────────────────┘    └─────────────────┘
```

## 🚀 Prerequisites

Before running the application, ensure you have the following installed:

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- MongoDB 5.0+
- RabbitMQ 3.9+
- Git

## 🛠️ Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/FitnessManagementSystemUpdated.git
cd FitnessManagementSystemUpdated
```

### 2. Start the infrastructure
```bash
docker-compose up -d
```

### 3. Build and run services
```bash
# Build all services
mvn clean install

# Run Eureka Server
cd eureka && mvn spring-boot:run

# Run other services in separate terminals
cd ../userservice && mvn spring-boot:run
cd ../activityservice && mvn spring-boot:run
cd ../aiservice && mvn spring-boot:run
```

## 🔍 Service Details

### 1. AI Service
- **Port**: 8083
- **Description**: Processes fitness activities using Gemini AI to generate personalized recommendations.
- **Key Features**:
  - Listens to activity messages from RabbitMQ
  - Generates AI-powered fitness recommendations
  - Stores recommendations in MongoDB
  - Provides REST APIs to retrieve recommendations

### 2. Activity Service
- **Port**: 8081
- **Description**: Manages user fitness activities and related operations.

### 3. User Service
- **Port**: 8082
- **Description**: Handles user authentication, authorization, and profile management.

### 4. Eureka Server
- **Port**: 8761
- **Description**: Service discovery server for microservices.

## 📚 API Documentation

API documentation is available through Swagger UI when services are running:
- AI Service: http://localhost:8083/swagger-ui.html
- Activity Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html

## 🚀 Deployment

### Production Deployment
For production deployment, consider using:
- Kubernetes for container orchestration
- Config Server for centralized configuration
- Spring Cloud Gateway for API Gateway
- Monitoring with Prometheus and Grafana
- Logging with ELK Stack

## 🛠 Development

### Code Style
- Follow Google Java Style Guide
- Use Lombok annotations to reduce boilerplate
- Keep methods small and focused
- Write meaningful commit messages

### Branching Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes

## 🐛 Troubleshooting

### Common Issues
1. **Port Conflicts**: Ensure required ports are not in use
2. **Dependency Issues**: Run `mvn clean install -U`
3. **Database Connection**: Verify MongoDB is running and accessible
4. **RabbitMQ**: Check if RabbitMQ is running and the queue is created

### Logs
Check logs in:
- `logs/` directory for each service
- Docker container logs: `docker logs <container_id>`

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Built with ❤️ by the Fitness Management Team</p>
  <p>🏆 Best viewed with a healthy lifestyle</p>
</div>
