# HVitOps Health Platform

A complete healthcare platform based on microservices, built with modern technologies and scalable architectural patterns.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                      Frontend (React + Vite)                                    │
│                      Port: 3000                                                 │
└──────────────────────────────┬──────────────────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────────────────┐
│                    API Gateway (Spring Boot)                                    │
│                      Port: 8080                                                 │
│              JWT Authentication & Routing                                       │
└──┬──────────────────────────┬─────────────────┬──────────────────────┬──────────┘
   │                          │                 │                      │           
   ▼                          ▼                 ▼                      ▼           
┌─────────────────┐ ┌──────────────────┐  ┌──────────────────┐ ┌──────────────────┐  
│  Appointments   │ │       Auth       │  │ Laboratory Tests │ │ Medical Records  │  
│  (Spring Boot)  │ │  (Spring Boot)   │  │    (Quarkus)     │ │   (Micronaut)    │  
│   Port: 8081    │ |   Port: 8085     |  │   Port: 8082     │ │   Port: 8083     │  
└────────┬────────┘ └─────┬────────────┘  └────────┬─────────┘ └────────┬─────────┘  
         │                │                        │                    │
         ▼                ▼                        ▼                    ▼ 
    ┌─────────────────────────┐              ┌────────────┐       ┌───────────┐
    │     PostgreSQL          │              │ MongoDB    │       │ MongoDB   │
    │     Port: 5432          │              │Port: 27017 │       │Port: 27017│
    └─────────────────────────┘              └────────────┘       └───────────┘

┌──────────────────────────────────────────────────────────────────┐
│              Notifications Service (Node.js + Express)           │
│                      Port: 8084                                  │
│                    Redis Queue (Port: 6379)                      │
└──────────────────────────────────────────────────────────────────┘

```

## Components

### 1. **hvitops-web** (Frontend)

- **Stack**: React 19 + Vite + TypeScript + TailwindCSS
- **Manager**: NPM/PNPM
- **Port**: 3000
- **Features**:
  - Login with JWT authentication
  - Appointments dashboard
  - Laboratory test management
  - Medical records history
  - Appointment scheduling

### 2. **hvitops-gateway** (API Gateway)

- **Stack**: Java 21 + Spring Boot 3 + Spring Cloud Gateway
- **Manager**: Maven
- **Port**: 8080
- **Features**:
  - Centralized JWT authentication
  - Request routing
  - Token validation
  - Reverse proxy for services

### 3. **hvitops-appointments** (Appointments Service)

- **Stack**: Java 21 + Spring Boot 3 + Gradle
- **Database**: PostgreSQL
- **Port**: 8081
- **Features**:
  - Appointments CRUD
  - Date validation (prevents past appointments)
  - Filtering by patient or doctor
  - Statuses: SCHEDULED, COMPLETED, CANCELLED

### 4. **hvitops-laboratory-tests** (Lab Tests Service)

- **Stack**: Java 17 + Quarkus + Gradle
- **Database**: MongoDB
- **Port**: 8082
- **Features**:
  - Lab tests CRUD
  - Search by patient
  - Statuses: scheduled, pending_results, completed
  - Test results storage

### 5. **hvitops-records** (Medical Records Service)

- **Stack**: Java 21 + Micronaut + Maven
- **Database**: MongoDB
- **Port**: 8083
- **Features**:
  - Medical records CRUD
  - Search by patient or physician
  - Diagnosis and prescription storage
  - Clinical notes

### 6. **hvitops-notifications** (Notifications Service)

- **Stack**: Node.js + Express + Yarn
- **Message Queue**: Redis
- **Port**: 8084
- **Features**:
  - Notification queue
  - Email sending simulation
  - Asynchronous notification processing

### 7. **hvitops-auth** (Authentication Service)

- **Stack**: Java 21 + Spring Boot 3 + Gradle
- **Database**: PostgreSQL
- **Port**: 8085
- **Features**:
  - User authentication

## Databases

### PostgreSQL

- **Port**: 5432
- **User**: hvitops_user
- **Password**: hvitops_password
- **Database**: hvitops_appointments
- **Tables**: appointments

### MongoDB

- **Port**: 27017
- **User**: hvitops_user
- **Password**: hvitops_password
- **Databases**:
  - hvitops_laboratory (lab_tests)
  - hvitops_records (medical_records)

### Redis

- **Port**: 6379
- **Use**: Notification queue

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Git

## Installation and Execution

### 1. Clone or extract the project

```
cd hvitops-platform

```

### 2. Start all services

```
docker-compose up -d

```

This command will:

- Create and start all containers
- Initialize databases with seed data
- Configure communication networks between services

### 3. Verify service status

```
docker-compose ps

```

### 4. Access the services

| Service | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Appointments | http://localhost:8081 |
| Laboratory Tests | http://localhost:8082 |
| Medical Records | http://localhost:8083 |
| Notifications | http://localhost:8084 |
| PostgreSQL | localhost:5432 |
| MongoDB | localhost:27017 |
| Redis | localhost:6379 |

## Seed Data

The system is initialized with example data:

### Users

- **Patients**: IDs 1, 2, 3
- **Doctors**: IDs 101, 102
- **Lab Technicians**: IDs 201, 202

### Appointments

- 4 example appointments with different statuses

### Lab Tests

- 3 tests in different states (scheduled, pending_results, completed)

### Medical Records

- 3 records with diagnoses and prescriptions

## Authentication

The system uses **JWT (JSON Web Tokens)** for authentication:

```
Secret Key: hvitops-secret-key-for-jwt-token-validation-2024

```

### JWT Token Example

```
{
  "sub": "1",
  "role": "PATIENT",
  "iat": 1234567890,
  "exp": 1234571490
}

```

## Stopping the Services

```
docker-compose down

```

To also remove data volumes:

```
docker-compose down -v

```

## Logs

View logs for a specific service:

```
docker-compose logs -f [service-name]

```

## Local Development

### Build a specific service

```
# Gateway
cd hvitops-gateway
mvn clean package

# Appointments
cd hvitops-appointments
gradle build

# Laboratory Tests
cd hvitops-laboratory-tests
gradle build

# Records
cd hvitops-records
mvn clean package

# Notifications
cd hvitops-notifications
npm install

```

## Directory Structure

```
hvitops-platform/
├── hvitops-web/                    # React Frontend
├── hvitops-gateway/                # API Gateway
├── hvitops-appointments/           # Appointments Service
├── hvitops-laboratory-tests/       # Lab Tests Service
├── hvitops-records/                # Medical Records Service
├── hvitops-notifications/          # Notifications Service
├── docker-config/
│   ├── postgres/
│   │   └── init.sql               # PostgreSQL initialization script
│   └── mongodb/
│       └── init.js                # MongoDB initialization script
├── docker-compose.yml             # Container orchestration
└── README.md                      # This file

```

## Design Patterns

### Microservices

- Each service is independent and scalable
- Synchronous communication via REST/HTTP
- Database per Service pattern

### Security

- Centralized authentication at the Gateway
- JWT for token validation
- Data isolation per service

## License

MIT