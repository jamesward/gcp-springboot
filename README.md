GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally:
```
./mvnw spring-boot:run
```






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


# Kotlin Bars

<!-- [![Run on Google Cloud](https://deploy.cloud.run/button.png)](https://deploy.cloud.run) -->

## Local Dev

Run the server:
```
./gradlew bootRun
```

[http://localhost:8080/](http://localhost:8080/)

Create container & run with docker:
```
./gradlew bootBuildImage --imageName=kotlin-bars

docker run --rm -ePOSTGRES_PASSWORD=password -p5432:5432 --name my-postgres postgres:13.1

docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  kotlin-bars \
  init

# psql
docker exec -it my-postgres psql -U postgres

docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  kotlin-bars
```

[http://localhost:8080/](http://localhost:8080/)

