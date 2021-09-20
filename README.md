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

Containerize & Run Locally:
```
./mvnw compile jib:dockerBuild -Dimage=gcp-springboot-push

docker run -it -p8080:8080 \
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
```
