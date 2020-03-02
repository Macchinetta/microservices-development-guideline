# 初めに
ソフトウェアのバージョン等はガイドラインを参照

# ツール等のインストール
1. Git Bashをインストールする https://git-scm.com/download/win
    - これ以降のコマンドは特に指定しない場合はGit Bashで実行する
1. VirtualBoxをインストールする https://www.virtualbox.org/wiki/Downloads
1. マイクロサービスの実行環境を構築する
    1. chocolateyのインストール
        - 各種ツールのインストールに利用
        - Powershellを管理者として開き、Chocolateyをインストールする:
            ```
            Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-ObjectSystem.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
             ```
    1. chocolateyで必要なツールをインストール
        - インストールするツールは以下の通り
          1. minikube
              - ローカルのkubernetesクラスタ環境
          1. kubernetes-cli
              - kubernetesの操作コマンド
          1. kubernetes-helm
              - kubernetes上の各種ツールインストールに利用
          1. docker-desktop
              - コンテナイメージのビルドに利用
    
        - **Powershellを閉じて、新しいPowershellを管理者として開きなおし**、下記のコマンドを実行する:
          ```
          choco install -y minikube kubernetes-cli kubernetes-helm docker-desktop
          ```
1. kubectlのPATH調整
    - 理由：Docker Desktopにkubectlが付属しているがバージョンが古いのでchocoで入れたほうにパスを通す
    - システム環境変数のPATHから、C:\ProgramData\chocolatey\binを一番上に移動
1. docker-desktopの設定

    dockerコマンドを使用するためにdocker-desktopをインストールするが、実際に使用するのはminikubeに同梱されたdockerになる。

    インストール時にスタートアップに登録されるがWindows上にインストールしたdockerそのものは使用しないため、これを無効化する。

    1. タスクマネージャーを起動
    1. 「スタートアップ」タブを選択
    1. Docker Desktopを無効にする

