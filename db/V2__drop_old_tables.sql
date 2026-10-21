-- =====================================================================
-- 기존 대문자 테이블 삭제 (V1 적용 후, 새 테이블로 옮긴 것을 확인하고 실행)
-- 기존 데이터(테스트 3건)는 db/backup_20260923_old_tables.sql 에 백업되어 있음
--
-- 실행 방법: Supabase 대시보드 > SQL Editor 에 붙여넣고 Run
-- =====================================================================
BEGIN;
DROP TABLE IF EXISTS public."Perfume_Ingredient_Map";
DROP TABLE IF EXISTS public."Perfumes";
DROP TABLE IF EXISTS public."Ingredients";
COMMIT;
