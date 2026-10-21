# SCENT STUDIO platform — 프로젝트 규칙

이 파일은 Claude가 이 프로젝트에서 작업할 때 **항상** 지키는 규칙이다. 규칙과 다르게 해야 할 이유가 있으면 코드를 바꾸기 전에 먼저 사용자에게 묻는다.

## 1. 프로젝트 개요

| 구분 | 기술 | 위치 / 포트 |
|---|---|---|
| Frontend | Vue 3 (`<script setup>`) + Vite 4 + Vue Router + Pinia + Axios, Node 16 | `frontend/` · 5173 |
| Backend | Spring Boot 2.7 + 전자정부 표준프레임워크 4.3 + MyBatis, Java 8, Maven | `backend/` · 8090 |
| DB | Supabase (PostgreSQL 17) | 접속정보: `backend/.../egovProps/globals-local.properties` |
| 외부 API | Fragella (향수 검색 · 비슷한 향수), Google Gemini (한글 검색어 → 영문) | https://api.fragella.com/docs.html · https://ai.google.dev/api/generate-content |

- 개발 중 요청 흐름: 브라우저 → Vite(5173) `/api` 프록시 → Spring Boot(8090) → MyBatis → Supabase
- 8080 은 사용자의 다른 프로젝트(hiucs_frontend)가 쓰므로 백엔드는 8090.

## 2. 프론트엔드 규칙

### 폴더 역할
| 폴더 | 역할 |
|---|---|
| `src/views/` | 라우트 1개 = 페이지 1개. 데이터를 준비하고 컴포넌트를 조립한다. 하위 폴더 허용 (`views/admin/`) |
| `src/components/{common,layout,업무명}/` | 여러 곳에서 쓰는 화면 부품. props 로 받고 emit 으로 알린다 |
| `src/api/*.js` | **axios 호출 정의만** (아래 형식) |
| `src/utils/*.js` | 순수 계산·변환 함수 (예: `scentProfile.js`, `fragella.js`) |
| `src/stores/*.js` | Pinia 스토어 (여러 화면이 같이 쓰는 데이터) |
| `src/assets/styles/base.css` | 색상·폰트 토큰(`--c-*`, `--f-*`), 리셋 |

### API 파일 (`src/api/*.js`) — 절대 규칙
- **axios 호출 정의만 둔다.** 목업 데이터, 검색·필터·정렬, sessionStorage, FormData 생성 같은 로직을 넣지 않는다.
- 공통 헤더·타임아웃·에러 로그는 `api/http.js` 의 axios 설정(인터셉터)에서 처리한다.
- 형식:
  ```js
  import axios from 'axios'
  import { API_BASE } from './http'

  /**
   * [기능명]
   * 메뉴 > 경로
   * */
  const getXxxList = (curPage = 1, perPage = '10', keyword = '') => {
    return axios.get(`${API_BASE}/xxx-list/${curPage}?perPage=${perPage}&keyword=${encodeURIComponent(keyword)}`)
  }

  export { getXxxList }
  ```
- 호출하는 쪽은 아래 **API 호출 틀**을 쓴다.
- 사용자가 입력하는 값(검색어 등)은 `encodeURIComponent` 로 감싼다.

### API 호출 틀 (View · 컴포넌트 · 스토어) — 절대 규칙
백엔드는 항상 `{ code, message, response }` 로 응답한다 (4-1 참고). 호출은 `async/await` 대신 아래 틀로 쓴다.
```js
const loadDetail = () => {
  loading.value = true
  getXxxDtl(xxxSn)
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        content.value = response
        break
      case 101:
        // 데이터 없음: 목록이면 빈 목록, 상세면 안내
        content.value = null
        break
      default:
        errorMessage.value = message
        break
      }
    })
    .catch((err) => {
      errorMessage.value = '예기치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      console.log(err)
    })
    .finally(() => {
      loading.value = false
    })
}
```
- `case` 는 `switch` 와 같은 들여쓰기. `case` 안에서 변수를 선언하면 `case 100: { ... }` 블록으로 감싼다.
- `.catch` 는 네트워크 오류·HTTP 401 등. 관리자 키 오류(401)는 `err.response.status === 401` 로 확인해 로그인 화면으로.

