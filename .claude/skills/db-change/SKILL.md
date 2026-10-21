---
name: db-change
description: SCENT STUDIO platform 의 Supabase(PostgreSQL) 스키마를 추가·변경하는 절차 — 테이블/컬럼 추가, 제약조건, 마이그레이션 SQL 파일 작성과 적용. DB 구조를 바꾸거나 새 테이블이 필요할 때 사용.
---

# DB 변경 절차

프로젝트 규칙은 루트 `CLAUDE.md` 의 "DB 규칙"이 기준이다.

## 1. 현재 상태 먼저 읽기
- `globals-local.properties` 의 접속정보로 **읽기 전용 조회**: 테이블·컬럼·제약조건·RLS·행 수
- Python `pg8000` 사용 시 SSL 은 `sslmode=require` 와 같게 (인증서 검증 끔). 한글 출력은 `PYTHONIOENCODING=utf-8`
- 비밀번호를 출력하거나 채팅에 쓰지 않는다

## 2. SQL 파일 작성
- 파일: `db/V{다음번호}__{설명}.sql` (예: `V3__add_perfume_image.sql`)
- 규칙: 소문자 snake_case, `BIGINT GENERATED ALWAYS AS IDENTITY`, `TIMESTAMPTZ DEFAULT now()`, 필요한 `UNIQUE`/`CHECK`/`FOREIGN KEY`, 새 테이블은 `ENABLE ROW LEVEL SECURITY`
- 테이블·컬럼 용도는 `COMMENT ON` 으로 한국어 설명
- 전체를 `BEGIN; ... COMMIT;` 로 감싼다

## 3. 적용
- **추가·변경(CREATE / ALTER ADD / INSERT 기본데이터)**: 사용자에게 변경 내용을 보여주고 동의를 받은 뒤, 한 트랜잭션으로 적용 (실패 시 전체 롤백)
- **삭제(DROP / DELETE / TRUNCATE / 컬럼 삭제)**: Claude 가 실행하지 않는다
  1. 영향 받는 데이터를 `db/backup_{날짜}_{설명}.sql` 로 백업
  2. 삭제 SQL 을 별도 파일로 작성
  3. 사용자가 Supabase 대시보드 > SQL Editor 에서 직접 실행하도록 안내
- 적용 후 제약조건이 실제로 막는지 **롤백되는 테스트 INSERT** 로 확인 (SAVEPOINT 사용, 흔적 남기지 않음)

## 4. 코드 반영
- 매퍼 XML 의 SQL, VO, 관리자 CSV 양식(`frontend/public/templates/`) 중 영향 받는 곳 수정
- 관리자 화면 현황(`AdminData_SQL_postgres.xml` 의 `selectSummary`)에 새 테이블 건수가 필요하면 추가
- 사용자에게 테이블별 역할과 데이터가 들어가는 순서를 쉬운 말로 보고
