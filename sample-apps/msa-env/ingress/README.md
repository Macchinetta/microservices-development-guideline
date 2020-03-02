# Ingress

* minikubeではsetup/README.mdに従ってIngressを有効化するので「1. Ingress Controllerのインストール」は不要。
* 「2. Ingressの適用」は必要に応じて実施すること。


## 1. Ingress Controllerのインストール

### helm chart
https://github.com/helm/charts/tree/master/stable/nginx-ingress

### インストール手順

1. helm install
   ```
   $ helm install --name ni --namespace kube-system stable/nginx-ingress
   ```
2. インストール確認
   ```
   $ helm status ni
   
   # インストールログが表示されるのでdepolyment, pod, serviceなどkube-systemに作成されていることを確認
   ```
## 2. Ingressの適用
Ingressの有効化/インストールを確認出来たら、kubectlでガイドの記述にあるnamespaceごとに設定ファイルを選択して設定を取り込むこと。

```
kubectl apply -f (適用したいIngressのyamlファイル) -n (適用するnamespace)
```
