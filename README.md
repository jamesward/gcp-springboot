GCP Spring Boot
-----------------

[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)

Run Locally:
```
./mvnw spring-boot:run
```

```
curl localhost:8080 -H "Content-Type: application/json" -d '{"message": {"data":"eyJpZCI6IDEsICJuYW1lIjogImZvbyJ9"}}'
```

Containerize & Run Locally (Cloud Datastore):
```
export PROJECT_ID=YOUR_PROJECT_ID
export GOOGLE_APPLICATION_CREDENTIALS=YOUR_CREDS_FILE

gcloud app create --region=us-central --project=$PROJECT_ID
gcloud datastore databases create --region=us-central --project=$PROJECT_ID

./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-pubsub-push

docker run -it -p8080:8080 \
  -eGOOGLE_CLOUD_PROJECT=$PROJECT_ID \
  -eGOOGLE_APPLICATION_CREDENTIALS=/certs/svc_account.json \
  -v$GOOGLE_APPLICATION_CREDENTIALS:/certs/svc_account.json \
  gcp-springboot-pubsub-push
```

Create Push Subscription and Connect via ngrok:
```
ngrok http 8080
```

 
```
export PROJECT_ID=YOUR_PROJECT_ID
export PUSH_ENDPOINT=YOUR_HTTPS_NGROK_ENDPOINT

gcloud pubsub subscriptions create bars-push --topic=bars --push-endpoint=$PUSH_ENDPOINT --project=$PROJECT_ID
```



Containerize & Store on GCR:
```
export PROJECT_ID=YOUR_PROJECT_ID

gcloud services enable containerregistry.googleapis.com \
  --project=$PROJECT_ID

./mvnw compile jib:build -Dimage=gcr.io/$PROJECT_ID/gcp-springboot-pubsub-push

gcloud compute addresses create gcp-springboot-pubsub-push --global --project=$PROJECT_ID

declare IP=$(gcloud compute addresses describe gcp-springboot-pubsub-push --global --format='value(address)' --project=$PROJECT_ID)

cat <<EOF | kubectl apply -f -
apiVersion: networking.gke.io/v1
kind: ManagedCertificate
metadata:
  name: managed-cert
spec:
  domains:
    - $IP.nip.io
EOF

kubectl describe managedcertificate managed-cert

cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gcp-springboot-pubsub-push
spec:
  selector:
    matchLabels:
      run: gcp-springboot-pubsub-push
  replicas: 1
  template:
    metadata:
      labels:
        run: gcp-springboot-pubsub-push
    spec:
      containers:
      - name: gcp-springboot-pubsub-push
        image: gcr.io/$PROJECT_ID/gcp-springboot-pubsub-push:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gcp-springboot-pubsub-push
spec:
  selector:
    run: gcp-springboot-pubsub-push
  type: NodePort
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: managed-cert-ingress
  annotations:
    kubernetes.io/ingress.global-static-ip-name: gcp-springboot-pubsub-push
    networking.gke.io/managed-certificates: managed-cert
    kubernetes.io/ingress.class: "gce"
spec:
  defaultBackend:
    service:
      name: gcp-springboot-pubsub-push
      port:
        number: 80
EOF

kubectl get ingress

gcloud pubsub subscriptions create bars-push --topic=bars --push-endpoint=https://$IP.nip.io --project=$PROJECT_ID

```
