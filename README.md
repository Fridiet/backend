# Fridiet Backend

냉장고 재고·사용자·식단 캘린더·월간 결산을 관리하는 Spring Boot API 서버입니다.
로그인, QR 용기, 레시피 조리 완료와 재고 차감, AI 서버 연동을 포함합니다.

## 개발 및 실행

Java 21, Gradle Wrapper, MySQL 8을 사용합니다. 사용하는 기능에 따라
Redis, SMTP, Firebase, S3 및 소셜 로그인 설정을 별도로 준비합니다.

`src/main/resources/application.example.yml`을 로컬 `application.yml`로 복사하고
본인의 설정을 입력하세요. 설정 파일과 Firebase 서비스 계정은 Git에 올리지 않습니다.
공유/운영 DB에서 검토 없이 `ddl-auto=update`를 사용하지 마세요.

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

Linux/macOS: `bash gradlew test`, `bash gradlew bootRun`.
기본 포트는 8080입니다. 대부분의 API는 로그인 JWT가 필요하며 `/`는 웹 화면이 아닙니다.

## AI 연결

`ai.base-url`은 별도로 실행한 AI 서버 주소, `AI_SERVICE_TOKEN`은 서버 간 인증값입니다.
토큰과 DB 비밀번호를 프론트엔드에 넣지 않습니다. 운영 주소·키는 이 저장소에 없습니다.
OAuth 콜백은 배포 주소와 카카오/구글 콘솔 설정을 일치시켜야 합니다.
소셜 로그인 이름 보완은 새 로그인 토큰부터 적용되므로 다시 로그인해야 합니다.

단위 정의와 검증 사례는 독립 빌드를 위해 `src/main/resources/quantity_units.properties`,
`quantity_cases.json`에 포함했습니다. AI의 같은 정의를 변경할 때 함께 동기화·검증합니다.

## 관련 저장소

- [Frontend](https://github.com/Fridiet/frontend)
- [Backend](https://github.com/Fridiet/backend)
- [AI](https://github.com/Fridiet/ai)

기존 통합 프로젝트 [euna22/fridiet](https://github.com/euna22/fridiet)의 작업본에서
서버 수정 사항을 보존하여 분리했습니다. 과거 Git 이력·운영 설정·개인 데이터는 포함하지 않습니다.
