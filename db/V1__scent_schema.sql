-- =====================================================================
-- SCENT STUDIO 향수/향료 스키마 (Supabase PostgreSQL)
-- 2026-09-23  기존 "Perfumes" / "Ingredients" / "Perfume_Ingredient_Map" 을 대체
--             (기존 테이블 삭제는 V2__drop_old_tables.sql 에서 따로 실행)
--
-- 테이블/컬럼명은 소문자 snake_case (PostgreSQL 관례, SQL 에서 따옴표 불필요)
-- 모든 테이블 RLS 활성화 + 정책 없음 → Supabase 공개 REST API 로는 접근 불가,
-- 백엔드(postgres 계정, BYPASSRLS)로만 읽고 쓴다.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 향 계열: 계열별 색상을 한 곳에서 관리
-- ---------------------------------------------------------------------
CREATE TABLE scent_family (
    family_code     VARCHAR(30)  PRIMARY KEY,                 -- CITRUS, WOODY ...
    family_name_en  VARCHAR(50)  NOT NULL,
    family_name_ko  VARCHAR(50),
    hex_color_code  CHAR(7)      NOT NULL CHECK (hex_color_code ~ '^#[0-9A-Fa-f]{6}$'),
    sort_order      SMALLINT     NOT NULL DEFAULT 0
);
COMMENT ON TABLE scent_family IS '향 계열 (시각화 색상 포함)';

