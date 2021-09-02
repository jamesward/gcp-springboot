GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Or Command Line:
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud run deploy gcp-springboot-structured-logging \
  --platform=managed \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --allow-unauthenticated \
  --source=.
```
