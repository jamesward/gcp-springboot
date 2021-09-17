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
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-postgres
```

Run on GKE:
```
gcloud services enable container.googleapis.com \
  --project=$PROJECT_ID

gcloud services enable servicenetworking.googleapis.com \
  --project=$PROJECT_ID

gcloud services enable sqladmin.googleapis.com \
  --project=$PROJECT_ID

gcloud services enable vpcaccess.googleapis.com \
  --project=$PROJECT_ID

declare id="kotlin-bars"

declare rand1=$(( ( RANDOM % 61 ) + 1 ))
declare rand2=$(( rand1 + 1 ))
declare rand3=$(( rand2 + 1 ))

gcloud compute networks create $id \
  --subnet-mode=custom \
  --project=$PROJECT_ID

gcloud compute networks subnets create $id \
  --network=$id \
  --range="10.$rand1.0.0/28" \
  --region=us-central1 \
  --project=$PROJECT_ID

gcloud container clusters create $id \
  --region=us-central1 \
  --enable-ip-alias \
  --subnetwork=$id \
  --project=$PROJECT_ID

gcloud container clusters create $id \
  --region=us-central1 \
  --enable-ip-alias \
  --create-subnetwork=name=$id \
  --project=$PROJECT_ID

gcloud compute addresses create $id \
  --global \
  --purpose=VPC_PEERING \
  --prefix-length=16 \
  --network=default \
  --project=$PROJECT_ID

gcloud services vpc-peerings connect \
  --ranges=$id \
  --network=default \
  --project=$PROJECT_ID

declare db_user=postgres
declare db_pass=$(dd bs=24 count=1 if=/dev/urandom status=none | base64 | tr +/ _.)
declare db_name=postgres

cat <<EOF> gcp-springboot-postgres-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: gcp-springboot-postgres-config
data:
  SPRING_R2DBC_USERNAME: $db_user
  SPRING_R2DBC_PASSWORD: $db_pass
EOF

declare operation=$(gcloud beta sql instances create $id \
  --database-version=POSTGRES_13 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --project=$PROJECT_ID \
  --root-password=$db_pass \
  --network=default \
  --no-assign-ip \
  --async \
  --format='value(name)')

gcloud beta sql operations wait $operation \
  --timeout=unlimited \
  --project=$PROJECT_ID

declare db_host=$(gcloud sql instances describe $id \
  --project=$PROJECT_ID \
  --format='value(ipAddresses.ipAddress)')

cat <<EOF>> gcp-springboot-postgres-config.yaml
  SPRING_R2DBC_URL: r2dbc:postgresql://$db_host/postgres
EOF

kubectl apply -f gcp-springboot-postgres-config.yaml

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: gcp-springboot-postgres-init
spec:
  containers:
  - name: gcp-springboot-postgres-init
    image: gcr.io/$PROJECT_ID/gcp-springboot-postgres
    args: ["init"]
    env:
    - name: SPRING_R2DBC_USERNAME
      valueFrom:
        configMapKeyRef:
          name: gcp-springboot-postgres-config
          key: SPRING_R2DBC_USERNAME
    - name: SPRING_R2DBC_PASSWORD
      valueFrom:
        configMapKeyRef:
          name: gcp-springboot-postgres-config
          key: SPRING_R2DBC_PASSWORD
    - name: SPRING_R2DBC_URL
      valueFrom:
        configMapKeyRef:
          name: gcp-springboot-postgres-config
          key: SPRING_R2DBC_URL
  restartPolicy: Never
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gcp-springboot-postgres
spec:
  selector:
    matchLabels:
      run: gcp-springboot-postgres
  replicas: 1
  template:
    metadata:
      labels:
        run: gcp-springboot-postgres
    spec:
      containers:
      - name: gcp-springboot-postgres
        image: gcr.io/$PROJECT_ID/gcp-springboot-postgres
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_R2DBC_USERNAME
          valueFrom:
            configMapKeyRef:
              name: gcp-springboot-postgres-config
              key: SPRING_R2DBC_USERNAME
        - name: SPRING_R2DBC_PASSWORD
          valueFrom:
            configMapKeyRef:
              name: gcp-springboot-postgres-config
              key: SPRING_R2DBC_PASSWORD
        - name: SPRING_R2DBC_URL
          valueFrom:
            configMapKeyRef:
              name: gcp-springboot-postgres-config
              key: SPRING_R2DBC_URL
---
apiVersion: v1
kind: Service
metadata:
  name: gcp-springboot-postgres-loadbalancer
spec:
  type: LoadBalancer
  externalTrafficPolicy: Cluster
  ports:
    - port: 80
      targetPort: 8080
  selector:
    run: gcp-springboot-postgres
EOF


```