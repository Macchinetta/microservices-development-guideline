# msa-flight-ticket-fare-calculation

運賃計算サービス


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
{git cloneしたディレクトリ}/msa-flight-ticket-fare-calculation

$ ./mvnw clean package
```

### Jacoco
テスト実行後、カバレッジレポートが作成されるので確認すること。

  - target/site/jacoco/index.html

### JavaDoc
ビルド時にJavaDoc, javadoc.jarが下記に生成されるようにpom.xmlに設定されている。

  - target/site/apidocs/flight-ticket-fare-calculation/index.html
  - target/javadoc-jar/m9a-msa-flight-ticket-fare-calculation-{version}-javadoc.jar
    - Dockerイメージ生成の際に`target/*.jar`を取得するため、javadoc.jarのパスを別に設定している(pom.xmlを参照)

## マイクロサービスの実行
実行前にmsa-env/setup/README.mdに従って環境セットアップが済んでいること

### Dockerイメージ生成
- 開発環境では基本的に最新を実行する想定のため下記の手順ではタグを指定していない
- テスト等で異なるバージョンを起動したい場合は適宜タグを設定する
```
$ eval $(minikube -p m9amsa docker-env)

$ pwd
{git cloneしたディレクトリ}/msa-flight-ticket-fare-calculation

$ docker build . -t flight-ticket-fare-calculation
(省略)

$ docker images | grep -E '(IMAGE|flight-ticket-fare-calculation)'
REPOSITORY                                                                                 TAG                            IMAGE ID            CREATED              SIZE
flight-ticket-fare-calculation                                                             latest                         a929b399ad3e        About a minute ago   377MB
```

### minikubeデプロイ
- 開発環境ではnamespace=localを使用する
- マイクロサービスはアプリとDBのコンテナのセットで動作する
  - アプリのコンテナはDBを前提に起動処理を行うためDBからデプロイする

```
$ pwd
{git cloneしたディレクトリ}/msa-flight-ticket-fare-calculation

$ ls manifest/
deployment.yml  deployment-db.yml  service.yml  service-db.yml

$ kubectl apply -f manifest/deployment-db.yml -n local
deployment.apps/flight-ticket-fare-calculation-db created

$ kubectl apply -f manifest/service-db.yml -n local
service/flight-ticket-fare-calculation-db created

$ kubectl apply -f manifest/deployment.yml -n local
deployment.apps/flight-ticket-fare-calculation created

$ kubectl apply -f manifest/service.yml -n local
service/flight-ticket-fare-calculation created

$ kubectl get service -n local | grep -E '(NAME|flight-ticket-fare-calculation)'
NAME                                                     TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
flight-ticket-fare-calculation                           ClusterIP      10.96.14.177     <none>        80/TCP              11m
flight-ticket-fare-calculation-db                        ClusterIP      10.102.126.67    <none>        5432/TCP            11m

$ kubectl get po -n local | grep -E '(NAME|flight-ticket-fare-calculation)'
NAME                                                              READY   STATUS    RESTARTS   AGE
flight-ticket-fare-calculation-c76c889f7-gbgkk                    1/1     Running   0          12m
flight-ticket-fare-calculation-db-5b5b59fcdc-4lrbb                1/1     Running   0          12m

```

