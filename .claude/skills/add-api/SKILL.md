---
name: add-api
description: SCENT STUDIO platform 에 새 기능 API 를 추가하는 절차 (Controller → Service → ServiceImpl → DAO → SQL XML, 프론트 api/*.js axios 정의). 새 조회·등록·수정·삭제 API, 외부 API 연동 엔드포인트를 만들 때 사용.
---

# 새 API 추가 절차

프로젝트 규칙은 루트 `CLAUDE.md` 가 기준이다. 이 스킬은 그 규칙대로 파일을 만드는 순서다.

## 0. 먼저 정할 것
- 업무 모듈 이름 (예: `perfume`, `admin`) — 이미 있으면 그 모듈에 추가
- URL: `/api/{업무}/{기능}` (목록은 `/api/{업무}/{기능}-list/{curPage}` 형태 가능)
- DB 를 쓰는지, 외부 API 를 부르는지
- 테이블이 새로 필요하면 먼저 `db-change` 스킬

## 1. 백엔드
1. **VO / DTO** (형식은 `CLAUDE.md` "VO · DTO" — 탭 들여쓰기, Lombok 5개, 머리 주석 `@author : 이건상`, 필드마다 `// 설명`)
   - 테이블 1:1 → `{업무}/vo/{테이블 PascalCase}.java` (이미 있으면 재사용)
   - 그 외(목록+집계, 요청/응답 묶음, 외부 API 응답) → `{업무}/dto/{용도}.java`
2. **SQL** `resources/egovframework/mapper/platform/{업무}/{업무}_SQL_postgres.xml`
   - `namespace="kr.co.seedssoft.platform.{업무}.dao.{업무}Dao"`, `id` 는 `select/insert/update/delete + 대상`
   - `resultType` · 파라미터는 VO/DTO 전체 경로 (Map · egovMap 금지)
   - `INSERT ... RETURNING` 을 `<select>` 로 쓸 때 `flushCache="true" useCache="false"`
3. **DAO** `dao/{업무}Dao.java` — `@Mapper("{업무}Dao")` **인터페이스**, 메서드 선언만 (구현 없음, 들여쓰기 2칸)
   - 메서드 이름 = XML `id`, XML `namespace` = `kr.co.seedssoft.platform.{업무}.dao.{업무}Dao`
4. **Service 인터페이스** `service/{업무}Service.java` — 메서드마다 `throws Exception`, 한 줄 주석
5. **ServiceImpl** `service/impl/{업무}ServiceImpl.java` — `extends EgovAbstractServiceImpl implements {업무}Service`, `@Service("{업무}Service")`, `@Resource(name = "{업무}Dao")`
   - 트랜잭션은 자동 (Impl 메서드). 잘못된 입력은 `IllegalArgumentException("사유")` → 103 으로 응답됨
   - 외부 API 호출도 여기서. 키는 `@Value("${Globals.Xxx.ApiKey}")`, 값은 `globals-local.properties` (+ `.example` 자리표시자)
6. **Controller** `web/{업무}Controller.java` — `CLAUDE.md` "컨트롤러 형식" 그대로 (hiucs 와 동일)
   - 엔드포인트는 내부 클래스 `API` 에 상수로 먼저 추가 (`GET_XXX_LIST = BASE + "/xxx-list"; // 설명`) → `@GetMapping(API.GET_XXX_LIST)`
   - 메서드 주석: `@Program Name` · `@Describe` · `@Param1..` · `@Date` · `@Writer : 이건상` · `@History` (ServiceImpl 메서드도 동일, Service 인터페이스는 `/* 설명 */`)
   - 파라미터 블록 형식 (`(` 줄바꿈, 파라미터 한 줄씩, `) throws Exception {`)
   - `return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_100, 데이터));`
   - 빈 목록·없는 대상 → `COMMON_101`
7. 관리자 전용이면 URL 을 `/api/admin/...` 로 (인터셉터가 관리자 키 검사)

## 2. 프론트
1. `src/api/{업무}.js` 에 **axios 정의만** 추가:
   ```js
   /**
    * [기능명]
    * 메뉴 > 경로
    * */
   const getXxx = (param) => {
     return axios.get(`${API_BASE}/{업무}/xxx?param=${encodeURIComponent(param)}`)
   }
   ```
   파일 끝 `export { ... }` 에 추가
2. View · 컴포넌트에서는 `CLAUDE.md` "API 호출 틀" 그대로:
   `.then` → `const { code, message, response } = res.data` → `switch (parseInt(code))` (`case 100` / `case 101` / `default`) → `.catch` → `.finally`
3. 화면에 쓰던 목업 변수가 있으면 이 API 호출로 교체하고 `TODO` 주석 제거

## 3. 확인
- `cd backend && mvn -q -B compile` → 서버 재시작 후 `curl http://localhost:5173/api/...` 로 `code` 가 상황별(100/101/103 등)로 맞게 오는지 확인
- `cd frontend && npm run build`
- 한글 파라미터는 curl 대신 인코딩해서 테스트 (터미널 인코딩 깨짐 주의)
- DB 에 테스트 데이터를 넣었다면 끝나고 정리했는지 사용자에게 보고
