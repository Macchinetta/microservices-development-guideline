# ログ収集サーバ
elasticsearch, fluentd, kibanaの設定

* Ingressの設定はこのディレクトリのyamlには含まれていないため、別途ingress_default.ymlを適用すること

## elasticsearch
- localでのセットアップ例
  1. helm install
      ```
      $ helm install --name m9a-e --namespace default -f elastic-values.yml stable/elasticsearch
      ```

- helm設定値について
  - https://github.com/elastic/helm-charts/tree/master/elasticsearch

## fluentd
- localでのセットアップ例
  1. helm install
      ```
      $ helm install --name m9a-f --namespace default -f fluentd-values.yml stable/fluentd-elasticsearch
      ```

- helm設定値について
  - https://github.com/helm/charts/tree/master/stable/fluentd-elasticsearch

## kibana
- localでのセットアップ例

 1. helm install
      ```
      $ helm install --name m9a-k --namespace default -f kibana-values.yml --version 3.2.3 stable/kibana
      ```
  1. hostsファイルに下記を追記
      ```
      (minikubeのipアドレス) msa-kibana
      ```
  1. ブラウザで開く
      - http://kibana.sampleapplication.xyz/

- helm設定値について
  - https://github.com/helm/charts/tree/master/stable/kibana



## 追記
- EKS上で最新版のkibana helmチャートを使用すると下記のエラー（現時点：2019/10/15）

      （Error: validation failed: unable to recognize "": no matches for kind "Ingress" in version "networking.k8s.io/v1beta1"）



- helmチャートが使用しているkibanaのバージョン履歴調査し、下記の一覧が表示される。

    ```
    $ helm search -l stable/kibana
    NAME         	CHART VERSION	APP VERSION	DESCRIPTION                                                 
    stable/kibana	3.2.4        	6.7.0      	Kibana is an open source data visualization plugin for El...
    stable/kibana	3.2.3        	6.7.0      	Kibana is an open source data visualization plugin for El...
    stable/kibana	3.2.2        	6.7.0      	Kibana is an open source data visualization plugin for El...
    stable/kibana	3.2.1        	6.7.0      	Kibana is an open source data visualization plugin for El...
    stable/kibana	3.2.0        	6.7.0      	Kibana is an open source 
    ```

- Git上で stable/kibanaのCHART：3.2.4の更新履歴確認すると、下記の変更がされていた。今回のエラーの原因だと思われる。
(https://github.com/helm/charts/commit/7a50ed67a45959c11b675bb274819263f4ed253b#diff-1104eae0826c45bb7d9cd233aff17b1e)

    ```
    stable/kibana/templates/ingress.yaml

       - apiVersion: extensions/v1beta1
       + apiVersion: networking.k8s.io/v1beta1
    ```

- stable/kibanaのCHART最新版（3.2.4）の前のバージョン（3.2.3）指定し、kibana無事に構築できた。


