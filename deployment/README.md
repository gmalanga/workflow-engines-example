# Deployment

## Minikube

This requires:

- `minikube` installed and configured on yur machine
- `helm v.3` installed and configured on your machine
- `kubeclt` installed and configured on your machine

### 1.1 - Check the K8s status

```bash
kubectl cluster-info
# You should see something like:
# Kubernetes master is running at https://kubernetes.docker.internal:6443
# KubeDNS is running at https://kubernetes.docker.internal:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

kubectl get svc
# You should see something like:
# NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
# kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   96d
```

### 1.2 - Deploy the Kubernetes Web UI (Dashboard)

Reference: `https://github.com/kubernetes/dashboard/tree/master/aio/deploy/helm-chart/kubernetes-dashboard`

```bash
# Add kubernetes-dashboard repository
helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/

# Deploy a Helm Release named "my-release" using the kubernetes-dashboard chart
helm install kubernetes-dashboard/kubernetes-dashboard \
    --name-template kubernetes-dashboard \
    -f helm-files/values-kubernetes-dashboard.yaml \
    -n kube-system

# check the installation
helm list --all-namespaces
```

Get the token and start the proxy:

```bash
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep k8s-admin | awk '{print $1}')

kubectl proxy
```

Connect to `http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:https/proxy/#!/login`

### 1.3 - Deploy Camunda BPM Platform Helm Chart

Reference: `https://github.com/camunda/camunda-helm/tree/main/charts/camunda-bpm-platform`

```bash
# Add the camunda-bpm-platform repository
helm repo add camunda https://helm.camunda.cloud

# Update repos
helm repo update

# Create the k8s namespace camunda-bpm-platform
kubectl create namespace camunda-bpm-platform

# Install the camunda-bpm-platform
helm install camunda/camunda-bpm-platform \
    --name-template camunda \
    -f helm-files/values-camunda.yaml \
    -n camunda-bpm-platform

# check the installation
helm list --all-namespaces
```

#### 1.3.1 - Connect to Camunda

```bash
# Get the application URL by running these commands:
export CAMUNDA_POD_NAME=$(kubectl get pods --namespace camunda-bpm-platform -l "app.kubernetes.io/name=camunda-bpm-platform,app.kubernetes.io/instance=camunda" -o jsonpath="{.items[0].metadata.name}")
  
export CAMUNDA_CONTAINER_PORT=$(kubectl get pod --namespace camunda-bpm-platform $CAMUNDA_POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
  
kubectl --namespace camunda-bpm-platform port-forward $CAMUNDA_POD_NAME 8080:$CAMUNDA_CONTAINER_PORT
```

Visit `http://localhost:8080/camunda/` to use your application. The default credentials for admin access to the webapps is `demo/demo`.

#### 1.3.2 - Remove Camunda BPM Platform

```bash
# remove camunda
helm delete camunda -n camunda-bpm-platform

# check
helm list --all-namespaces

# remove the namespace
kubectl delete namespace camunda-bpm-platform

# check
kubectl get ns
```

### 1.4 - Deploy Flowable BPM Platform Helm Chart

Reference: `https://github.com/flowable/flowable-engine/tree/master/k8s`

```bash
# Add the flowable-bpm-platform repository
helm repo add flowable https://flowable.org/helm/

# Update repos
helm repo update

# Create the k8s namespace flowable-bpm-platform
kubectl create namespace flowable-bpm-platform

# Install the flowable-bpm-platform
helm install flowable/flowable \
         --name-template flowable \
         -f helm-files/values-flowable.yaml \
         -n flowable-bpm-platform

# check the installation
helm list --all-namespaces
```

#### 1.4.1 - Connect to Flowable

```bash
# Get the application URL by running these commands:
export FLOWABLE_POD_NAME=$(kubectl get pods --namespace flowable-bpm-platform -l "app.kubernetes.io/name=ui,app.kubernetes.io/instance=flowable" -o jsonpath="{.items[0].metadata.name}")
  
export FLOWABLE_CONTAINER_PORT=$(kubectl get pod --namespace flowable-bpm-platform $FLOWABLE_POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
  
kubectl --namespace flowable-bpm-platform port-forward $FLOWABLE_POD_NAME 9090:$FLOWABLE_CONTAINER_PORT
```

Visit `http://localhost:9090/flowable-ui/` to use your application. The default credentials for admin access to the webapps is `admin/test`.

#### 1.4.2 - Remove Flowable BPM Platform

```bash
# remove flowable
helm delete flowable -n flowable-bpm-platform

# check
helm list --all-namespaces

# remove the namespace
kubectl delete namespace flowable-bpm-platform

# check
kubectl get ns
```
