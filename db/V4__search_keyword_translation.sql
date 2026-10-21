-- =====================================================================
-- V4: 한글 향수 검색어 → 영문 검색어 변환 기록
--   Fragella 는 영문으로만 검색되므로, 한글 검색어를 AI(Gemini)로 한 번 변환하고
--   결과를 저장해 두었다가 같은 검색어는 다시 AI 를 부르지 않고 쓴다.
--   관리자가 CSV 로 직접 넣거나 고친 값(ADMIN)은 AI 결과보다 우선한다.
-- =====================================================================
BEGIN;

CREATE TABLE search_keyword_translation (
    search_keyword_translation_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keyword_ko                     VARCHAR(100) NOT NULL CHECK (keyword_ko <> ''),
    keyword_en                     VARCHAR(200) NOT NULL CHECK (keyword_en <> ''),
    source_type                    VARCHAR(10)  NOT NULL CHECK (source_type IN ('AI', 'ADMIN')),
    hit_count                      INT          NOT NULL DEFAULT 0,
    created_at                     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_search_keyword_translation_ko ON search_keyword_translation (keyword_ko);
COMMENT ON TABLE search_keyword_translation IS '한글 향수 검색어 → 영문 검색어 변환 기록 (AI 변환 결과 저장 · 관리자 수정)';
COMMENT ON COLUMN search_keyword_translation.keyword_ko IS '한글 검색어 (앞뒤 공백 제거 · 공백 하나로 · 소문자)';
COMMENT ON COLUMN search_keyword_translation.keyword_en IS 'Fragella 로 보낼 영문 검색어 (브랜드 · 향수 공식 영문명)';
COMMENT ON COLUMN search_keyword_translation.source_type IS 'AI: Gemini 자동 변환, ADMIN: 관리자 입력 (AI 가 덮어쓰지 않음)';
COMMENT ON COLUMN search_keyword_translation.hit_count IS '저장된 변환을 다시 쓴 횟수';

ALTER TABLE search_keyword_translation ENABLE ROW LEVEL SECURITY;

COMMIT;
