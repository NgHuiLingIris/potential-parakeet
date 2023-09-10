Install minikube from Step 1 here https://minikube.sigs.k8s.io/docs/start/
Open cmd, run `minikube start`
Run `minikube addons enable metrics-server`
Mount the volume with `minikube mount <Your Directory>\potential-parakeet\keycloak\themes:/host`
In a separate cmd tab, run `minikube dashboard`
In the current folder, run `kubectl apply -f keycloak-secrets.yaml,keycloak-deployment.yaml,keycloak-service.yaml,postgres-data-persistentvolumeclaim.yaml,postgres-deployment.yaml,postgres-service.yaml`
Run `minikube service keycloak -n default` to run the keycloak service
To test: Create a new realm and ensure that your custom theme is present