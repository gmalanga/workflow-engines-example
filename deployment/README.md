# Deployment

## Minikube

This requires:

- `minikube` installed and configured on yur machine (use the Docker Desktop installation)
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

Reference: https://github.com/kubernetes/dashboard/tree/master/aio/deploy/helm-chart/kubernetes-dashboard

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

Connect to http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:https/proxy/#!/login

### 1.3 - Deploy the ingress-nginx

```bash
# add the repository
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

# install the ingress in the default namespace
helm install ingress-nginx ingress-nginx/ingress-nginx 

# check the installation
helm list --all-namespaces
```

#### 1.3.1 - Remove the ingress-nginx

```bash
helm delete ingress-nginx

```

### 1.4 - Deploy Camunda BPM Platform using Helm Chart

Reference: https://github.com/camunda/camunda-helm/tree/main/charts/camunda-bpm-platform

```bash
# Add the camunda-bpm-platform repository
helm repo add camunda https://helm.camunda.cloud

# Update repos
helm repo update

# Create the k8s namespace camunda-bpm-platform
kubectl create namespace camunda

# Install the camunda-bpm-platform
helm install camunda/camunda-bpm-platform \
    --name-template camunda \
    -f helm-files/values-camunda.yaml \
    -n camunda

# check the installation
helm list --all-namespaces
```

#### 1.4.1 - Connect to Camunda

```bash
# Add kubernetes.docker.internal hostname to your hosts file
sudo echo "127.0.0.1        kubernetes.docker.internal" > /etc/hosts

# deploy the camunda ingress
kubectl apply -f helm-files/values-camunda-ingress.yaml

```

Visit http://kubernetes.docker.internal/camunda/ to use the application. The default credentials for admin access to the webapps is `demo/demo`.

```bash
# Workaround if the ingress-nginx does not work

# Get the application URL by running these commands:
export CAMUNDA_POD_NAME=$(kubectl get pods --namespace camunda -l "app.kubernetes.io/name=camunda-bpm-platform,app.kubernetes.io/instance=camunda" -o jsonpath="{.items[0].metadata.name}")
  
export CAMUNDA_CONTAINER_PORT=$(kubectl get pod --namespace camunda $CAMUNDA_POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
  
kubectl --namespace camunda port-forward $CAMUNDA_POD_NAME 8080:$CAMUNDA_CONTAINER_PORT

# Visit `http://localhost:8080/camunda/` to use the application. The default credentials for admin access to the webapps is `demo/demo`.
```

#### 1.4.2 - Remove Camunda BPM Platform

```bash
# remove camunda
helm delete camunda -n camunda

# check
helm list --all-namespaces

# remove the namespace
kubectl delete namespace camunda

# check
kubectl get namespace
```

### 1.5 - Deploy Flowable BPM Platform using Helm Chart

Reference: https://github.com/flowable/flowable-engine/tree/master/k8s

```bash
# Add the flowable-bpm-platform repository
helm repo add flowable https://flowable.org/helm/

# Update repos
helm repo update

# Create the k8s namespace flowable-bpm-platform
kubectl create namespace flowable

# Install the flowable-bpm-platform
helm install flowable/flowable \
         --name-template flowable \
         -f helm-files/values-flowable.yaml \
         -n flowable

# check the installation
helm list --all-namespaces
```

#### 1.5.1 - Connect to Flowable

```bash
# Add kubernetes.docker.internal hostname to your hosts file
sudo echo "127.0.0.1        kubernetes.docker.internal" > /etc/hosts

# deploy the flowable ingress
kubectl apply -f helm-files/values-flowable-ingress.yaml

```

Visit http://kubernetes.docker.internal/flowable-ui/ to use the application. The default credentials for admin access to the webapps is `admin/test`.

```bash
# Workaround if the ingress-nginx does not work

# Get the application URL by running these commands:
export FLOWABLE_POD_NAME=$(kubectl get pods --namespace flowable -l "app.kubernetes.io/name=ui,app.kubernetes.io/instance=flowable" -o jsonpath="{.items[0].metadata.name}")
  
export FLOWABLE_CONTAINER_PORT=$(kubectl get pod --namespace flowable $FLOWABLE_POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
  
kubectl --namespace flowable port-forward $FLOWABLE_POD_NAME 9090:$FLOWABLE_CONTAINER_PORT

#Visit `http://localhost:9090/flowable-ui/` to use your application. The default credentials for admin access to the webapps is `admin/test`.
```

#### 1.5.2 - Remove Flowable BPM Platform

```bash
# remove flowable
helm delete flowable -n flowable

# check
helm list --all-namespaces

# remove the namespace
kubectl delete namespace flowable

# check
kubectl get namespace
```

### 1.6 - Deploy Activiti BPM Cloud using Helm Chart

Reference: https://github.com/Activiti/activiti-cloud-full-chart

#### 1.6.1 - Install Activiti

```bash
# check out the git repo
git clone https://github.com/Activiti/activiti-cloud-full-chart.git
cd activiti-cloud-full-chart

# Update dependency
helm dependency update charts/activiti-cloud-full-example

# Add domain to your values.yaml file
# Run the command
ifconfig
# Look for en0, you should see smething like this:
# en0: flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
#	options=400<CHANNEL_IO>
#	ether 3c:22:fb:c3:5e:84
#	inet 192.168.1.15 netmask 0xffffff00 broadcast 192.168.1.255
#	media: autoselect
#	status: active

# Copy the IP address (in the example is 192.168.1.15) and ".nip.io"
# Should be able to ping the IP address
ping 192.168.1.15.nip.io 

# Change the domain value in the file values.yaml
global:
  gateway:
    domain: 192.168.1.15.nip.io

# Create the k8s namespace activiti
kubectl create namespace activiti

# Install activiti
helm install charts/activiti-cloud-full-example \
         --name-template activiti \
         -f values.yaml \
         -n activiti

# check the installation
helm list --all-namespaces
```

#### 1.6.2 - Connect to Activiti

Get the application URLs:

```bash
Activiti Gateway         : http://gateway-activiti.192.168.1.20.nip.io
Activiti Identity        : http://identity-activiti.192.168.1.20.nip.io/auth `admin:admin`
Activiti Modeler         : http://gateway-activiti.192.168.1.20.nip.io/modeling `modeler:password` 
Activiti Runtime Bundle  : http://gateway-activiti.192.168.1.20.nip.io/rb
Activiti Cloud Connector : http://gateway-activiti.192.168.1.20.nip.io/example-cloud-connector
Activiti Query           : http://gateway-activiti.192.168.1.20.nip.io/query
Activiti Audit           : http://gateway-activiti.192.168.1.20.nip.io/audit
Notifications GraphiQL   : http://gateway-activiti.192.168.1.20.nip.io/notifications/graphiql
Notifications WebSockets : http://gateway-activiti.192.168.1.20.nip.io/notifications/ws/graphql
Notifications Graphql    : http://gateway-activiti.192.168.1.20.nip.io/notifications/graphql

```


#### 1.6.3 - Remove Activiti

```bash
# remove activiti
helm delete activiti -n activiti

# check
helm list --all-namespaces

# remove the namespace
kubectl delete namespace activiti

# check
kubectl get namespace
```
