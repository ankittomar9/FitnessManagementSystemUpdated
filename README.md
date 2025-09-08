# Fitness Management System

A microservices-based fitness management platform that provides activity tracking, AI-powered recommendations, and user management.

## 📋 Table of Contents
- [Architecture Overview](#-architecture-overview)
  - [API Gateway](#api-gateway)
  - [Config Server](#config-server)
  - [Keycloak Authentication](#-keycloak-authentication)
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

## 🔐 Keycloak Authentication

The system uses Keycloak for authentication and authorization. Here's how it's integrated:

### Keycloak Server
- **Container**: Pre-configured in docker-compose.yml
- **Admin Console**: http://localhost:8181
- **Default Admin Credentials**:
  - Username: `admin`
  - Password: `admin`

### Keycloak Configuration
1. **Realm**: `fitness-oauth2` (pre-configured)
2. **Client**: `fitness-management-client`
3. **JWT Validation**:
   - Uses RS256 algorithm
   - Validates tokens against JWKS endpoint
   - Token issuer: `http://localhost:8181/realms/fitness-oauth2`

### Secured Endpoints
All endpoints except the following require a valid JWT token:
- `/actuator/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/webjars/**`

### Getting Access Token
```bash
curl -X POST 'http://localhost:8181/realms/fitness-oauth2/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=fitness-management-client' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user' \
  --data-urlencode 'password=password' \
  --data-urlencode 'client_secret=your-client-secret'
```

## 🚀 Prerequisites

Before running the application, ensure you have the following installed:

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- MongoDB 5.0+
- RabbitMQ 3.9+
- Keycloak 26.3.3 (managed via Docker)
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

### 3. Configure Keycloak (First Time Setup)
1. Access Keycloak Admin Console at http://localhost:8181
2. Log in with admin credentials (admin/admin)
3. Import the pre-configured realm (if available) or manually create:
   - Realm: `fitness-oauth2`
   - Client: `fitness-management-client`
   - Configure valid redirect URIs and web origins
   - Set up users and roles as needed

### 4. Build and run services
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

API documentation is available through Swagger UI when services are running. To access protected endpoints:

1. Get an access token from Keycloak
2. Click the "Authorize" button in Swagger UI
3. Enter: `Bearer <your_access_token>`

Available services:
- AI Service: http://localhost:8083/swagger-ui.html
- Activity Service: http://localhost:8081/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- API Gateway: http://localhost:8080/swagger-ui.html

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

## 🔑 Security Best Practices

1. **Keycloak Configuration**:
   - Change default admin credentials in production
   - Use HTTPS in production environments
   - Regularly rotate client secrets
   - Set appropriate token expiration times

2. **JWT Validation**:
   - Tokens are validated using the JWKS endpoint
   - Signature verification is enforced
   - Token expiration is checked

3. **Rate Limiting**:
   - Implement rate limiting on authentication endpoints
   - Monitor for suspicious login attempts

## 🐛 Troubleshooting

### Common Issues

#### Keycloak Issues
- **Connection Refused**: Ensure Keycloak container is running (`docker ps`)
- **Invalid Token**: Verify token audience and issuer match Keycloak configuration
- **CORS Errors**: Check allowed origins in Keycloak client settings

#### Authentication Issues
- **401 Unauthorized**: Verify token is valid and not expired
- **403 Forbidden**: Check user roles and permissions in Keycloak
- **Token Validation Failed**: Ensure JWKS endpoint is accessible and returning keys
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
