export AFFINITY_PORT=8081
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

./gradlew bootRun
