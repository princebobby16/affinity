# Apache Camel Project

## Overview
This project is built using [Apache Camel](https://camel.apache.org/) and runs with [Gradle](https://gradle.org/). It integrates with external services including:
- **MinIO** (Object Storage)
- **RabbitMQ** (Message Broker)
- **PostgreSQL** (Database)

These dependencies are hosted in a separate repository and must be set up before running the application.

## Prerequisites
Ensure the following are installed on your system:
- **Java 17+**
- **Gradle**

## Setup Instructions

### 1. Configure External Dependencies
Since MinIO, RabbitMQ, and PostgreSQL are located in a different repository [https://github.com/princebobby16/billing-dependencies](https://github.com/princebobby16/billing-dependencies), ensure they are up and running before starting this project. 
If they are containerized, start them using Docker Compose in the appropriate repository.

### 2. Update `AFFINITY_HOME_DIR`
Before running the application, update the `AFFINITY_HOME_DIR` environment variable in the `run.sh` script to a valid directory on your machine:

```sh
export AFFINITY_HOME_DIR=/path/to/your/directory
```

### 3. Run the Application
To start the application, execute:
```sh
./run.sh
```

## Project Structure
```
├── src/                    # Source code
│   ├── main/
│   │   ├── java/           # Java source files
│   │   ├── resources/      # Configuration files
├── build.gradle            # Gradle build configuration
├── run.sh                  # Script to start the application
├── README.md               # This file
```

## Environment Variables
This project requires certain environment variables to be set for proper execution:
- `AFFINITY_PORT`: 9999 
- `AFFINITY_HOME_DIR`: /home/prince/personal/affinity 
- `ENVIRONMENT`: local 
- `AFFINITY_DATABASE_URI`: jdbc:postgresql://localhost:5432/affinity 
- `AFFINITY_DATABASE_USER`: postgres 
- `AFFINITY_DATABASE_PWD`: postgres 
- `AFFINITY_RABBITMQ_HOST`: 127.0.0.1 
- `AFFINITY_RABBITMQ_PORT`: 5672 
- `AFFINITY_RABBITMQ_USERNAME`: guest 
- `AFFINITY_RABBITMQ_PASSWORD`: guest 
- `AFFINITY_RABBITMQ_EXCHANGE`: billable-hours 
- `AFFINITY_RABBITMQ_QUEUE`: billable-hours 
- `AFFINITY_MINIO_PORT`: 9000 
- `AFFINITY_MINIO_HOST`: http://127.0.0.1
- `AFFINITY_MINIO_USERNAME`: minioadmin 
- `AFFINITY_MINIO_PASSWORD`: minioadmin 
- `AFFINITY_MINIO_INVOICE_BUCKET`: invoices 
- `AFFINITY_MINIO_BILLABLE_HOURS_BUCKET`: billable-hours

Example:
```sh
export AFFINITY_PORT=9999
export AFFINITY_HOME_DIR=/home/prince/personal/affinity
export ENVIRONMENT=local

export AFFINITY_DATABASE_URI="jdbc:postgresql://localhost:5432/affinity"
export AFFINITY_DATABASE_USER=postgres
export AFFINITY_DATABASE_PWD=postgres

export AFFINITY_RABBITMQ_PORT=5672
export AFFINITY_RABBITMQ_HOST=127.0.0.1
export AFFINITY_RABBITMQ_USERNAME=guest
export AFFINITY_RABBITMQ_PASSWORD=guest
export AFFINITY_RABBITMQ_EXCHANGE=billable-hours
export AFFINITY_RABBITMQ_QUEUE=billable-hours

export AFFINITY_MINIO_PORT=9000
export AFFINITY_MINIO_HOST=http://127.0.0.1
export AFFINITY_MINIO_USERNAME=minioadmin
export AFFINITY_MINIO_PASSWORD=minioadmin
export AFFINITY_MINIO_INVOICE_BUCKET=invoices
export AFFINITY_MINIO_BILLABLE_HOURS_BUCKET=billable-hours
```

## Troubleshooting
If you encounter any issues, ensure:
- All dependencies are correctly set up and running.
- Environment variables are correctly configured.
- The `run.sh` script has executable permissions: `chmod +x run.sh`

## License
This project is licensed under the MIT License.

