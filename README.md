# Scoring Algorithm Refactoring Assignment

This project contains a refactored survey response scoring system for Serand.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose

## Quick Start

### 1. Start MongoDB using Docker

```bash
# Start MongoDB and Mongo Express
docker-compose up -d

# Verify containers are running
docker-compose ps
```

### 2. Build and Run the Application

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### 3. Access the Application

- **Spring Boot Application**: http://localhost:8080
- **MongoDB Express (Admin UI)**: http://localhost:8081
  - Username: `admin`
  - Password: `password`

## Project Structure

```
src/
├── main/
│   ├── java/com/serand/assessment/
│   │   ├── model/          # Data models
│   │   ├── service/        # Business logic services
│   │   ├── dto/           # Data Transfer Objects
│   │   └── ScoringApplication.java
│   └── resources/
│       └── application.yml # Application configuration
├── test/                   # Unit tests
└── docs/                   # Documentation and original code
```

## MongoDB Collections

The application uses the following MongoDB collections:
- `surveys` - Survey definitions
- `survey_responses` - Candidate survey responses
- `candidates` - Candidate information
- `applications` - Job applications
- `companies` - Company information

## Development

### Running Tests

```bash
mvn test
```

### Stopping MongoDB

```bash
docker-compose down
```

### Viewing Logs

```bash
# Application logs
mvn spring-boot:run

# MongoDB logs
docker-compose logs mongodb

# Mongo Express logs
docker-compose logs mongo-express
```

## Refactoring Status

- ✅ Project setup with Spring Boot
- ✅ MongoDB configuration
- ✅ Model classes created
- ✅ Mock services implemented
- 🔄 CentralScoringEngine (in progress)
- 🔄 ScoreCompositionService (pending)
- 🔄 SurveyResponseService refactoring (pending)
- 🔄 Unit tests (pending) 