### 임시(목업) 데이터 — 절대 규칙
- 필요하면 **그 화면의 View 파일 `<script setup>` 안에 변수(상수)로** 선언한다. 별도 mocks 파일, api 파일, 백엔드에 두지 않는다.
- 목업의 모양은 **나중에 받을 실제 API 응답과 필드명·구조·타입까지 똑같이** 만든다. 외부 API 는 공식 문서의 응답 예시로 확인하고, 확인한 출처를 주석에 적는다.
- 목업을 쓰는 자리에는 `// TODO: API 연동 후 교체` 주석을 단다.
- 실존 브랜드·제품에 사실과 다를 수 있는 값을 넣지 않는다 (자체 제품 또는 가상 브랜드 사용).
- 외부 API 응답 → 화면용 형태 변환은 `utils/` 의 변환 함수(예: `fromFragella`)로 한다. 목업이든 실제 응답이든 같은 함수를 쓴다.

### 줄바꿈 (JS · Vue)
- 한 줄은 **100자 이내**. 넘으면 나눈다.
- 속성이 3개 이상이거나 한 줄이 길어지는 태그는 **속성 하나당 한 줄**:
  ```html
  <input
    v-model="keyInput"
    type="password"
    placeholder="관리자 키"
    aria-label="관리자 키"
  />
  ```
- 객체 리터럴·함수 인자가 한 줄에 안 들어가면 **항목 하나당 한 줄**, 긴 조건식은 이름 붙인 변수로 나눈다
  (`const nameChanged = ...`, `const colorChanged = ...` → `return nameChanged || colorChanged`).
- 체이닝은 `.then` / `.catch` / `.finally` 를 각각 새 줄에서 시작한다.

### 컴포넌트·스타일
- Vue 3 `<script setup>` + `<style scoped>`. 색·폰트는 `base.css` 토큰(`var(--c-brown-600)` 등)을 쓴다.
- 반응형 기준: `max-width: 1023px`(태블릿·모바일 헤더), `767px`(모바일). 모바일 화면도 항상 함께 만든다.
- 버튼은 `<button type="button">`, 클릭 가능한 카드는 `role="button" tabindex="0"` + Enter/Space 처리. 이미지·아이콘에는 alt / `aria-hidden`.

## 3. 백엔드 규칙 (전자정부 표준프레임워크 방식)

### 패키지·파일 구조 (업무 모듈 단위)
```
kr.co.seedssoft.platform.{업무}/
├─ web/{업무}Controller.java               @RestController + 내부 클래스 API (엔드포인트 상수)
├─ service/{업무}Service.java              인터페이스, 메서드는 throws Exception
├─ service/impl/{업무}ServiceImpl.java     extends EgovAbstractServiceImpl, @Service("{업무}Service")
├─ dao/{업무}Dao.java                      @Mapper("{업무}Dao") 인터페이스
├─ vo/{테이블명}.java                      테이블 1:1 (예: perfume_ingredient → PerfumeIngredient)
└─ dto/{용도}.java                         테이블과 다른 형태 (목록+집계, 요청/응답, CSV 한 줄 등)
resources/egovframework/mapper/platform/{업무}/{업무}_SQL_postgres.xml
                                          namespace="kr.co.seedssoft.platform.{업무}.dao.{업무}Dao"
kr.co.seedssoft.platform.cmmn/            공통 (response, web, util)
```

### 클래스 헤더 주석 — 절대 규칙
- **모든 Java 클래스 · 인터페이스**(Controller, Service, ServiceImpl, DAO, VO, DTO, config, util) 선언 바로 위에 헤더를 단다:
  ```java
  /**
   * @ClassName : PerfumeController.java
   * @Description : 향수 탐색 (공개 API).
   *                둘째 줄부터는 이렇게 들여써서 이어 쓴다.
   * @author : 이건상
   * @since : 2026. 09. 27.
   */
  ```
  - `@since` 는 파일을 만든 날짜 (수정할 때 바꾸지 않는다)

