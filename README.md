GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Or Command Line with Cloud Run source deploy (Google Cloud Buildpacks):
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud run deploy gcp-springboot-main \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --source=.
```

Or Command Line with Spring's Buildpacks:
```
export PROJECT_ID=YOUR_PROJECT_ID

./mvnw spring-boot:build-image \
  -Dspring-boot.build-image.imageName=gcr.io/$PROJECT_ID/gcp-springboot:main

docker push gcr.io/$PROJECT_ID/gcp-springboot:main

gcloud run deploy gcp-springboot-main \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --image=gcr.io/$PROJECT_ID/gcp-springboot:main
```
