# K8Sの各種設定値

## ポート

| # | サービス | name | type | port | targetPort | 
|--: | -------- | ---- |  ---- | ---- | ---------- |
|1. | フライトサービス | flight | ClusterIP | 80 | 28080 |
| &nbsp; | フライトサービス/DB | flight-db | ClusterIP | 5432 | 5432 |
|2. | フライトチケットサービス | flight-ticket | ClusterIP | 80 | 28080 |
|3. | 予約サービス | reserve | ClusterIP | 80 | 28080 |
|&nbsp; | 予約サービス/DB | reserve-db | ClusterIP | 5432 | 5432 |
|4. | アカウントサービス | account | ClusterIP | 80 | 28080 |
|&nbsp; | アカウントサービス/DB | account-db | ClusterIP | 5432 | 5432 |
|5. | 購入サービス | purchase | ClusterIP | 80 | 28080 |
|6. | 運賃計算サービス | flight-ticket-fare-calculation | ClusterIP | 80 | 28080 |
|7. | 予約通知サービス | reserve-notice | ClusterIP | 80 | 28080 |
|8. | 購入通知サービス | purchase-notice | ClusterIP | 80 | 28080 |
