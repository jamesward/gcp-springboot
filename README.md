GCP Spring Boot
-----------------

Or Command Line with Spring's Buildpacks:
```
export PROJECT_ID=YOUR_PROJECT_ID

./mvnw spring-boot:build-image \
  -Dspring-boot.build-image.imageName=gcr.io/$PROJECT_ID/gcp-springboot:graalvm

docker push gcr.io/$PROJECT_ID/gcp-springboot:graalvm

gcloud run deploy gcp-springboot-graalvm \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --image=gcr.io/$PROJECT_ID/gcp-springboot:graalvm
```
