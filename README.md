GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally:
```
./mvnw spring-boot:run
```

[http://localhost:8080/](http://localhost:8080/)

Containerize & Run Locally:
```
./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-postgres

# Start Postgres Container
docker run --rm -ePOSTGRES_PASSWORD=password -p5432:5432 --name my-postgres postgres:13.1

# Init Schema
docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  gcp-springboot-postgres \
  init

# psql
docker exec -it my-postgres psql -U postgres

# Run
docker run -it --network host \
  -eSPRING_R2DBC_URL=r2dbc:postgresql://localhost/postgres \
  -eSPRING_R2DBC_USERNAME=postgres \
  -eSPRING_R2DBC_PASSWORD=password \
  gcp-springboot-postgres
```

Containerize & Store on GCR:
```
mvn compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-postgres
```