### DAO — 절대 규칙
- DAO 는 `{업무}/dao/` 패키지의 **`@Mapper` 인터페이스** (`org.egovframe.rte.psl.dataaccess.mapper.Mapper`).
  구현 클래스를 만들지 않는다 — `EgovConfigAppMapper` 의 `@MapperScan("kr.co.seedssoft.platform.*.dao")` 가 자동 생성.
- **메서드 이름 = SQL XML 의 id**, **XML namespace = 인터페이스 전체 경로**.
  둘이 어긋나면 서버는 정상으로 뜨고, **그 메서드를 호출하는 순간** `Invalid bound statement (not found)` 에러가 난다
  → 새 DAO 메서드를 만들면 반드시 실제로 호출해서 확인한다. (XML 문법·resultType 오타는 서버 시작 때 에러)
- 파라미터는 VO·DTO 하나 (또는 값 하나). 값이 여러 개면 VO·DTO 로 묶는다.
- 형식 (들여쓰기 2칸, 메서드 사이 빈 줄):
  ```java
  package kr.co.seedssoft.platform.perfume.dao;

  import java.util.List;

  import kr.co.seedssoft.platform.perfume.vo.ScentFamily;
  import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

  /**
   * @ClassName : PerfumeDao.java
   * @Description : 향수 탐색 DAO (SQL: mapper/platform/perfume/Perfume_SQL_postgres.xml)
   * @author : 이건상
   * @since : 2026. 09. 27.
   */
  @Mapper("PerfumeDao")
  public interface PerfumeDao {

    List<ScentFamily> selectNoteFamilyList();
  }
  ```
- ServiceImpl 에서 `@Resource(name = "PerfumeDao")` 로 주입.

### VO · DTO — 절대 규칙
- **VO = 테이블 1:1.** 클래스명은 테이블명을 PascalCase 로 (접미사 없음), 필드는 테이블 컬럼 전부(camelCase). 테이블에 없는 필드를 넣지 않는다.
- **DTO = 테이블과 다른 모든 것** (다른 테이블 값·집계가 섞인 목록, 화면용 묶음, 업로드 결과, 외부 API 응답 등). 클래스명은 용도 (`ScentFamilyList`, `ImportResult`, `AdminSummary`).
- 여러 모듈이 쓰는 테이블의 VO 는 그 테이블을 주로 다루는 모듈에 둔다 (향 관련 테이블 → `perfume/vo`).
- 형식 (들여쓰기는 **탭**):
  ```java
  package kr.co.seedssoft.platform.perfume.vo;

  import lombok.*;

  /**
   * @ClassName : PerfumeAccord
   * @Description : 향수별 메인 어코드 (perfume_accord)
   * @author : 이건상
   * @since : 2026. 09. 27.
   */

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  public class PerfumeAccord {
  	private Long perfumeId; // 향수ID : perfume
  	private String accordName; // 어코드명
  	private Integer strength; // 강도 (0~100)
  }
  ```
  - 모든 필드 끝에 `// 한글 설명`, 다른 테이블을 가리키면 `: 테이블명`
  - `@since` 는 파일을 만든 날짜, `@author` 는 `이건상`
- MyBatis `resultType` / `parameterType` 은 VO·DTO 전체 경로. `Map` 이나 `egovMap` 으로 주고받지 않는다.

