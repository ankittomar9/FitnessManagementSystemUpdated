# Fitness Management System

A microservices-based fitness management platform that provides activity tracking, AI-powered recommendations, and user management with a modern React-based frontend.

## 📋 Table of Contents
- [Architecture Overview](#-architecture-overview)
  - [Frontend Application](#frontend-application)
  - [API Gateway](#api-gateway)
  - [Config Server](#config-server)
  - [Keycloak Authentication](#-keycloak-authentication)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Frontend Development](#-frontend-development)
- [Service Details](#-service-details)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
- [Development](#-development)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)

## 🏗️ Architecture Overview

The system follows a microservices architecture with the following components:

## 🖥️ Frontend Application

The frontend is a modern, responsive React application built with Vite, featuring a clean and intuitive user interface for managing fitness activities and user profiles.

### Technology Stack
- **Framework**: React 18 with Vite
- **State Management**: Redux Toolkit
- **UI Components**: Material-UI (MUI) with Emotion
- **Routing**: React Router
- **HTTP Client**: Axios
- **Authentication**: OAuth2 with PKCE
- **Date Handling**: date-fns

### Key Features
- **Activity Management**: View, create, and track fitness activities
- **User Authentication**: Secure login with Keycloak integration
- **Responsive Design**: Works on desktop and mobile devices
- **Real-time Updates**: Dynamic UI updates using React hooks
- **Form Validation**: Client-side form validation

### Project Structure
```
fitness-app-frontend/
├── public/              # Static files
├── src/
│   ├── assets/          # Images, fonts, etc.
│   ├── components/      # Reusable UI components
│   │   ├── Activity.jsx
│   │   ├── ActivityDetail.jsx
│   │   ├── ActivityForm.jsx
│   │   └── ActivityList.jsx
│   ├── services/        # API services
│   ├── store/           # Redux store configuration
│   ├── App.jsx          # Main application component
│   └── main.jsx         # Application entry point
└── ...
```

### Getting Started with Frontend

1. Navigate to the frontend directory:
   ```bash
   cd fitness-app-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:5173
   ```

### Environment Variables
Create a `.env` file in the frontend root with the following variables:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_KEYCLOAK_URL=your-keycloak-url
VITE_KEYCLOAK_REALM=your-realm
VITE_KEYCLOAK_CLIENT_ID=your-client-id
```

### Available Scripts
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## 🚀 API Gateway

The API Gateway serves as the single entry point for all client requests, handling routing, load balancing, security, and user synchronization with Keycloak.

### Technology Stack
- **Framework**: Spring Cloud Gateway (WebFlux-based, non-blocking)
- **Service Discovery**: Eureka Client for dynamic service lookup
- **Configuration**: Spring Cloud Config Client for centralized configuration
- **Communication**: Reactive WebClient for non-blocking service-to-service calls
- **Security**: Spring Security with JWT validation and OAuth2 Resource Server
- **Authentication**: Keycloak integration for identity and access management

### Key Components

#### 1. User Service Integration
Handles all user-related operations by communicating with the User Service.

**Key Classes**:
- `UserService`: Reactive service for user operations with circuit breaker pattern
- `UserResponse`: Secure DTO for user data responses with sensitive data protection
- `RegisterRequest`: Validated DTO for user registration requests
- `WebClientConfig`: Configures WebClient with load balancing and logging

**Endpoints**:
- `POST /api/users/register`: Register a new user with Keycloak integration
- `GET /api/users/{userId}/validate`: Validate if a user exists in the system

#### 2. Security Configuration (`SecurityConfig`)
- **JWT Token Validation**: Validates tokens against Keycloak realm
- **CORS Configuration**: Pre-configured for frontend origin (`http://localhost:5173`)
- **CSRF Protection**: Disabled for stateless JWT authentication
- **Request Authorization**: All endpoints require authentication by default

#### 3. Keycloak User Synchronization (`KeycloakUserSyncFilter`)
- Automatically syncs Keycloak users with the local user database
- Extracts user details from JWT tokens
- Handles new user registration if not found in the system
- Adds `X-User-ID` header to downstream requests

### Security Features
- **JWT Validation**: Validates tokens using Keycloak's public keys
- **CORS Protection**: Strict origin and method restrictions
- **Secure Headers**: Only allows specific headers (`Authorization`, `Content-Type`, `X-User-ID`)
- **Credential Handling**: Secure password management with dummy values for OAuth2 flows

### Logging Strategy
- **Request/Response Logging**: Detailed logging of API calls with sensitive data masking
- **Error Logging**: Multi-level logging (DEBUG, INFO, WARN, ERROR)
- **Security Events**: Logs authentication and authorization events
- **Performance Metrics**: Logs request processing times and service latencies

### Error Handling
- **Validation Errors**: 400 Bad Request for invalid inputs
- **Authentication Errors**: 401 Unauthorized for invalid/missing tokens
- **Authorization Errors**: 403 Forbidden for insufficient permissions
- **Service Errors**: 5xx errors with appropriate fallbacks

### Performance Optimization
- **Non-blocking I/O**: Full reactive stack for high concurrency
- **Connection Pooling**: Optimized WebClient configuration
- **Timeouts**: Configurable timeouts for service calls
- **Circuit Breaker**: Resilience patterns for fault tolerance
- **Caching**: Response caching where appropriate

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

## 🔐 Keycloak Integration

The gateway integrates with Keycloak for authentication and user management, providing a seamless SSO experience.

### Key Features
- **JWT Token Validation**: Validates tokens using Keycloak's public keys
- **User Synchronization**: Automatically creates local user profiles from Keycloak
- **Role-Based Access Control**: Integrates with Keycloak roles and permissions
- **Token Propagation**: Forwards user identity to downstream services

### User Flow
1. Client authenticates with Keycloak and receives JWT
2. Gateway validates JWT and extracts user information
3. If user doesn't exist locally, creates a new user profile
4. Adds `X-User-ID` header to downstream requests
5. Proxies request to appropriate microservice

### Configuration
Keycloak settings are managed through environment variables:
```properties
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/fitness-realm
KEYCLOAK_RESOURCE=fitness-client
KEYCLOAK_CREDENTIALS_SECRET=your-client-secret
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

### Frontend Development

#### Adding New Dependencies
```bash
# Add a production dependency
npm install package-name

# Add a development dependency
npm install --save-dev package-name
```

#### Code Style
- Follow the existing component structure
- Use functional components with hooks
- Keep components small and focused
- Use meaningful component and variable names
- Add PropTypes for component props

#### State Management
- Use Redux for global state
- Keep local component state for UI-specific state
- Use Redux Toolkit's `createSlice` for reducers

#### Testing
Run the test suite:
```bash
npm test
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Built with ❤️ by the Fitness Management Team</p>
  <p>🏆 Best viewed with a healthy lifestyle</p>
</div>