1. JDKをインストール
    - DL元参考: [AdoptOpenJDK](https://adoptopenjdk.net/archive.html?variant=openjdk11&jvmVariant=hotspot)
    - OpenJDKを使用するのでバイナリをDLしてパスを通す

# minikubeセットアップ
1. 準備
    - 本プロジェクトの設定情報を使うため`msa-env`をGitLabからcloneする。
    - Hyper-Vをオフにする。
        1. コントロール パネルを起動
        1. プログラムを選択
        1. 「windowの機能の有効化または無効化」を選択
        1. Hyper-Vを外す
1. 前提
    - 開発環境のprofileは`m9amsa`
    - 開発環境ではAWSで運用するstaging環境、product環境と設定情報を明確に分けるためnamespace=localを設定する
1. デフォルトのメモリ設定(2G)では足りないので自環境のメモリを確認して増やす
    ```
    $ minikube config set memory 10240 -p m9amsa
    $ minikube config set cpus 4 -p m9amsa
    ```
1. 起動する
    ```
    $ minikube -p m9amsa start
    (kubernetesイメージのDLなどが実行される。エラーが出なければOK)
    ```
    - kubectlの設定もstartで行われるので、profile指定して起動するとkubectlはm9amsaに対して実行されるようになる
1. namespaceの作成
    ```
    $ kubectl create namespace local
    namespace/local created
    
    $ kubectl get namespaces
    NAME              STATUS   AGE
    default           Active   20d
    kube-node-lease   Active   20d
    kube-public       Active   20d
    kube-system       Active   20d
    local             Active   7s
    ```
1. kafkaの導入
    
    非同期通信にkafkaを使用するため、minikubeにインストールする。
    ```
    $ helm init
    
    $ helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/
    "confluentinc" has been added to your repositories
    
    $ helm repo update
    (エラーが表示されなければOK)
    $ helm install --set cp-schema-registry.enabled=false,cp-kafka-rest.enabled=false,cp-kafka-connect.enabled=false confluentinc/cp-helm-charts --name m9amsa --namespace local
    (省略)
    
    # helm install したkafkaインスタンスの確認
    $ kubectl get service -n local | grep -E "(NAME|m9amsa)"
    NAME                           TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
    9amsa-cp-kafka                ClusterIP         0.109.17.66     <none>        9092/TCP            23h
    9amsa-cp-kafka-headless       ClusterIP      None                 <none>        9092/TCP            23h
    9amsa-cp-ksql-server          ClusterIP         0.111.10.255    <none>        8088/TCP            23h
    9amsa-cp-zookeeper            ClusterIP         0.103.182.149   <none>        2181/TCP            23h
    9amsa-cp-zookeeper-headless   ClusterIP      None                 <none>        2888/TCP,3888/TCP   23h
    $ kubectl get pod -n local | grep -E "(NAME|m9amsa)"
    NAME                                    READY   STATUS          RESTARTS   AGE
    m9amsa-cp-kafka-0                       2/2     Running   0                 23h
    m9amsa-cp-kafka-1                       2/2     Running   0                 23h
    m9amsa-cp-kafka-2                       2/2     Running   0                 23h
    m9amsa-cp-ksql-server-6b5968b49-hzv7v   2/2     Running   0                 23h
    m9amsa-cp-zookeeper-0                   2/2     Running   0                 23h
    m9amsa-cp-zookeeper-1                   2/2     Running   0                 23h
    m9amsa-cp-zookeeper-2                   2/2     Running   0                 23h
    ```
1. ingressの有効化と設定

    Gatewayとしてingressを利用するため設定を行う。
    ```
    # ingressを有効化
    $ minikube addons enable ingress -p m9amsa
    (省略)
    
    $ kubectl get po -n kube-system | grep -E "(NAME|ingress)"
    NAME                                        READY   STATUS    RESTARTS           AGE
    nginx-ingress-controller-7b465d9cf8-rb6l9   1/1     Running   60                 13d
    
    $ cd {msa-envのclone先}
    # local用ingressを設定
    
    $ kubectl apply -f ingress/ingress_local.yml
    ingress.extensions/ingress-local created
    
    $ kubectl apply -f ingress/ingress_actuator_local.yml
    ingress.extensions/ingress-actuator created
    
    $ kubectl get ingress -n local
    NAME               HOSTS          ADDRESS     PORTS   AGE
    ingress-actuator   local-server   10.0.2.15   80      2m18s
    ingress-local      local-server   10.0.2.15   80      3m21s
    ```
1. hostsの設定
    
    ingressはホスト名での名前解決を行うため、設定したホスト名でリクエストできるようにする。

    minikubeのIPアドレスを取得
    ```
    # IPは環境によって異なる
    $ minikube -p m9amsa ip
    192.168.99.100
    ```

    hostsファイル(C:\Windows\System32\drivers\etc\hosts)に以下を追記
    ```
    192.168.99.100 local-server
    ```

1. ConfigMapの設定

    各マイクロサービスの環境情報(DB接続先など)をKubernetesのConfigMapとして保持するため、デプロイ前に設定する必要がある。
    ```
    $ cd {msa-envのclone先}

    $  kubectl apply -f configmap/env-configmap.yml -n local
    configmap/m9amsa-configmap created

    $ kubectl get cm -n local
    NAME                                           DATA   AGE
    m9amsa-configmap                               18     8s
    m9amsa-cp-kafka-jmx-configmap                  1      24h
    m9amsa-cp-ksql-server-jmx-configmap            1      24h
    m9amsa-cp-ksql-server-ksql-queries-configmap   1      24h
    m9amsa-cp-zookeeper-jmx-configmap              1      24h
    ```

1. dockerコマンドの確認

    先に書いた通り使用したいdockerの本体はminikube上にあるためdockerコマンドの設定をminikube用に変更する。

    ```
    # minikubeコマンドを使用して必要な環境変数を設定する
    $ eval $(minikube -p m9amsa docker-env) 

    $ docker images
    (k8sなどのイメージが一覧で表示されることを確認)
    ```