-- 2026-09-23 기존 테이블 백업 (새 스키마 적용 전)

INSERT INTO public."Perfumes" (perfume_id, brand_name, perfume_name) VALUES (1, '샤넬', '탐다오');
INSERT INTO public."Ingredients" (ingredient_id, ingredient_name_en, scent_family, hex_color_code, note_type) VALUES (1, 'Bergamot', 'Citrus', '#FFEB3B', 'Top');
INSERT INTO public."Perfume_Ingredient_Map" (map_id, perfume_id, ingredient_id) VALUES (1, 1, 1);
