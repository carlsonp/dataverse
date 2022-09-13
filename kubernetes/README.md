# Deployment of Dataverse on Kubernetes

## Pre-Reqs
1. AKS + ACR Deployed
2. Helm CLI
3. Azure CLI

## Steps

The steps below deploy the different components of Dataverse. To accelerate deployment, [bitnami helm charts](https://github.com/bitnami/charts/tree/master/bitnami) for different services such as Solr and Postgres are used.

0. Setup Common Environment Variables:

Define the following `.env` file in the current directory to set the following env vars:

```bash
RG_NAME=dataverse-test-deployment
AKS_NAME=aks-cluster
ACR_NAME=acrdataverse0913
```

Run the following to then set the env vars:

```bash
set -a
source .env
set +a
```

Finally, make sure you are logged into your AKS Cluster:

```bash
az aks get-credentials --name $AKS_NAME --resource-group $RG_NAME
```

1. Deploy Solr:

First step is to build the custom solr image to include the Dataverse specific configurations:

```bash
cd ./solr
az acr build --registry $ACR_NAME -g $RG_NAME --image solr:dataverse .
```

Next, update `./solr/values.yaml` to reference your specific ACR repository. Find the following section in the file and update with your values:

```yaml
image:
  registry: <ACR_NAME>.azurecr.io
  repository: solr
  tag: dataverse
```

```bash
cd ./solr
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install solr bitnami/solr -f values.yaml
```

2. Deploy Postgres:

> Take note that in the `values.yaml` file both the passwords for the DB are set and the upstream image points to the bitnami image.

```bash
cd ./postgres
helm install postgresql bitnami/postgresql -f values.yaml
```

3. Deploy RServe:

First build the container image:

```bash
cd ./rserver
az acr build --registry $ACR_NAME -g $RG_NAME --image rserve:dataverse .
```

Next update `./rserve/template.yaml` to reference your image:

```yaml
    spec:
      containers:
      - name: rserve
        image: <ACR-NAME>.azurecr.io/rserve:dataverse
        ports:
        - containerPort: 6311
```

Then run the following:

```bash
kubectl apply -f template.yaml
```

4. Deploy Dataverse:

> Take note that this is happening from the root of the repo, not the `./kubernetes` folder.

First, build the image:

```bash
cd ..
az acr build --registry $ACR_NAME -g $RG_NAME --image dataverse:dataverse .
```

Next, update `manifest.yaml` with the proper ACR name:

```yaml
    spec:
      containers:
      - name: dataverse
        image: <ACR-NAME>.azurecr.io/dataverse/dataverse:v1
```

Next, create a secret in kubernetes that uses the `.env` file in the root directory, which will create env vars read in by the dataverse container:

```bash
kubectl create secret generic my-env-list --from-env-file=.env
```

Next, review the following and create the azure blob storage class for the pvc.

[Azure Blob CSI Driver Install](https://docs.microsoft.com/en-us/azure/aks/azure-blob-csi?tabs=Blobfuse)

Finally, apply the manifest:

```bash
kubectl apply -f manifest.yaml
```