-- ---------------------------------------------------------------------
-- 향료(노트) 마스터
-- ---------------------------------------------------------------------
CREATE TABLE ingredient (
    ingredient_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ingredient_name_en  VARCHAR(100) NOT NULL,
    ingredient_name_ko  VARCHAR(100),
    family_code         VARCHAR(30)  REFERENCES scent_family (family_code),
    default_note_type   VARCHAR(6)   CHECK (default_note_type IN ('TOP', 'MIDDLE', 'BASE')),  -- 일반적인 휘발도
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_ingredient_name_en ON ingredient (lower(ingredient_name_en));
CREATE INDEX ix_ingredient_family ON ingredient (family_code);
COMMENT ON TABLE ingredient IS '향료(노트) 마스터';
COMMENT ON COLUMN ingredient.default_note_type IS '일반적인 휘발도. 향수별 실제 위치는 perfume_ingredient.note_type';

-- ---------------------------------------------------------------------
-- 향수 마스터
-- ---------------------------------------------------------------------
CREATE TABLE perfume (
    perfume_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand_name     VARCHAR(100) NOT NULL,
    perfume_name   VARCHAR(200) NOT NULL,
    concentration  VARCHAR(20),                               -- EDP, EDT, Parfum ...
    release_year   SMALLINT     CHECK (release_year BETWEEN 1800 AND 2100),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_perfume_brand_name ON perfume (lower(brand_name), lower(perfume_name));
COMMENT ON TABLE perfume IS '향수 마스터';

-- ---------------------------------------------------------------------
-- 향수별 노트 피라미드 (탑/미들/베이스는 향수마다 다르므로 여기에)
-- ---------------------------------------------------------------------
CREATE TABLE perfume_ingredient (
    perfume_id     BIGINT      NOT NULL REFERENCES perfume (perfume_id) ON DELETE CASCADE,
    ingredient_id  BIGINT      NOT NULL REFERENCES ingredient (ingredient_id),
    note_type      VARCHAR(6)  NOT NULL CHECK (note_type IN ('TOP', 'MIDDLE', 'BASE')),
    sort_order     SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (perfume_id, ingredient_id, note_type)
);
CREATE INDEX ix_perfume_ingredient_ingredient ON perfume_ingredient (ingredient_id);
COMMENT ON TABLE perfume_ingredient IS '향수-향료 매핑 (노트 피라미드)';

-- ---------------------------------------------------------------------
-- 향수별 메인 어코드
-- ---------------------------------------------------------------------
CREATE TABLE perfume_accord (
    perfume_id   BIGINT       NOT NULL REFERENCES perfume (perfume_id) ON DELETE CASCADE,
    accord_name  VARCHAR(50)  NOT NULL,
    strength     SMALLINT     CHECK (strength BETWEEN 0 AND 100),   -- 강도(%)
    PRIMARY KEY (perfume_id, accord_name)
);
COMMENT ON TABLE perfume_accord IS '향수별 메인 어코드';

-- ---------------------------------------------------------------------
-- IFRA 규제 (공식 엑셀 업로드로 채움)
-- 규제는 화학 물질(CAS) 단위 + 제품 카테고리별로 제한치가 다르다
-- ---------------------------------------------------------------------
CREATE TABLE ifra_standard (
    standard_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_name     VARCHAR(300) NOT NULL,
    cas_numbers       VARCHAR(300),                            -- 여러 개면 콤마 구분
    restriction_type  VARCHAR(30),                             -- Prohibition / Restriction / Specification
    amendment_no      SMALLINT,                                -- IFRA 개정 차수 (예: 51)
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_ifra_standard_material ON ifra_standard (lower(material_name));
COMMENT ON TABLE ifra_standard IS 'IFRA 규제 물질';

CREATE TABLE ifra_limit (
    standard_id    BIGINT        NOT NULL REFERENCES ifra_standard (standard_id) ON DELETE CASCADE,
    category_code  VARCHAR(5)    NOT NULL,                     -- 1, 2, 3, 4, 5A ... 12
    max_percent    NUMERIC(9, 4),                              -- 최대 허용 농도(%). NULL = 제한 없음
    is_prohibited  BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (standard_id, category_code)
);
COMMENT ON TABLE ifra_limit IS 'IFRA 제품 카테고리별 제한치';

-- 향료 ↔ 규제 물질 연결 (이름 자동 매칭은 부정확 → 관리자가 확인)
CREATE TABLE ingredient_ifra_map (
    ingredient_id  BIGINT       NOT NULL REFERENCES ingredient (ingredient_id) ON DELETE CASCADE,
    standard_id    BIGINT       NOT NULL REFERENCES ifra_standard (standard_id) ON DELETE CASCADE,
    verified       BOOLEAN      NOT NULL DEFAULT FALSE,        -- 관리자 확인 여부
    PRIMARY KEY (ingredient_id, standard_id)
);
COMMENT ON TABLE ingredient_ifra_map IS '향료-IFRA 규제 물질 매핑';

-- ---------------------------------------------------------------------
-- 관리자 데이터 업로드 이력
-- ---------------------------------------------------------------------
CREATE TABLE admin_import_log (
    log_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    import_type     VARCHAR(30)  NOT NULL,                     -- PERFUME_CSV, IFRA_EXCEL, FAMILY_COLOR
    file_name       VARCHAR(255),
    total_count     INT          NOT NULL DEFAULT 0,
    inserted_count  INT          NOT NULL DEFAULT 0,
    updated_count   INT          NOT NULL DEFAULT 0,
    failed_count    INT          NOT NULL DEFAULT 0,
    message         TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
COMMENT ON TABLE admin_import_log IS '관리자 데이터 업로드 이력';

-- ---------------------------------------------------------------------
-- RLS: 공개 API 차단 (정책 없음)
-- ---------------------------------------------------------------------
ALTER TABLE scent_family        ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredient          ENABLE ROW LEVEL SECURITY;
ALTER TABLE perfume             ENABLE ROW LEVEL SECURITY;
ALTER TABLE perfume_ingredient  ENABLE ROW LEVEL SECURITY;
ALTER TABLE perfume_accord      ENABLE ROW LEVEL SECURITY;
ALTER TABLE ifra_standard       ENABLE ROW LEVEL SECURITY;
ALTER TABLE ifra_limit          ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredient_ifra_map ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_import_log    ENABLE ROW LEVEL SECURITY;

-- ---------------------------------------------------------------------
-- 기본 데이터: 향 계열 + 색상
-- ---------------------------------------------------------------------
INSERT INTO scent_family (family_code, family_name_en, family_name_ko, hex_color_code, sort_order) VALUES
    ('CITRUS',   'Citrus',   '시트러스', '#FFEB3B', 1),
    ('FRUITY',   'Fruity',   '프루티',   '#FF8A65', 2),
    ('FLORAL',   'Floral',   '플로럴',   '#F48FB1', 3),
    ('GREEN',    'Green',    '그린',     '#66BB6A', 4),
    ('AROMATIC', 'Aromatic', '아로마틱', '#9CCC65', 5),
    ('AQUATIC',  'Aquatic',  '아쿠아틱', '#4FC3F7', 6),
    ('SPICY',    'Spicy',    '스파이시', '#E64A19', 7),
    ('WOODY',    'Woody',    '우디',     '#8D6E63', 8),
    ('AMBER',    'Amber',    '앰버',     '#FFA000', 9),
    ('GOURMAND', 'Gourmand', '구르망',   '#A1887F', 10),
    ('MUSK',     'Musk',     '머스크',   '#D7CCC8', 11),
    ('POWDERY',  'Powdery',  '파우더리', '#E1BEE7', 12),
    ('LEATHER',  'Leather',  '레더',     '#6D4C41', 13);

-- 기존 테스트 향료 이관
INSERT INTO ingredient (ingredient_name_en, ingredient_name_ko, family_code, default_note_type)
VALUES ('Bergamot', '베르가못', 'CITRUS', 'TOP');

COMMIT;
