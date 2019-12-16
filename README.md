GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Or Command Line Jib:
```
export PROJECT_ID=YOUR_PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot:jib

gcloud run deploy gcp-springboot-jib \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --image=gcr.io/$PROJECT_ID/gcp-springboot:jib
```

Run Locally:
```
./mvnw spring-boot:run
```
