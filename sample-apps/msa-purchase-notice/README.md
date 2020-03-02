# msa-purchase-notice

購入通知サービス

## ローカルデータベースの準備
 - ローカル（localhost:5432）にPostgresにデータベースを作成する

| Property   | Value       |
|:-----------|:------------|
| DB名      | m9amsa |
| ユーザ     | postgres    |
| パスワード | postgres    |

- 既存テーブルをクリアする
```
$ psql  -U postgres -d m9amsa
m9amsa=# DROP SCHEMA public CASCADE;
m9amsa=# CREATE SCHEMA public;
m9amsa=# \q
```

## build
```
$ pwd
{git cloneしたディレクトリ}/msa-purchase-notice

$ ./mvnw clean package
```

### Jacoco
テスト実行後、カバレッジレポートが作成されるので確認すること。

  - target/site/jacoco/index.html

### JavaDoc
ビルド時にJavaDoc, javadoc.jarが下記に生成されるようにpom.xmlに設定されている。

  - target/site/apidocs/purchaseNotice/index.html
  - target/javadoc-jar/m9a-msa-purchase-notice-{version}-javadoc.jar
    - Dockerイメージ生成の際に`target/*.jar`を取得するため、javadoc.jarのパスを別に設定している(pom.xmlを参照)

## マイクロサービスの実行
実行前にmsa-env/setup/README.mdに従って環境セットアップが済んでいること

### Dockerイメージ生成
- 開発環境では基本的に最新を実行する想定のため下記の手順ではタグを指定していない
- テスト等で異なるバージョンを起動したい場合は適宜タグを設定する
```
$ eval $(minikube -p m9amsa docker-env)

$ pwd
{git cloneしたディレクトリ}/msa-purchase-notice

$ docker build . -t purchase-notice
(省略)

$ docker images | grep -E '(IMAGE|purchase-notice)'
REPOSITORY                                                       TAG                            IMAGE ID            CREATED              SIZE
purchase-notice                                                  latest                         9adc891b94a6        About a minute ago   403MB
```

### minikubeデプロイ
- 開発環境ではnamespace=localを使用する
- マイクロサービスはアプリ + DBのコンテナのセットで動作する
  - アプリのコンテナはDBを前提に起動処理を行うためDBからデプロイする

```
$ pwd
{git cloneしたディレクトリ}/msa-purchase-notice

$ ls manifest/
deployment.yml  deployment-db.yml  service.yml  service-db.yml

$ kubectl apply -f manifest/deployment-db.yml -n local
deployment.apps/purchase-notice-db created

$ kubectl apply -f manifest/service-db.yml -n local
service/purchase-notice-db created

$ kubectl apply -f manifest/deployment.yml -n local
deployment.apps/purchase-notice created

$ kubectl apply -f manifest/service.yml -n local
service/purchase-notice created

$ kubectl get service -n local | grep -E '(NAME|purchase-notice)'
NAME                           TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
purchase-notice                ClusterIP      10.105.178.141     <none>        80/TCP              11m
purchase-notice-db             ClusterIP      10.109.211.63    <none>        5432/TCP            11m

$ kubectl get po -n local | grep -E '(NAME|purchase-notice)'
NAME                                    READY   STATUS    RESTARTS   AGE
purchase-notice-6d56df88d8-hj47k        1/1     Running   0          12m
purchase-notice-db-7df457d967-x978p     1/1     Running   0          12m

```