### 컨트롤러 형식 — 절대 규칙 (hiucs 와 동일)
**① 엔드포인트는 클래스 안 `API` 내부 클래스에 상수로 미리 정의**한다. 클래스에 `@RequestMapping` 을 붙이지 않는다.
```java
@RestController
public class AdminDataController {

  @Resource(name = "adminDataService")
  private AdminDataService adminDataService;

  public static class API {
    public static final String BASE = "/api/admin";
    public static final String BASE_PATTERN = BASE + "/**";

    /* 데이터 현황 · 향 계열 */
    public static final String GET_FAMILY_LIST = BASE + "/families"; // 향 계열 목록
    public static final String PUT_FAMILY = BASE + "/families/{familyCode}"; // 향 계열 한글명 · 색상 수정
  }
```
- 상수명: `{HTTP 메서드}_{대상}` (`GET_`, `POST_`, `PUT_`, `PATCH_`, `DELETE_`), 목록은 `_LIST`. 업무 묶음마다 `/* 제목 */`, 줄 끝에 `// 설명`
- 매핑은 상수로: `@GetMapping(API.GET_FAMILY_LIST)`
- 경로 패턴이 필요한 설정(인터셉터 등)은 `XxxController.API.BASE_PATTERN` 을 쓴다 (예: `EgovConfigWeb` 의 관리자 키 인터셉터)

**② API(요청 처리 메서드)마다 hiucs 주석 블록** + **파라미터 블록 형식**:
```java
  /**
   * @Program Name : updateFamily()
   * @Describe : 관리자 > 데이터 관리 > 향 계열 한글명 · 색상 수정
   * @Param1 : familyCode(String) 계열 코드 (예: WOODY)
   * @Param2 : scentFamily(ScentFamily) 수정 정보 (familyNameKo, hexColorCode)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @PutMapping(API.PUT_FAMILY)
  public ResponseEntity<CommonResponseBody<Void>> updateFamily(
    @PathVariable String familyCode,
    @RequestBody ScentFamily scentFamily
  ) throws Exception {
    scentFamily.setFamilyCode(familyCode);
    CommonResponseCode responseCode = adminDataService.updateFamily(scentFamily)
        ? CommonResponseCode.COMMON_100
        : CommonResponseCode.COMMON_101;
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode));
  }
```
- `@Describe` 는 `메뉴 > 화면 > 기능`. `@Param1, @Param2 ...` 는 `이름(타입) 설명` (파라미터가 없으면 생략)
- `@History` 는 `날짜 | 작성자 | 내용`. 나중에 고치면 아래 줄에 추가 (`@History :` 뒤와 같은 위치로 맞춤):
  ```
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   *            2026. 10. 11. | 이건상 | 향료 별칭 · 어코드→계열 추가
  ```
- **ServiceImpl 의 `@Override` 메서드에도 같은 주석 블록**을 단다. Service 인터페이스는 메서드 위에 `/* 한 줄 설명 */` 만.
- 메서드 선언 다음 줄부터 파라미터 **하나당 한 줄**(메서드보다 2칸 더), `) throws Exception {` 는 따로 한 줄. 파라미터가 없어도 `(` 줄바꿈 `)` 형태.
- 본문도 메서드보다 2칸 더 들여쓴다.
- 파라미터가 3개 이상이거나 한 줄이 120자를 넘는 **일반 메서드**도 같은 방식으로 파라미터를 나눈다.
- 한 줄 120자 이내. 긴 조건식·삼항식은 연산자 앞에서 줄을 나눈다.

### 응답 형식 — 절대 규칙
- 모든 API 는 `ResponseEntity.ok(new CommonResponseBody<>(코드, 데이터))` → `{ code, message, response }`.
- 코드는 `cmmn/response/CommonResponseCode` (hiucs 와 같은 번호): `100` 정상, `101` 데이터 없음(빈 목록·없는 대상), `102` DB 오류(DB 연결 실패 포함), `103` 파라미터 오류, `104` 필수값 누락, `105~107` 등록·수정·삭제 실패, `108` 파일 업로드 실패, `132` 이미 존재, `140` 권한 없음, `199` 알 수 없는 오류.
- 외부 API 코드 (이 프로젝트에서 추가): `150` 외부 API 호출 실패(키 오류 · 상대 서버 오류 · 연결 실패), `151` 외부 API 월 사용량 초과, `152` 한글 검색어 → 영문 변환 실패(Gemini 오류 · 키 없음). Service 는 RestTemplate 예외를 그대로 던지고, **Controller 가 try/catch 로 101 처럼 직접 코드를 정해** 응답한다
  (`HttpClientErrorException.TooManyRequests` → 151, 그 밖의 `RestClientException` → 150). 화면은 `case 150` / `case 151` 에서 자체 문구를 **토스트**(`useToastStore().show()`, 3초)로 띄운다.
