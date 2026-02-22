# Spring Boot Project - VotingSystem

***

Spring Bootを用いて制作した汎用的な投票システム。

---
## 🖥️ プロジェクト紹介
後で進行するプロジェクトで使うため制作した投票システムです。

### 🗓️ 開発期間
2026.02.09 ~ 2026.02.21

### ⚙️ 使用技術
* kotlin
* coroutine
* spring boot
* spring-reactive-web
* spring-rest-docs
* r2dbc
* MySQL
* H2 (for test)

## 使用方法
### データベースの準備
下のファイルのスクリプトを実行してテーブルを生成してください。

[sql file for initialize table](docs/sql/schema.sql)

※ 本スクリプトはMySQLの環境を前提にして作成されました。

### Configuration
運用に必要な設定を行います。

```yaml
spring:
  application:
    name: voting_system
  r2dbc:
    url: "MY-SQL-URL"
    username: "USERNAME"
    password: "PASSWORD"
vote:
  expire:
    use-expire: true # 基本値はfalse
    ttl-seconds: 600 # 10分ごとに処理を実行
  validation:
    item:
      use-validator: true # デフォルトはfalse
      regex-string: "[0-9]{2}" # 2桁の数字のみ有効
    user-id:
      use-validator: true # デフォルトはfalse
      regex-string: "[a-zA-Z]+" # UserIdはアルファベットのみ有効
```

## APIについて
### 用語説明
|用語| 説明                     |
|--|------------------------|
|Election(選挙)|選挙の開始から終了までの全般のプロセスのこと。 |
|Vote(投票)|ユーザーが項目を選択して表を出すこと。|
|Item(アイテム)|ユーザーが投票する時選択したもの。|

※ 正規表現によってユーザーが選択できるものの数が無限になる場合が存在するため、
いくつかの中で一つを選ぶんだと思う可能性が有る「候補」という用語を使わず、「アイテム」という用語を使いました。

### API文書 
* [indexページ](src/main/resources/static/docs/index.html)
* [Electionのページ](src/main/resources/static/docs/election.html)
* [Voteのページ](src/main/resources/static/docs/vote.html)
* [ErrorCodeのページ](src/main/resources/static/docs/errorcode.html)

プログラムを実行したあと/docs/index.htmlのページにアクセス確認することもできます。
