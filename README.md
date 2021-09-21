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
./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-jib

# Run
docker run -it -p8080:8080 gcp-springboot-jib
```

Containerize & Store on GCR:
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-jib
```

Run on GKE:
```
gcloud services enable container.googleapis.com \
  --project=$PROJECT_ID

gcloud container clusters create gcp-springboot \
  --zone=us-central1-c \
  --enable-ip-alias \
  --scopes=cloud-platform \
  --project=$PROJECT_ID

cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gcp-springboot-jib
spec:
  selector:
    matchLabels:
      run: gcp-springboot-jib
  replicas: 1
  template:
    metadata:
      labels:
        run: gcp-springboot-jib
    spec:
      containers:
      - name: gcp-springboot-jib
        image: gcr.io/$PROJECT_ID/gcp-springboot-jib
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gcp-springboot-jib-loadbalancer
spec:
  type: LoadBalancer
  externalTrafficPolicy: Cluster
  ports:
    - port: 80
      targetPort: 8080
  selector:
    run: gcp-springboot-jib
EOF
```