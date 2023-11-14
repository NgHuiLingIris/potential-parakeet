# potential-parakeet
Keycloak 21.1.0 Tutorial/ Boilerplate code

## To start
Run Docker Compose with `docker-compose up`

## To enable minikube/kubernetes deployment
1 Update and Deploy the secrets: `kubectl apply -f minikube-scripts-secret`

You can go cloud karafka for the test kafka cluster.

Note to encode it in base64


2 Follow the `setup.md` in minikube-scripts folder

3 Deploy the application: `kubectl apply -f minikube-scripts`
You can check that the application is working here

4 Deploy Prometheus: `kubectl apply -f prometheus-deployment`

5 Deploy node-exporter: `kubectl apply -f node-exporter`

6 Deploy cadvisor: `kubectl apply -f cadvisor`

7 Deploy Grafana: `kubectl apply -f grafana-deployment`


## References
You can check out the relevant articles here:

https://medium.com/@equinox.explores/keycloak-prometheus-and-grafana-5c06a38720e2

https://medium.com/mobile-app-circular/setting-up-local-ci-cd-with-keycloak-minikube-kubernetes-and-argocd-679617f58798