- 결과는 HTTP 200 + code 로 알린다. 예외는 `cmmn/web/GlobalExceptionHandler` 가 코드로 바꾼다 (`IllegalArgumentException` → 103 + 그 메시지). 예외: 관리자 키 오류는 HTTP 401 + code 140.
- 목록 API 는 빈 목록이면 `101` + 빈 배열.
- 주입은 `@Resource(name = "빈이름")`.
- 들여쓰기: **Controller · Service · ServiceImpl · DAO 는 2칸 (hiucs 와 동일)**, VO · DTO 는 탭, 그 밖(config · cmmn)은 4칸.
- SQL 은 **XML 매퍼에만** 쓴다 (Java 에 SQL 문자열 금지).
- 트랜잭션: `service.impl` 패키지의 `*Impl` 메서드는 `EgovConfigAppTransaction` 이 자동으로 트랜잭션을 건다 (`@Transactional` 불필요, Exception 발생 시 롤백).
  단 외부 API 만 부르는 `Fragella*Impl` · `Gemini*Impl` 은 제외 (응답을 기다리는 동안 DB 연결을 붙잡지 않도록). 외부 API 전용 서비스를 새로 만들면 포인트컷 제외에 추가한다.
- 백엔드는 **DB 처리와 외부 API 호출만** 한다. 목업 데이터 파일·목업 서비스 로직을 두지 않는다.

### 설정·비밀값
- 업무 설정: `resources/egovframework/egovProps/globals.properties` (`Globals.*` 키, git 에 올라감 → 비밀값 금지)
- 비밀값(DB 비밀번호, 관리자 키, 외부 API 키): `globals-local.properties` (git 제외). 새 키를 추가하면 `.example` 파일에도 자리표시자를 추가한다.
- Spring Boot 자체 설정(포트, 로그, 업로드 크기)만 `application.yml`.
- 비밀값을 채팅·로그·커밋에 노출하지 않는다.

### 외부 API — Fragella
- 호출은 백엔드만 한다 (`perfume/service/impl/FragellaServiceImpl`, RestTemplate 빈 `fragellaRestTemplate` = `config/EgovConfigAppFragella`).
  화면은 `/api/perfume/fragrances?keyword=`, `/api/perfume/fragrances/similar?name=` 를 부른다.
- 키: `Globals.Fragella.ApiKey` (globals-local.properties, 비밀값). 공개 위젯 키(`pub_...`)와 도메인 설정은 쓰지 않는다.
- **User-Agent 헤더 필수**: Java 기본 UA 는 Fragella 앞단(Cloudflare)이 403 `error code: 1010` 으로 막는다.
- 응답 DTO(`perfume/dto/Fragrance` 등)는 `@JsonProperty` 로 **Fragella 필드명 그대로** 주고받는다 → 화면은 `utils/fragella.js` 의 `fromFragella()` 로 변환.
- **사용량**: 호출 1번 = 월 사용량 1회 (무료 20회/월, `GET /api/v1/usage` 로 확인 — 이 조회는 사용량에 안 셈).
  - 검색은 Enter · 버튼을 누를 때만 한다 (타이핑마다 검색 금지). 향수 1개 분석 = 검색 1 + 비슷한 향수 1.
  - 테스트는 꼭 필요한 만큼만 실제로 호출하고, 화면 확인은 저장해 둔 응답을 돌려주는 방식(요청 가로채기)으로 한다.
- 약관상 Fragella 데이터를 DB 에 대량 저장·캐시하지 않는다. DB 에는 우리 기준 데이터(향료 사전 · 별칭 · 어코드→계열)만 둔다.
- 노트 · 어코드 → 계열 매핑: `ingredient`(+`ingredient_alias`) 와 `accord_family_map`. 관리자 화면 CSV 로 추가 · 수정 (`db/seed/` 에 올린 기본 데이터 보관).

