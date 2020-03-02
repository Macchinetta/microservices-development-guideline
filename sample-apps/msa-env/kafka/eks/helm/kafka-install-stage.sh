#!/bin/sh

## Setup script

helm init
while [ "$(kubectl get pod -n stage -o jsonpath="{$.items[0].status.containerStatuses[0].ready}" -l app=helm)" != "true" ]; do echo "waiting..."; sleep 3; done
helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/
helm repo update
helm install --set cp-schema-registry.enabled=false,cp-kafka-rest.enabled=false,cp-kafka-connect.enabled=false confluentinc/cp-helm-charts --name m9amsa --namespace stage
while [ "$(kubectl get pod m9amsa-cp-kafka-2 -n stage -o jsonpath="{$.status.phase}")" != "Running" ]; do echo "waiting..."; sleep 3; done
