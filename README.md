# Platform

| 구분 | 기술 |
|---|---|
| DB | Supabase (PostgreSQL, Free) |
| Backend | Spring Boot 2.7 / Java 8 / Maven / 전자정부 표준프레임워크 4.3 (MyBatis) |
| Frontend | Vue 3 / Vite 4 / Vue Router / Pinia / Axios |

```
platform/
├─ backend/    Spring Boot + eGovFrame (http://localhost:8090)
└─ frontend/   Vue 3 + Vite (http://localhost:5173, /api → 8090 프록시)
```

## 1. Supabase 연결

1. https://supabase.com 에서 프로젝트 생성 (DB 비밀번호 기억해두기)
2. 대시보드 상단 **Connect** → **Session pooler** 탭의 host / user 확인
   - Direct connection(`db.<ref>.supabase.co`)은 IPv6 전용이라 접속이 안 될 수 있음 → **Session pooler(5432)** 사용
3. 설정 파일 생성
   ```
   cd backend\src\main\resources\egovframework\egovProps
   copy globals-local.properties.example globals-local.properties
   ```
   `globals-local.properties` 에 Url / UserName / Password 입력 (이 파일은 git에 올라가지 않음)

## 2. 실행

```bash
# 백엔드
cd backend
mvn spring-boot:run

# 프론트 (새 터미널)
cd frontend
npm install   # 최초 1회
npm run dev
```

http://localhost:5173 접속 → "Backend UP / Database UP" 이 보이면 연동 완료.
(API 직접 확인: http://localhost:8090/api/health)

## 3. 백엔드 구조 (전자정부 표준프레임워크 방식)

```
backend/src/main/
├─ java/kr/co/seedssoft/platform/
│  ├─ config/                         기존 eGov context-*.xml 을 Java 설정으로 옮긴 것
│  │  ├─ EgovConfigAppProperties      globals.properties 로딩, propertiesService
│  │  ├─ EgovConfigAppCommon          messageSource, leaveaTrace
│  │  ├─ EgovConfigAppDatasource      dataSource (Globals.* 접속정보 사용)
│  │  ├─ EgovConfigAppMapper          MyBatis sqlSession
│  │  ├─ EgovConfigAppTransaction     *Impl 메서드에 트랜잭션 자동 적용
│  │  └─ EgovConfigWeb                CORS
│  └─ health/                         업무 모듈 예시
│     ├─ web/HealthController         Controller
│     ├─ service/HealthService        Service 인터페이스
│     └─ service/impl/
│        ├─ HealthServiceImpl         extends EgovAbstractServiceImpl
│        └─ HealthDAO                 extends EgovAbstractMapper
└─ resources/
   ├─ application.yml                 Spring Boot 설정(포트, 로그)만
   └─ egovframework/
      ├─ egovProps/globals.properties        전역 설정 (DB, CORS)
      ├─ egovProps/globals-local.properties  로컬 실제 접속정보 (git 제외, 직접 생성)
      ├─ message/message-common.properties   공통 메시지
      └─ mapper/
         ├─ config/mapper-config.xml         MyBatis 공통 설정
         └─ platform/health/Health_SQL_postgres.xml
```

새 기능(예: 게시판)을 만들 때는 `health` 와 같은 모양으로 추가합니다.

1. `board/service/BoardVO.java`, `BoardService.java`
2. `board/service/impl/BoardServiceImpl.java`, `BoardDAO.java`
3. `board/web/BoardController.java`
4. `resources/egovframework/mapper/platform/board/Board_SQL_postgres.xml` (namespace = `BoardDAO`)

## 4. 운영 배포 시 환경변수

globals.properties 의 값은 환경변수로 덮어쓸 수 있습니다. 키의 `.` 을 `_` 로 바꾸고 대문자로 씁니다.

| 변수 | 설명 |
|---|---|
| `GLOBALS_POSTGRES_URL` / `GLOBALS_POSTGRES_USERNAME` / `GLOBALS_POSTGRES_PASSWORD` | Supabase 접속 정보 |
| `GLOBALS_ALLOW_ORIGIN` | 프론트를 다른 도메인에 올릴 때 프론트 주소 (콤마 구분) |
| `SERVER_PORT` | 기본 8090 |

## 참고

- 백엔드가 DB에 직접 붙으므로 Supabase JS 클라이언트/anon key 는 사용하지 않음.
  단, Supabase는 `public` 스키마 테이블을 REST(Data API)로도 노출하므로,
  테이블 생성 후 **RLS를 켜두거나** (Table Editor → Enable RLS) 대시보드에서 Data API 를 꺼두는 것을 권장.
- 무료 플랜은 7일간 요청이 없으면 프로젝트가 일시정지됨 → 대시보드에서 Restore.
- 테이블은 Supabase SQL Editor 에서 생성.
- eGov 라이브러리는 Maven Central 이 아닌 `https://maven.egovframe.go.kr/maven/` 에서 받음 (pom.xml 에 설정됨).