### 외부 API — Gemini (한글 검색)
- Fragella 는 영문으로만 검색된다 → 한글 검색어는 `PerfumeServiceImpl.selectSearchKeyword()` 가 영문으로 바꾼다.
  1. `search_keyword_translation` 에 저장된 변환이 있으면 그 값 (관리자 CSV `ADMIN` 값이 AI 값보다 우선)
  2. 없으면 `GeminiServiceImpl` 로 변환 → `AI` 로 저장 (같은 검색어는 다시 AI 를 부르지 않음)
- 모델 · 주소: `Globals.Gemini.Model` · `Globals.Gemini.BaseUrl` (globals.properties), 키: `Globals.Gemini.ApiKey` (globals-local.properties). 헤더 `x-goog-api-key`.
- 검색 응답은 `FragranceSearchList { keyword, searchKeyword, translated, fragrances }` → 화면은 "○○(으)로 검색했어요" 를 보여준다.
- 한글 검색어 최소 2자, 영문 3자. 변환 결과가 향수 검색어가 아니면 저장하지 않고 101.

### API 공통
- 관리자 API 는 `/api/admin/**` → `AdminKeyInterceptor` 가 `X-Admin-Key` 검사.
- 에러 응답에 스택 트레이스를 싣지 않는다 (`server.error.include-stacktrace: never` 유지).

## 4. DB 규칙 (Supabase)
- 테이블·컬럼명: **소문자 snake_case** (`perfume`, `perfume_ingredient`). 대문자·따옴표 이름 금지.
- PK 는 `BIGINT GENERATED ALWAYS AS IDENTITY`, 날짜는 `TIMESTAMPTZ DEFAULT now()`.
- 모든 테이블 `ENABLE ROW LEVEL SECURITY` (정책 없음 → 공개 REST API 차단, 백엔드만 접근).
- 스키마 변경은 `db/V{번호}__{설명}.sql` 파일로 남긴다.
- **삭제(DROP/DELETE/TRUNCATE) 는 Claude 가 실행하지 않는다.** SQL 파일로 만들어 사용자가 Supabase SQL Editor 에서 실행한다. 실행 전 백업 SQL 을 `db/` 에 남긴다.

## 5. 작업 방식
- 사용자는 문법을 잘 모른다: 설명은 **한국어로 쉽게**, 코드 설명 요청에는 실제 코드 + 한 줄씩 주석으로 답한다.
- 외부 서비스의 데이터·약관·응답 형식은 **공식 문서로 확인한 뒤** 말하고, 확인 못 한 것은 "확인 못 함"이라고 한다.
- 약관상 금지된 크롤링(예: Fragrantica)은 하지 않는다.
- 작업 후 확인:
  - 프론트: `cd frontend && npm run build`
  - 백엔드: `cd backend && mvn -q -B compile` (실행 중인 서버는 devtools 로 자동 재시작)
  - 화면 작업은 실제 브라우저로 데스크톱(1440)·모바일(390) 확인
- 파이썬 등으로 소스를 일괄 수정할 때 이스케이프(`\b`, `\w` 등)가 깨지지 않았는지 확인한다.
  이 환경에서는 셸 heredoc 안의 역슬래시·따옴표가 변형되므로, 수정 스크립트는 **파일로 저장한 뒤 실행**한다.
- 한글이 든 JSON 을 터미널 curl 로 보내면 인코딩이 깨져 103 이 나온다 → Python urllib 등으로 UTF-8 로 보내서 테스트한다.
- 줄 길이 점검: JS·Vue 100자, Java 120자를 넘는 줄이 없는지 확인한다 (한글도 한 글자로 센다).

## 6. 미정 (사용자 결정 대기 — 임의로 바꾸지 말 것)
- **Service 반환 형식**: 지금 Service 는 데이터를 그대로 반환하고 응답 코드는 Controller 가 정한다. hiucs 처럼 Service 가 `ResultWithCode` 를 반환하게 할지 검토 중.
