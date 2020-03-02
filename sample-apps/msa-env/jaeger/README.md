# Jaeger

* Ingressの設定はこのディレクトリのyamlには含まれていないため、別途ingress_default.ymlを適用すること

- セットアップ例
  1. kubectl
  ```
  kubectl apply -f jaeger-all-in-one-template.yml -n default
  ```
  1. hostsファイルに下記を追記
      ```
      (導入した環境のipアドレス) jaeger.sampleapplication.xyz
      ```
  1. ブラウザで開く
      - http://jaeger.sampleapplication.xyz/