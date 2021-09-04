GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

# todo: service account stuff

Or Command Line with Cloud Run source deploy (Google Cloud Buildpacks):
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud alpha run deploy gcp-springboot-metrics \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --no-cpu-throttling \
  --source=.
```

Or Command Line with Spring's Buildpacks:
```
export PROJECT_ID=YOUR_PROJECT_ID

./mvnw spring-boot:build-image \
  -Dspring-boot.build-image.imageName=gcr.io/$PROJECT_ID/gcp-springboot:metrics

docker push gcr.io/$PROJECT_ID/gcp-springboot:metrics

gcloud alpha run deploy gcp-springboot-metrics \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --no-cpu-throttling \
  --image=gcr.io/$PROJECT_ID/gcp-springboot:metrics
```
