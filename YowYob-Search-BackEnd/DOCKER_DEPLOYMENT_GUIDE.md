# 🐳 Docker Deployment Guide - YowYob Search Backend

## Overview

Complete Docker containerization of YowYob Search Backend platform with all 8 microservices and 7 infrastructure services.

**Total Services:** 15 (8 microservices + 7 infrastructure)
**Configuration Status:** ✅ Fully validated and ready for deployment
**Deployment Command:** `docker-compose up -d`

---

## Microservices (8 services)

All microservices use **multi-stage Maven builder + Alpine JRE** pattern for optimized image sizes.

| Service | Port | Dockerfile | Status |
|---------|------|-----------|--------|
| API Gateway | 8080 | ✅ Created | Routing, JWT Auth |
| Auth Service | 8081 | ✅ Created | Authentication |
| Search Service | 8082 | ✅ Created | Keyword parsing, proximity search |
| Listing Service | 8083 | ✅ Created | Product listings |
| User Service | 8084 | ✅ Created | User management |
| Geo Service | 8085 | ✅ Created | IP geolocation, geocoding |
| Crawler Service | 8086 | ✅ Created | Web crawling |
| Notification Service | 8087 | ✅ Created | RabbitMQ messaging |

### Dockerfile Pattern

All microservices follow this pattern:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE <PORT>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- Build dependencies (Maven) excluded from final image
- Optimized image size (~300-400 MB per service)
- Alpine base reduces vulnerability surface
- Fast startup with pre-built JARs

---

## Infrastructure Services (7 services)

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL 15 | 5432 | Relational database for Auth, User, Listing services |
| Elasticsearch 8.11.0 | 9200 | Full-text search engine with synonym support |
| Redis 7 | 6379 | Distributed caching + IP geolocation cache |
| RabbitMQ 3 | 5672, 15672 | Message queue for async notifications |
| Prometheus | 9090 | Metrics collection from all services |
| Loki | 3100 | Log aggregation |
| Grafana | 3000 | Monitoring dashboard + alerts |

**Additional Infrastructure:**
- Zookeeper (2181): Kafka coordination
- Kafka (9092): Event streaming
- Promtail: Log forwarder to Loki
- Tempo (3200, 4318): Distributed tracing

---

## Configuration Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | 329 lines, 15 services, all dependencies configured |
| `prometheus.yml` | Scrape configs for metrics collection |
| `loki-config.yaml` | Log storage and ingestion config |
| `promtail-config.yaml` | Log collection from /var/log/app |
| `tempo-config.yaml` | Distributed tracing config |

---

## Quick Start

### 1. Validate Configuration
```bash
cd /home/rouchda-yampen/Bureau/YowYob-Search-BackEnd
docker-compose config --quiet
# Output: (no error = valid)
```

### 2. Start All Services
```bash
docker-compose up -d
```

### 3. Check Service Status
```bash
docker-compose ps
# Lists all 15 running services
```

### 4. View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f search-service

# Last 50 lines
docker-compose logs --tail=50 api-gateway
```

### 5. Test API Gateway
```bash
# Health check
curl http://localhost:8080/api/search/health

# Search endpoint
curl "http://localhost:8080/api/search?type=restaurant&keyword=resto"

# Proximity search
curl "http://localhost:8080/api/search/proximity?referenceCity=Douala&proximityRadius=5"

# Near-me with coordinates
curl "http://localhost:8080/api/search/near-me?latitude=4.0511&longitude=9.7679"

# Near-me with IP geolocation
curl "http://localhost:8080/api/search/near-me?ip=203.0.113.42"
```

### 6. Access Monitoring Dashboards
```
- Grafana:     http://localhost:3000
- Prometheus:  http://localhost:9090
- Elasticsearch: http://localhost:9200
- RabbitMQ:    http://localhost:15672 (guest/guest)
```

### 7. Stop All Services
```bash
docker-compose down

# Also remove volumes
docker-compose down -v
```

---

## Environment Variables

Each service has environment variables configured in `docker-compose.yml`:

### Database Services
```yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/yowyob_*
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Elasticsearch
```yaml
SPRING_ELASTICSEARCH_REST_URIS=http://elasticsearch:9200
```

### Redis
```yaml
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

### Inter-Service Communication
```yaml
GEO_SERVICE_URL=http://geo-service:8085
SPRING_RABBITMQ_HOST=rabbitmq
```

### JWT Security
```yaml
JWT_SECRET=your-super-secret-jwt-key-change-in-production
```

---

## Networking

All services are connected via `app-network` bridge network:

```yaml
networks:
  app-network:
    driver: bridge
