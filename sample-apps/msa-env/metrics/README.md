# メトリクスサーバ
Prometheus, grafanaの設定

* Ingressの設定はこのディレクトリのyamlには含まれていないため、別途ingress_default.ymlを適用すること

## Prometheus
- セットアップ例
  1. helm install
      ```
      $ helm install --name prom --namespace default -f prom-values-default.yml stable/prometheus
      ```
  1. hostsファイルに下記を追記
      ```
      (導入した環境のipアドレス) prometheus.sampleapplication.xyz
      ```
  1. ブラウザで開く
      - http://prometheus.sampleapplication.xyz/

- helm設定値について
  - https://github.com/helm/charts/tree/master/stable/prometheus

## grafana
- セットアップ例
  1. helm install
      ```
      $ helm install --name graf --namespace default -f graf-values-default.yml stable/grafana
      ```
  1. hostsファイルに下記を追記
      ```
      (導入した環境のipアドレス) grafana.sampleapplication.xyz
      ```
  1. ブラウザで開く
      - http://grafana.sampleapplication.xyz/
      - graf-values-default.ymlに設定したアカウント、パスワードを入力

- helm設定値について
  - https://github.com/helm/charts/tree/master/stable/grafana


