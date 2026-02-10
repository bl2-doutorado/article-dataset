# Deployment Guide - HVitOps Health Platform

## Requirements

- Docker 20.10+
- Docker Compose 2.0+
- Minimum 4GB of available RAM
- Minimum 10GB of disk space

## Quick Installation

### 1. Extract the project

```
unzip hvitops-platform.zip
cd hvitops-platform

```

### 2. Start the Services

```
docker-compose up -d

```

### 3. Wait for Initialization

Services take a few minutes to initialize. To verify the status:

```
docker-compose ps

```

All services should have the status "Up" and a "healthy" healthcheck.

### 4. Access the Platform

| Service | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Appointments | http://localhost:8081 |
| Laboratory Tests | http://localhost:8082 |
| Medical Records | http://localhost:8083 |
| Notifications | http://localhost:8084 |

## Health Verification

### Check Container Status

```
docker-compose ps

```

### Check Logs of a Specific Service

```
docker-compose logs -f [service-name]

```

### Test Gateway Connectivity

```
curl http://localhost:8080/health

```

### Test Notifications Connectivity

```
curl http://localhost:8084/health

```

## Seed Data

The system is automatically initialized with sample data:

- **Appointments**: 4 records
- **Laboratory Tests**: 3 records
- **Medical Records**: 3 records

This data is available immediately after initialization.

## Stopping the Services

### Stop keeping data

```
docker-compose stop

```

### Stop and remove containers

```
docker-compose down

```

### Stop and remove everything (including volumes)

```
docker-compose down -v

```

## Restarting Services

### Restart a specific Service

```
docker-compose restart [service-name]

```

### Restart all Services

```
docker-compose restart

```

## Troubleshooting

### Port already in use

If you receive a "port in use" error, change the port mapping in `docker-compose.yml`:

```
ports:
  - "3001:3000"  # Change 3000 to 3001

```

### Service fails to start

1. Check the logs: `docker-compose logs [service]`
2. Verify available disk space
3. Check file permissions

### Database Connection Error

1. Wait 30 seconds for the database to initialize
2. Verify if the database container is running
3. Restart the service: `docker-compose restart [service]`

### JWT Authentication Error

Verify if the JWT token is valid and has not expired. The secret key is:

```
hvitops-secret-key-for-jwt-token-validation-2024

```

## Monitoring

### View resource usage

```
docker stats

```

### View real-time logs

```
docker-compose logs -f

```

### Check connectivity between Services

```
docker-compose exec [service] curl http://[other-service]:port/health

```

## Backup and Recovery

### Backup PostgreSQL data

```
docker-compose exec postgres pg_dump -U hvitops_user hvitops_appointments > backup.sql

```

### Backup MongoDB data

```
docker-compose exec mongo mongodump --username hvitops_user --password hvitops_password --out /Backup

```

### Restore PostgreSQL data

```
docker-compose exec -T postgres psql -U hvitops_user hvitops_appointments < backup.sql

```

## Performance

### Increase Gateway Memory

In `docker-compose.yml`, modify:

```
environment:
  JAVA_OPTS: "-Xmx1024m -Xms512m"  # Increase from 512m to 1024m

```

### Increase Java Services Memory

Adjust `JAVA_OPTS` in each service as needed.

## Security

### Change Default Credentials

1. Edit `docker-compose.yml`
2. Change `POSTGRES_PASSWORD` and `MONGO_INITDB_ROOT_PASSWORD`
3. Restart services: `docker-compose down && docker-compose up -d`

### Change JWT Secret Key

1. Edit `docker-compose.yml`
2. Change `JWT_SECRET` in the gateway service
3. Restart the gateway: `docker-compose restart gateway`

## Scalability

### Increase Service replicas

```
docker-compose up -d --scale appointments=3

```

### Use Load Balancer (Nginx)

Add an Nginx service to `docker-compose.yml` to distribute load.

## Cleanup

### Remove unused images

```
docker image prune -a

```

### Remove unused volumes

```
docker volume prune

```

### Remove everything (Caution!)

```
docker system prune -a --volumes

```

## Support

For issues or questions, refer to:

- `README.md` - General documentation
- `API_DOCUMENTATION.md` - API documentation
- Service logs: `docker-compose logs`