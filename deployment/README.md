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
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep eks-admin | awk '{print $1}')

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
    -n camunda-bpm-platform

# check the installation
helm list --all-namespaces
```

