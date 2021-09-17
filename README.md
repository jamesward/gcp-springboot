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
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-spanner
```

Run on GKE:
```
gcloud services enable container.googleapis.com \
  --project=$PROJECT_ID

gcloud container clusters create $id \
  --zone=us-central1-c \
  --enable-ip-alias \
  --create-subnetwork=name=$id \
  --scopes=cloud-platform \
  --project=$PROJECT_ID

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: gcp-springboot-spanner-init
spec:
  containers:
  - name: gcp-springboot-spanner-init
    image: gcr.io/$PROJECT_ID/gcp-springboot-spanner
    args: ["init"]
  restartPolicy: Never
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gcp-springboot-spanner
spec:
  selector:
    matchLabels:
      run: gcp-springboot-spanner
  replicas: 1
  template:
    metadata:
      labels:
        run: gcp-springboot-spanner
    spec:
      containers:
      - name: gcp-springboot-spanner
        image: gcr.io/$PROJECT_ID/gcp-springboot-spanner
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gcp-springboot-spanner-loadbalancer
spec:
  type: LoadBalancer
  externalTrafficPolicy: Cluster
  ports:
    - port: 80
      targetPort: 8080
  selector:
    run: gcp-springboot-spanner
EOF
```


Locally run init:
```
./mvnw spring-boot:run -Dspring-boot.run.arguments=init
```