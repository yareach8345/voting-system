# Spring Boot Project - VotingSystem

일본어 문서는 아래 파일음 참고해주세요.

日本語の文書はこちらのファイルをご覧ください。
[日本語の文書](README-JP.md)

***

스프링부트를 통해 제어하는 번용적인 투표시스템

---
## 🖥️ 프로젝트 개요
이후 진행할 프로젝트에서 사용하기 위해 제적한 투표 시스템

### 🗓️ 개발기간
2026.02.09 ~ 2026.02.21

### ⚙️ 사용기술
* kotlin
* coroutine
* spring boot
* spring-reactive-web
* spring-rest-docs
* r2dbc
* MySQL
* H2 (for test)

## 사용방법
### 데이터 베이스 준비
아래의 파일의 스크립트를 실행하여 테이블을 생성해주세요.

[sql file for initialize table](docs/sql/schema.sql)

※ 본스크립트는 MySQL사용을 전제로 작성되었습니다.

### Configuration
운영에 필요한 설정을 합니다.

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
    use-expire: true # 기본값 はfalse
    ttl-seconds: 600 # 10분마다 처리 실행
  validation:
    item:
      use-validator: true # 기본값 false
      regex-string: "[0-9]{2}" # 두자리 숫자만 유효합니다.
    user-id:
      use-validator: true # 기본값 false
      regex-string: "[a-zA-Z]+" # 유저id는 로마자만 유효합니다.
```

## API에 대하여
### 용어 설명
| 용어           | 설명                          |
|--------------|-----------------------------|
| Election(선거) | 선거 개시부터 종료까지의 전반의 프로세스를 칭함  |
| Vote(투표)     | 유저 항목을 선택해 표를 던지는 것         |
| Item(아이템)    | 여저가 투표시 선택한 아이템             |

※ 시스템에 의해 유저가 선택할 수 있는 선택지의 수가 무한이 되는 경우가 있을 수 있기 때문에,
여러가지중 하나를 선택한다는 의미를 가질 수 있는 '후보'보다 '아이템'이라는 용어를 사용했습니다.

### API문서
**※ 아래의 문서는 일본어로 작성되어있습니다.**
* [indexページ](https://yareach8345.github.io/voting-system/)
* [Electionのページ](https://yareach8345.github.io/voting-system/election.html)
* [Voteのページ](https://yareach8345.github.io/voting-system/vote.html)
* [ErrorCodeのページ](https://yareach8345.github.io/voting-system/errorcode.html)

프로그램 실행시 /docs/index.html페이지에 접속하여 api를 확인할 수도 있습니다.