```

This enables:
- Service-to-service DNS resolution (e.g., `http://search-service:8082`)
- Port isolation (ports only exposed to host, not between containers)
- Network security boundaries

---

## Volumes

| Volume | Mount Point | Purpose |
|--------|-------------|---------|
| `postgres_data` | `/var/lib/postgresql/data` | PostgreSQL persistence |
| `elasticsearch_data` | `/usr/share/elasticsearch/data` | Elasticsearch index storage |
| `grafana_data` | `/var/lib/grafana` | Grafana dashboard configs |
| `tempo_data` | `/tmp/tempo` | Trace storage |
| `loki_data` | `/loki` | Log storage |
| `app-logs` | `/var/log/app` | Application logs |

**Note:** All data persists between restarts. Use `docker-compose down -v` to clean up.

---

## Service Dependencies

Dependency order (what needs to run before what):

```
1. PostgreSQL, Elasticsearch, Redis, RabbitMQ (base services)
2. Auth-Service, Listing-Service, User-Service (depend on PostgreSQL)
3. Search-Service (depends on Elasticsearch + Geo-Service)
4. Geo-Service (depends on Redis)
5. Notification-Service (depends on RabbitMQ)
6. Crawler-Service (depends on Elasticsearch)
7. API-Gateway (depends on all services)
8. Monitoring (Prometheus, Loki, Grafana, Tempo)
```

Docker Compose automatically handles this order via `depends_on` constraints.

---

## Troubleshooting

### Service fails to start
```bash
# Check logs
docker-compose logs -f <service-name>

# Verify configuration
docker-compose config | grep -A 20 "<service-name>:"

# Check port conflicts
docker ps | grep <port-number>
```

### Database connection issues
```bash
# Verify postgres is running
docker-compose exec postgres psql -U postgres -c "SELECT 1"

# Check database exists
docker-compose exec postgres psql -U postgres -l
```

### Elasticsearch issues
```bash
# Check cluster health
curl http://localhost:9200/_cluster/health

# List indices
curl http://localhost:9200/_cat/indices?v

# Verify synonyms loaded
curl http://localhost:9200/products/_settings
```

### Redis connection issues
```bash
# Test connection
docker-compose exec redis redis-cli ping

# Check keys
docker-compose exec redis redis-cli keys "*"
```

### View container resource usage
```bash
docker stats

# Or specific container
docker stats search-service
```

---

## Production Deployment Checklist

- [ ] Change `JWT_SECRET` to secure random value
- [ ] Update database passwords from defaults
- [ ] Configure SMTP credentials for email alerts
- [ ] Set up persistent volume backups (postgres_data, elasticsearch_data)
- [ ] Enable resource limits in docker-compose.yml
- [ ] Set up reverse proxy (nginx) in front of API Gateway
- [ ] Configure HTTPS/TLS certificates
- [ ] Set up monitoring alerts in Grafana
- [ ] Configure log rotation for /var/log/app
- [ ] Enable authentication for Grafana and Prometheus
- [ ] Set resource quotas and memory limits
- [ ] Configure health checks for all services
- [ ] Test disaster recovery procedures
- [ ] Set up automated backups

---

## Performance Tuning

### Elasticsearch
```yaml
environment:
  - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase from 512m for larger datasets
```

### PostgreSQL
```yaml
environment:
  - POSTGRES_INITDB_ARGS=-c max_connections=200
```

### Service JVM Options
Add to Dockerfile or docker-compose environment:
```yaml
JAVA_OPTS: "-Xmx512m -Xms256m -XX:+UseG1GC"
```

---

## File Sizes (Multi-stage Build Optimization)

Typical image sizes with multi-stage builds:
- Build stage: ~1.2 GB (Maven cache)
- Final stage: ~300-400 MB per service (JRE + JAR)
- Total footprint: ~3.5 GB for all 8 microservices

---

## Next Steps

1. **Run Full Stack**
   ```bash
   docker-compose up -d
   ```

2. **Initialize Elasticsearch**
   ```bash
   bash seed_elasticsearch.sh
   ```

3. **Verify All Services**
   ```bash
   docker-compose ps
   # All should show "Up"
   ```

4. **Test API Gateway**
   ```bash
   curl http://localhost:8080/api/search/health
   ```

5. **Access Monitoring**
   ```
   http://localhost:3000 (Grafana)
   http://localhost:9090 (Prometheus)
   ```

---

**Created:** 2024
**Version:** Docker Compose 3.8
**Author:** YowYob Development Team
**Last Updated:** Complete Docker Containerization Phase
