# 실행 매뉴얼: 소스 코드 실행 환경 및 실행 방법

## 백엔드

### 1 실행 환경

- Java JDK 21
- Spring Boot 4.0.6
- Gradle 9.4.1 — 저장소에 포함된 Gradle Wrapper 사용
- MySQL 8.x
- Redis
- 외부 연동 서비스: Gmail SMTP, Firebase, AWS S3, 카카오·구글 로그인 API

아래 명령은 Windows PowerShell 기준입니다. 저장소를 내려받은 폴더명은 `backend`로 가정합니다.

### 2 환경 설정

`backend/src/main/resources/application.example.yml`을 같은 위치에 `application.yml`이라는 이름으로 복사합니다.
`backend` 폴더의 상위 폴더에서 다음 명령을 실행합니다.

```powershell
Copy-Item backend/src/main/resources/application.example.yml backend/src/main/resources/application.yml
```

생성한 `application.yml`에서 DB 접속 정보, JWT 서명키, AI 서버 주소를 설정합니다.
아래는 주요 항목의 예시이며, 전체 설정 파일을 대체하는 내용은 아닙니다.
예제 파일의 나머지 항목도 사용하는 서비스에 맞게 입력해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fridiet?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: "<DB 사용자명>"
    password: "<DB 비밀번호>"

jwt:
  secret: "<충분히 긴 임의의 JWT 서명키>"

ai:
  base-url: http://localhost:8000
  service-token: ${AI_SERVICE_TOKEN:}

frontend:
  redirect-uri: fridiet://login-callback

backend:
  qr-base-url: http://localhost:8080
```

AI 서버와 통신하는 인증 토큰은 백엔드를 실행할 PowerShell 창에서 설정합니다.
AI 서버의 `AI_SERVICE_TOKEN`과 같은 값을 사용합니다.

```powershell
$env:AI_SERVICE_TOKEN="<AI 서버와 동일한 인증 토큰>"
```

서비스별 추가 설정 항목은 다음과 같습니다.

| 설정 항목 | 용도 |
|---|---|
| `spring.data.redis` | 이메일 인증 등에서 사용하는 Redis 연결 |
| `spring.mail` | 인증 메일 발송 |
| `cloud.aws` | 사진 파일의 AWS S3 저장 |
| `kakao`, `google` | 소셜 로그인 및 계정 연동 |
| `firebase.config-path` | 푸시 알림용 Firebase 서비스 계정 파일 위치 |

Firebase 서비스 계정 파일은 별도로 준비하여 설정한 위치에 배치합니다.
소셜 로그인 콜백은 실행 주소와 카카오·구글 개발자 콘솔에 등록한 주소가 일치해야 합니다.
`frontend.redirect-uri`도 웹 또는 설치 앱 등 실제 실행 환경에 맞게 설정합니다.

`application.yml`, Firebase 서비스 계정 파일, 실제 비밀번호와 인증값은 공개 저장소에 업로드하지 않습니다.
공유·운영 DB에서는 스키마 변경을 검토한 뒤 적용하며, `ddl-auto=update`를 무조건 사용하지 않습니다.
백엔드는 `.env` 파일을 자동으로 불러오지 않으므로 Spring 설정 파일이나 프로세스 환경변수를 사용합니다.

### 3 실행 방법

`backend` 폴더의 상위 폴더에서 다음 명령을 실행합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

실행 후 기본 API 주소는 <http://localhost:8080>입니다.
백엔드는 웹 화면이 아닌 API 서버이므로 루트 주소에 일반 웹 페이지가 표시되지 않습니다.
인증이 필요한 API는 로그인 후 발급받은 JWT를 포함하여 요청합니다.

테스트와 배포용 JAR 빌드는 백엔드 폴더에서 수행합니다.

```powershell
.\gradlew.bat test
.\gradlew.bat bootJar
```

Linux/macOS 환경에서는 다음 명령을 사용합니다.

```bash
bash gradlew test
bash gradlew bootRun
```

AI 서버와 연동하려면 AI 서버도 별도로 실행해야 합니다.
소셜 로그인 토큰의 이름·건강 설정 필드는 새 로그인부터 발급되므로 기존 사용자는 다시 로그인합니다.

관련 저장소: [프론트엔드](https://github.com/Fridiet/frontend) · [AI](https://github.com/Fridiet/ai)

> 독립 빌드를 위해 단위 정의와 검증 사례를 `src/main/resources/quantity_units.properties`,
> `quantity_cases.json`에 포함했습니다. AI의 같은 정의를 수정할 때 함께 동기화·검증합니다.
