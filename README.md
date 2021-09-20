GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally:
```
./mvnw spring-boot:run
```

[http://localhost:8080/](http://localhost:8080/)

Containerize & Run Locally (Cloud Pub/Sub):
```
export PROJECT_ID=YOUR_PROJECT_ID
export GOOGLE_APPLICATION_CREDENTIALS=YOUR_CREDS_FILE

gcloud services enable pubsub.googleapis.com --project=$PROJECT_ID

gcloud pubsub topics create bars --project=$PROJECT_ID

./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-pubsub-publisher

# Start Postgres Container
docker run --rm -ePOSTGRES_PASSWORD=password -p5432:5432 --name my-postgres postgres:13.1

# Init Schema
docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  gcp-springboot-pubsub-publisher \
  init

# psql
docker exec -it my-postgres psql -U postgres

# Run
docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  gcp-springboot-pubsub-publisher
```

Containerize & Store on GCR:
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-pubsub-publisher
```
