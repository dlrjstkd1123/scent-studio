---
name: publish-page
description: SCENT STUDIO platform 에 새 화면(페이지)을 퍼블리싱하는 절차 — 시안 이미지 기반 Vue 페이지 제작, 목업 데이터를 View 변수로 두기, 데스크톱·모바일 확인. 시안/디자인을 보고 화면을 만들거나 API 연동 전 화면을 먼저 만들 때 사용.
---

# 화면 퍼블리싱 절차

프로젝트 규칙은 루트 `CLAUDE.md` 가 기준이다.

## 1. 시작 전
- 시안에서 **사용자가 빼거나 바꾸라고 한 요소**를 먼저 정리해서 말한다 (시안을 그대로 따르지 않는 부분)
- 데이터가 어디서 오는지 확인: 우리 DB / 외부 API / 아직 없음
- 외부 API 데이터라면 **공식 문서의 응답 예시를 먼저 확인**하고, 응답 형식 표를 사용자에게 보여준다

## 2. 파일 배치
| 만들 것 | 위치 |
|---|---|
| 페이지 | `src/views/{이름}View.vue` + `src/router/index.js` 에 lazy route (`() => import(...)`) |
| 여러 곳에서 쓸 부품 | `src/components/{업무}/` |
| 계산·변환 함수 | `src/utils/` |
| 서버 호출 | `src/api/{업무}.js` (axios 정의만) |
| 헤더 메뉴 연결 | `components/layout/SiteHeader.vue` 의 `NAV` |

## 3. 목업 데이터 (API 연동 전)
- **View 파일 `<script setup>` 안에 상수로** 선언: `const MOCK_XXX = [...]`
- 실제 API 응답과 **필드명·중첩 구조·타입(문자열/숫자)까지 동일**하게. 주석에 근거 문서 URL 과 `TODO: API 연동 후 교체`
- 실존 제품에 부정확한 값 금지 → 자체 제품·가상 브랜드
- 응답 → 화면 형태 변환은 `utils/` 함수로 (연동 후에도 재사용)

## 4. 디자인
- `base.css` 토큰 사용: 색 `--c-brown-*`, `--c-cream`, `--c-ivory`, `--c-muted`, 폰트 `--f-serif-kr`, `--f-serif-en`, `--f-sans`
- 공통 헤더 `SiteHeader` 재사용 (데스크톱은 fixed 76px → 첫 섹션에 padding-top)
- 반응형: `@media (max-width: 1023px)`, `@media (max-width: 767px)` — 모바일 필수
- 차트가 있으면 `dataviz` 스킬을 먼저 읽는다. 색만으로 구분하지 말고 이름·값을 함께 표시
- 이미지: `src/assets/images/{업무}/`, webp, 사진이 없으면 임시 이미지임을 사용자에게 알린다

## 5. 확인
1. `cd frontend && npm run build`
2. 브라우저 확인 (puppeteer-core 스크립트는 scratchpad 에 둔다, 프로젝트 의존성에 추가하지 않음)
   - 데스크톱 1440×900, 모바일 390×844
   - 주요 상호작용(검색·클릭·필터) 후 상태 캡처, 콘솔 에러 0 확인
3. 캡처를 직접 보고 겹침·잘림·빈 공간을 고친 뒤 보고
