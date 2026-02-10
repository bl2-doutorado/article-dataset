# HVitOps API Documentation

This document describes the endpoints, authentication, and business rules for the HVitOps microservices ecosystem.

## 1. Authentication

All endpoints (except `/health`) require a JWT (JSON Web Token) in the `Authorization` header:

```
Authorization: Bearer <token>

```

### JWT Payload Example

```
{
  "sub": "1",
  "role": "PATIENT",
  "iat": 1234567890,
  "exp": 1234571490
}

```

**Secret Key**: `hvitops-secret-key-for-jwt-token-validation-2024`

## 2. API Gateway (Port 8080)

The Gateway acts as the entry point for all services.

### Health Check

`GET /health`

**Response**: `200 OK`

```
{
  "status": "UP"
}

```

## 3. Appointments Service (Port 8081)

### Create Appointment

`POST /appointments`

**Request Body**:

```
{
  "patientId": 1,
  "doctorId": 101,
  "scheduledAt": "2026-01-25T10:00:00",
  "notes": "Regular checkup"
}

```

**Response**: `201 Created`

### Other Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/appointments` | List all appointments |
| GET | `/appointments/{id}` | Get appointment by ID |
| GET | `/appointments/patient/{pId}` | List appointments by patient |
| GET | `/appointments/doctor/{dId}` | List appointments by doctor |
| PUT | `/appointments/{id}` | Update appointment |
| DELETE | `/appointments/{id}` | Cancel appointment (204 No Content) |

## 4. Laboratory Tests Service (Port 8082)

### Create Laboratory Test

`POST /laboratory-tests`

**Request Body**:

```
{
  "patientId": 1,
  "date": "2026-01-21",
  "status": "scheduled",
  "items": [
    {
      "testType": "Blood Glucose",
      "result": null,
      "unit": "mg/dL",
      "referenceRange": "70-100"
    }
  ]
}

```

**Response**: `201 Created`

## 5. Medical Records Service (Port 8083)

### Create Medical Record

`POST /records`

**Request Body**:

```
{
  "patientId": 1,
  "physicianId": 101,
  "date": "2026-01-21",
  "diagnosis": "Hypertension",
  "prescriptions": ["Lisinopril 10mg daily"],
  "clinicalNotes": "Patient presents with elevated blood pressure"
}

```

**Response**: `201 Created`

## 6. Notifications Service (Port 8084)

### Create Notification

`POST /notifications` (Authentication not required for this demo)

**Request Body**:

```
{
  "recipient": "patient@example.com",
  "subject": "Appointment Reminder",
  "body": "You have an appointment tomorrow at 10:00 AM",
  "type": "email"
}

```

## 7. Business Rules

### Appointments

- **Validation**: Past dates are not allowed for new schedules.
- **States**: `SCHEDULED`, `COMPLETED`, `CANCELLED`.
- **Visibility**: Limited to the specific physician and patient involved.

### Medical Records

- **Mandatory**: Diagnosis and prescriptions must be provided.
- **Access**: Only authorized physicians and the patient can access these records.

## 8. HTTP Status Codes

| Code | Description |
| --- | --- |
| 200 | **OK** - Request succeeded |
| 201 | **Created** - Resource created successfully |
| 204 | **No Content** - Request succeeded with no body |
| 401 | **Unauthorized** - Valid JWT token missing |
| 403 | **Forbidden** - Insufficient permissions |
| 404 | **Not Found** - Resource ID does not exist |
| 500 | **Internal Error** - Server-side issue |

## 9. Troubleshooting

- **401 Errors**: Check if the token has expired or if the secret key matches the one used by the gateway.
- **400 Errors**: Ensure the JSON payload follows the correct data types (e.g., ISO dates).
- **500 Errors**: Check the microservice logs using `docker logs <container_name>`.