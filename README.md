GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally (Spanner Testcontainer):
```
./mvnw spring-boot:run
```

[http://localhost:8080/](http://localhost:8080/)

```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable spanner.googleapis.com \
  --project=$PROJECT_ID

gcloud spanner instances create spanner-instance \
  --config=regional-us-central1 \
  --nodes=1 \
  --description="A Spanner Instance" \
  --project=$PROJECT_ID

# db is created by init
#gcloud spanner databases create bars \
#  --instance=spanner-instance \
#  --project=$PROJECT_ID
```


Containerize & Run Locally:
```
export GOOGLE_APPLICATION_CREDENTIALS=YOUR_CREDS_FILE

./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-spanner

# Init DB
docker run -it \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  gcp-springboot-spanner \
  init

# Run App
docker run -it \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  -p8080:8080 \
  gcp-springboot-spanner
```

Containerize & Store on GCR:
```
mvn compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-spanner
```


Locally run init:
```
./mvnw spring-boot:run -Dspring-boot.run.arguments=init
```