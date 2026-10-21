-- =====================================================================
-- V3: Fragella 응답의 어코드 · 노트 이름을 우리 향 계열에 연결하는 표
--   1) accord_family_map : 메인 어코드(citrus, fresh spicy ...) → 향 계열
--   2) ingredient_alias  : 같은 향료의 다른 이름(Cedar → Cedarwood) → 향료
-- 기본 데이터는 초안. 관리자 화면 CSV 업로드로 추가 · 수정한다.
-- =====================================================================
BEGIN;

CREATE TABLE accord_family_map (
    accord_family_map_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    accord_name           VARCHAR(50)  NOT NULL CHECK (accord_name = lower(trim(accord_name)) AND accord_name <> ''),
    family_code           VARCHAR(30)  NOT NULL REFERENCES scent_family (family_code),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_accord_family_map_name ON accord_family_map (accord_name);
COMMENT ON TABLE accord_family_map IS '메인 어코드 → 향 계열 매핑 (Fragella Main Accords 를 계열 비율에 반영)';
COMMENT ON COLUMN accord_family_map.accord_name IS '어코드명 (소문자, Fragella Main Accords 값 그대로)';
COMMENT ON COLUMN accord_family_map.family_code IS '향 계열 코드';

CREATE TABLE ingredient_alias (
    ingredient_alias_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alias_name           VARCHAR(100) NOT NULL CHECK (trim(alias_name) <> ''),
    ingredient_id        BIGINT       NOT NULL REFERENCES ingredient (ingredient_id) ON DELETE CASCADE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_ingredient_alias_name ON ingredient_alias (lower(alias_name));
CREATE INDEX ix_ingredient_alias_ingredient ON ingredient_alias (ingredient_id);
COMMENT ON TABLE ingredient_alias IS '향료 별칭 (외부 데이터의 다른 표기를 같은 향료로 묶음)';
COMMENT ON COLUMN ingredient_alias.alias_name IS '별칭 (영문, 대소문자 무시하고 비교)';
COMMENT ON COLUMN ingredient_alias.ingredient_id IS '가리키는 향료';

ALTER TABLE accord_family_map ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredient_alias  ENABLE ROW LEVEL SECURITY;

-- ---------------------------------------------------------------- 기본 데이터: 어코드 → 계열
INSERT INTO accord_family_map (accord_name, family_code) VALUES
    ('citrus', 'CITRUS'),
    ('fruity', 'FRUITY'), ('tropical', 'FRUITY'), ('cherry', 'FRUITY'), ('sour', 'FRUITY'),
    ('floral', 'FLORAL'), ('white floral', 'FLORAL'), ('yellow floral', 'FLORAL'),
    ('rose', 'FLORAL'), ('tuberose', 'FLORAL'), ('violet', 'FLORAL'),
    ('green', 'GREEN'), ('mossy', 'GREEN'), ('tea', 'GREEN'),
    ('aromatic', 'AROMATIC'), ('herbal', 'AROMATIC'), ('lavender', 'AROMATIC'),
    ('anis', 'AROMATIC'), ('camphor', 'AROMATIC'), ('terpenic', 'AROMATIC'),
    ('aquatic', 'AQUATIC'), ('marine', 'AQUATIC'), ('ozonic', 'AQUATIC'), ('fresh', 'AQUATIC'),
    ('salty', 'AQUATIC'), ('mineral', 'AQUATIC'), ('metallic', 'AQUATIC'),
    ('warm spicy', 'SPICY'), ('fresh spicy', 'SPICY'), ('soft spicy', 'SPICY'), ('cinnamon', 'SPICY'),
    ('woody', 'WOODY'), ('oud', 'WOODY'), ('earthy', 'WOODY'), ('patchouli', 'WOODY'),
    ('smoky', 'WOODY'), ('conifer', 'WOODY'),
    ('amber', 'AMBER'), ('balsamic', 'AMBER'), ('tobacco', 'AMBER'), ('incense', 'AMBER'),
    ('sweet', 'GOURMAND'), ('vanilla', 'GOURMAND'), ('caramel', 'GOURMAND'), ('honey', 'GOURMAND'),
    ('chocolate', 'GOURMAND'), ('cacao', 'GOURMAND'), ('coffee', 'GOURMAND'), ('nutty', 'GOURMAND'),
    ('almond', 'GOURMAND'), ('coconut', 'GOURMAND'), ('lactonic', 'GOURMAND'), ('rum', 'GOURMAND'),
    ('whiskey', 'GOURMAND'), ('beeswax', 'GOURMAND'),
    ('musky', 'MUSK'), ('animalic', 'MUSK'), ('soapy', 'MUSK'),
    ('powdery', 'POWDERY'), ('iris', 'POWDERY'), ('aldehydic', 'POWDERY'),
    ('leather', 'LEATHER');

-- ---------------------------------------------------------------- 기본 데이터: 향료 별칭
INSERT INTO ingredient_alias (alias_name, ingredient_id)
SELECT a.alias_name, i.ingredient_id
  FROM (VALUES
        ('Calabrian Bergamot', 'Bergamot'), ('Italian Bergamot', 'Bergamot'),
        ('Sicilian Lemon', 'Lemon'), ('Amalfi Lemon', 'Lemon'),
        ('Mandarin', 'Mandarin Orange'), ('Italian Mandarin', 'Mandarin Orange'),
        ('Pink Grapefruit', 'Grapefruit'),
        ('Blackcurrant', 'Black Currant'), ('Cassis', 'Black Currant'),
        ('Orris', 'Iris'), ('Orris Root', 'Iris'),
        ('Turkish Rose', 'Rose'), ('Bulgarian Rose', 'Rose'), ('Damask Rose', 'Rose'), ('May Rose', 'Rose'),
        ('Jasmine Sambac', 'Jasmine'), ('Sambac Jasmine', 'Jasmine'),
        ('Marine Notes', 'Sea Notes'), ('Sea Water', 'Sea Notes'),
        ('Cedar', 'Cedarwood'), ('Atlas Cedar', 'Cedarwood'), ('Virginia Cedar', 'Cedarwood'),
        ('Virginian Cedar', 'Cedarwood'),
        ('Australian Sandalwood', 'Sandalwood'), ('Indian Sandalwood', 'Sandalwood'),
        ('Haitian Vetiver', 'Vetiver'), ('Bourbon Vetiver', 'Vetiver'),
        ('Patchouli Leaf', 'Patchouli'), ('Indonesian Patchouli Leaf', 'Patchouli'),
        ('Madagascar Vanilla', 'Vanilla'), ('Bourbon Vanilla', 'Vanilla'), ('Vanilla Bean', 'Vanilla'),
        ('Tonka', 'Tonka Bean'),
        ('Musk', 'White Musk')
       ) AS a (alias_name, ingredient_name_en)
  JOIN ingredient i ON lower(i.ingredient_name_en) = lower(a.ingredient_name_en);

COMMIT;
