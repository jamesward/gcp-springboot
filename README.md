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

gcloud services enable storage-component.googleapis.com --project=$PROJECT_ID

gsutil mb -p $PROJECT_ID gs://$PROJECT_ID-gcp-springboot-pubsub-pull

gcloud pubsub topics create bars --project=$PROJECT_ID

gcloud pubsub subscriptions create bars-pull --topic=bars --project=$PROJECT_ID

./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-pubsub-pull

# Run
docker run -it --network host \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eSPRING_PROFILES_ACTIVE=prod \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  gcp-springboot-pubsub-pull
```

Containerize & Store on GCR:
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-pubsub-pull
```
