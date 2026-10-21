package kr.co.seedssoft.platform.admin.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.admin.dao.AdminDataDao;
import kr.co.seedssoft.platform.admin.dto.AdminSummary;
import kr.co.seedssoft.platform.admin.dto.ImportResult;
import kr.co.seedssoft.platform.admin.dto.PerfumeCsvRow;
import kr.co.seedssoft.platform.admin.dto.ScentFamilyList;
import kr.co.seedssoft.platform.admin.dto.UpsertResult;
import kr.co.seedssoft.platform.admin.service.AdminDataService;
import kr.co.seedssoft.platform.admin.vo.AdminImportLog;
import kr.co.seedssoft.platform.cmmn.util.CsvUtil;
import kr.co.seedssoft.platform.cmmn.util.KeywordUtil;
import kr.co.seedssoft.platform.perfume.dto.IngredientAliasList;
import kr.co.seedssoft.platform.perfume.vo.AccordFamilyMap;
import kr.co.seedssoft.platform.perfume.vo.Ingredient;
import kr.co.seedssoft.platform.perfume.vo.PerfumeAccord;
import kr.co.seedssoft.platform.perfume.vo.PerfumeIngredient;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;
import kr.co.seedssoft.platform.perfume.vo.SearchKeywordTranslation;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @ClassName : AdminDataServiceImpl.java
 * @Description : 관리자 > 데이터 관리 서비스 구현 (CSV 업로드 · 계열 수정 · 이력)
 *                업로드는 한 파일이 한 트랜잭션이다.
 *                - 형식이 잘못된 행은 사유를 남기고 건너뛴다 (나머지 행은 저장).
 *                - DB 오류가 나면 파일 전체가 롤백된다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Service("adminDataService")
public class AdminDataServiceImpl extends EgovAbstractServiceImpl implements AdminDataService {

  private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
  private static final Pattern NOTE_SPLIT = Pattern.compile("\\s*\\|\\s*");

  /** 이력 메시지에 남길 최대 실패 사유 수 */
  private static final int MAX_LOG_ERRORS = 20;

  @Resource(name = "AdminDataDao")
  private AdminDataDao adminDataDao;

  /**
   * @Program Name : selectSummary()
   * @Describe : 관리자 > 데이터 관리 > 테이블별 데이터 건수
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   *            2026. 10. 11. | 이건상 | 향료 별칭 · 어코드 매핑 건수 추가
   */
  @Override
  public AdminSummary selectSummary() throws Exception {
    return adminDataDao.selectSummary();
  }

  /**
   * @Program Name : selectFamilyList()
   * @Describe : 관리자 > 데이터 관리 > 향 계열 목록 (향료 수 포함)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public List<ScentFamilyList> selectFamilyList() throws Exception {
    return adminDataDao.selectFamilyList();
  }

  /**
   * @Program Name : updateFamily()
   * @Describe : 관리자 > 데이터 관리 > 향 계열 한글명 · 색상 수정 (대상 없으면 false, 색상 형식 오류는 103)
   * @Param1 : scentFamily(ScentFamily) 수정 정보
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public boolean updateFamily(ScentFamily scentFamily) throws Exception {
    String hexColorCode = scentFamily.getHexColorCode();
    if (hexColorCode == null || !HEX_COLOR.matcher(hexColorCode).matches()) {
      throw new IllegalArgumentException("색상 코드는 #RRGGBB 형식이어야 합니다.");
    }
    scentFamily.setHexColorCode(hexColorCode.toUpperCase(Locale.ROOT));
    return adminDataDao.updateFamily(scentFamily) > 0;
  }

  // ------------------------------------------------------------------ 향료 CSV

  /**
   * @Program Name : importIngredients()
   * @Describe : 관리자 > 데이터 관리 > 향료 CSV 업로드 (있으면 수정, 없으면 추가)
   * @Param1 : fileName(String) 파일명
   * @Param2 : csv(byte[]) CSV 내용
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public ImportResult importIngredients(String fileName, byte[] csv) throws Exception {
    ImportResult result = new ImportResult(ImportResult.TYPE_INGREDIENT_CSV, fileName);
    List<Map<String, String>> rows = CsvUtil.read(csv, "ingredient_name_en");
    Set<String> familyCodes = new HashSet<>(adminDataDao.selectFamilyCodeList());
    result.setTotalCount(rows.size());

    for (Map<String, String> row : rows) {
      String rowNo = row.get("_row");
      Ingredient ingredient = new Ingredient();
      ingredient.setIngredientNameEn(row.get("ingredient_name_en"));
      ingredient.setIngredientNameKo(row.get("ingredient_name_ko"));

      if (ingredient.getIngredientNameEn() == null) {
        result.addError(rowNo, "ingredient_name_en(영문명)이 비어 있습니다.");
        continue;
      }
      if (ingredient.getIngredientNameEn().length() > 100) {
        result.addError(rowNo, "영문명이 100자를 넘습니다.");
        continue;
      }

      String familyCode = row.get("family_code");
      if (familyCode != null) {
        familyCode = familyCode.toUpperCase(Locale.ROOT);
        if (!familyCodes.contains(familyCode)) {
          result.addError(rowNo, "없는 계열 코드입니다: " + familyCode);
          continue;
        }
        ingredient.setFamilyCode(familyCode);
      }

      String noteTypeText = row.get("default_note_type");
      if (noteTypeText != null) {
        String noteType = normalizeNoteType(noteTypeText);
        if (noteType == null) {
          result.addError(
            rowNo,
            "default_note_type 은 TOP/MIDDLE/BASE 중 하나여야 합니다: " + noteTypeText
          );
          continue;
        }
        ingredient.setDefaultNoteType(noteType);
      }

      countUpsert(result, adminDataDao.upsertIngredient(ingredient));
    }
    return result;
  }

  // ------------------------------------------------------------------ 향수 CSV

  /**
   * @Program Name : importPerfumes()
   * @Describe : 관리자 > 데이터 관리 > 향수 CSV 업로드 (향수 + 노트 + 어코드, 처음 보는 향료는 자동 추가)
   * @Param1 : fileName(String) 파일명
   * @Param2 : csv(byte[]) CSV 내용
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public ImportResult importPerfumes(String fileName, byte[] csv) throws Exception {
    ImportResult result = new ImportResult(ImportResult.TYPE_PERFUME_CSV, fileName);
    List<Map<String, String>> rows = CsvUtil.read(csv, "brand_name", "perfume_name");
    result.setTotalCount(rows.size());

    for (Map<String, String> row : rows) {
      String rowNo = row.get("_row");
      PerfumeCsvRow perfume;
      try {
        perfume = toPerfume(row);
      } catch (IllegalArgumentException e) {
        result.addError(rowNo, e.getMessage());
        continue;
      }

      UpsertResult saved = adminDataDao.upsertPerfume(perfume);
      countUpsert(result, saved);
      Long perfumeId = saved.getId();

      // 노트/어코드는 CSV 내용으로 통째로 교체
      adminDataDao.deletePerfumeNoteList(perfumeId);
      adminDataDao.deletePerfumeAccordList(perfumeId);

      saveNotes(result, perfumeId, "TOP", perfume.getTopNotes());
      saveNotes(result, perfumeId, "MIDDLE", perfume.getMiddleNotes());
      saveNotes(result, perfumeId, "BASE", perfume.getBaseNotes());

      for (Map.Entry<String, Integer> accord : perfume.getAccords().entrySet()) {
        adminDataDao.insertPerfumeAccord(
          new PerfumeAccord(perfumeId, accord.getKey(), accord.getValue())
        );
      }
    }
    return result;
  }

  private void saveNotes(
    ImportResult result,
    Long perfumeId,
    String noteType,
    List<String> notes
  ) {
    int sortOrder = 1;
    for (String note : notes) {
      Ingredient ingredient = new Ingredient();
      ingredient.setIngredientNameEn(note);
      ingredient.setDefaultNoteType(noteType);

      UpsertResult saved = adminDataDao.findOrInsertIngredient(ingredient);
      if (Boolean.TRUE.equals(saved.getInserted())) {
        result.setNewIngredientCount(result.getNewIngredientCount() + 1);
      }
      adminDataDao.insertPerfumeNote(
        new PerfumeIngredient(perfumeId, saved.getId(), noteType, sortOrder++)
      );
    }
  }

  /** CSV 한 줄 → 향수. 형식이 잘못되면 IllegalArgumentException(사유) */
  private PerfumeCsvRow toPerfume(Map<String, String> row) {
    PerfumeCsvRow perfume = new PerfumeCsvRow();
    perfume.setBrandName(required(row, "brand_name", 100));
    perfume.setPerfumeName(required(row, "perfume_name", 200));

    String concentration = row.get("concentration");
    if (concentration != null && concentration.length() > 20) {
      throw new IllegalArgumentException("concentration 이 20자를 넘습니다.");
    }
    perfume.setConcentration(concentration);

    String year = row.get("release_year");
    if (year != null) {
      try {
        int releaseYear = Integer.parseInt(year);
        if (releaseYear < 1800 || releaseYear > 2100) {
          throw new NumberFormatException();
        }
        perfume.setReleaseYear(releaseYear);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("release_year 가 올바른 연도가 아닙니다: " + year);
      }
    }

    perfume.setTopNotes(splitNotes(row.get("top_notes")));
    perfume.setMiddleNotes(splitNotes(row.get("middle_notes")));
    perfume.setBaseNotes(splitNotes(row.get("base_notes")));
    boolean noNotes = perfume.getTopNotes().isEmpty()
      && perfume.getMiddleNotes().isEmpty()
      && perfume.getBaseNotes().isEmpty();
    if (noNotes) {
      throw new IllegalArgumentException("노트(top_notes/middle_notes/base_notes)가 하나도 없습니다.");
    }

    String accords = row.get("accords");
    if (accords != null) {
      for (String item : NOTE_SPLIT.split(accords)) {
        if (item.isEmpty()) {
          continue;
        }
        String[] parts = item.split(":", 2);
        String name = parts[0].trim().toLowerCase(Locale.ROOT);
        Integer strength = null;
        if (parts.length == 2 && !parts[1].trim().isEmpty()) {
          try {
            strength = Integer.valueOf(parts[1].trim());
          } catch (NumberFormatException e) {
            strength = -1;
          }
          if (strength < 0 || strength > 100) {
            throw new IllegalArgumentException("어코드 강도는 0~100 이어야 합니다: " + item);
          }
        }
        if (name.isEmpty() || name.length() > 50) {
          throw new IllegalArgumentException("어코드 이름이 올바르지 않습니다: " + item);
        }
        perfume.getAccords().put(name, strength);
      }
    }
    return perfume;
  }

  private static List<String> splitNotes(String text) {
    Set<String> notes = new LinkedHashSet<>();
    if (text != null) {
      for (String note : NOTE_SPLIT.split(text)) {
        if (note.isEmpty()) {
          continue;
        }
        if (note.length() > 100) {
          throw new IllegalArgumentException("노트 이름이 100자를 넘습니다: " + note);
        }
        notes.add(note);
      }
    }
    return new ArrayList<>(notes);
  }

  private static String required(
    Map<String, String> row,
    String column,
    int maxLength
  ) {
    String value = row.get(column);
    if (value == null) {
      throw new IllegalArgumentException(column + " 이(가) 비어 있습니다.");
    }
    if (value.length() > maxLength) {
      throw new IllegalArgumentException(column + " 이(가) " + maxLength + "자를 넘습니다.");
    }
    return value;
  }

  /** TOP/MIDDLE/BASE (대소문자 무관), 탑/미들/베이스 허용 */
  private static String normalizeNoteType(String text) {
    String noteType = text.trim().toUpperCase(Locale.ROOT);
    switch (noteType) {
    case "TOP":
    case "탑":
      return "TOP";
    case "MIDDLE":
    case "HEART":
    case "미들":
      return "MIDDLE";
    case "BASE":
    case "베이스":
      return "BASE";
    default:
      return null;
    }
  }

  private static void countUpsert(ImportResult result, UpsertResult saved) {
    if (Boolean.TRUE.equals(saved.getInserted())) {
      result.setInsertedCount(result.getInsertedCount() + 1);
    } else {
      result.setUpdatedCount(result.getUpdatedCount() + 1);
    }
  }

  // ------------------------------------------------------------------ 향료 별칭 CSV

  /**
   * @Program Name : importAliases()
   * @Describe : 관리자 > 데이터 관리 > 향료 별칭 CSV 업로드 (있으면 가리키는 향료 변경)
   * @Param1 : fileName(String) 파일명
   * @Param2 : csv(byte[]) CSV 내용
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @Override
  public ImportResult importAliases(String fileName, byte[] csv) throws Exception {
    ImportResult result = new ImportResult(ImportResult.TYPE_ALIAS_CSV, fileName);
    List<Map<String, String>> rows = CsvUtil.read(csv, "alias_name", "ingredient_name_en");
    Set<String> ingredientNames = new HashSet<>(adminDataDao.selectIngredientNameList());
    result.setTotalCount(rows.size());

    for (Map<String, String> row : rows) {
      String rowNo = row.get("_row");
      IngredientAliasList alias = new IngredientAliasList(
        row.get("alias_name"),
        row.get("ingredient_name_en")
      );

      if (alias.getAliasName() == null || alias.getIngredientNameEn() == null) {
        result.addError(rowNo, "alias_name, ingredient_name_en 은 비울 수 없습니다.");
        continue;
      }
      if (alias.getAliasName().length() > 100) {
        result.addError(rowNo, "별칭이 100자를 넘습니다.");
        continue;
      }
      // 별칭이 다른 향료의 정식 이름이면 같은 이름이 두 향료를 가리키게 된다
      if (ingredientNames.contains(alias.getAliasName().toLowerCase(Locale.ROOT))) {
        result.addError(rowNo, "이미 향료 영문명으로 등록된 이름입니다: " + alias.getAliasName());
        continue;
      }

      UpsertResult saved = adminDataDao.upsertIngredientAlias(alias);
      if (saved == null) {
        result.addError(rowNo, "없는 향료입니다 (향료 CSV 로 먼저 추가하세요): " + alias.getIngredientNameEn());
        continue;
      }
      countUpsert(result, saved);
    }
    return result;
  }

  // ------------------------------------------------------------------ 어코드 매핑 CSV

  /**
   * @Program Name : importAccords()
   * @Describe : 관리자 > 데이터 관리 > 어코드 → 계열 매핑 CSV 업로드 (있으면 계열 변경)
   * @Param1 : fileName(String) 파일명
   * @Param2 : csv(byte[]) CSV 내용
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @Override
  public ImportResult importAccords(String fileName, byte[] csv) throws Exception {
    ImportResult result = new ImportResult(ImportResult.TYPE_ACCORD_CSV, fileName);
    List<Map<String, String>> rows = CsvUtil.read(csv, "accord_name", "family_code");
    Set<String> familyCodes = new HashSet<>(adminDataDao.selectFamilyCodeList());
    result.setTotalCount(rows.size());

    for (Map<String, String> row : rows) {
      String rowNo = row.get("_row");
      String accordName = row.get("accord_name");
      String familyCode = row.get("family_code");

      if (accordName == null || familyCode == null) {
        result.addError(rowNo, "accord_name, family_code 는 비울 수 없습니다.");
        continue;
      }
      accordName = accordName.toLowerCase(Locale.ROOT);
      familyCode = familyCode.toUpperCase(Locale.ROOT);
      if (accordName.length() > 50) {
        result.addError(rowNo, "어코드 이름이 50자를 넘습니다.");
        continue;
      }
      if (!familyCodes.contains(familyCode)) {
        result.addError(rowNo, "없는 계열 코드입니다: " + familyCode);
        continue;
      }

      AccordFamilyMap accordFamilyMap = new AccordFamilyMap();
      accordFamilyMap.setAccordName(accordName);
      accordFamilyMap.setFamilyCode(familyCode);
      countUpsert(result, adminDataDao.upsertAccordFamilyMap(accordFamilyMap));
    }
    return result;
  }

  // ------------------------------------------------------------------ 한글 검색어 CSV

  /**
   * @Program Name : importKeywords()
   * @Describe : 관리자 > 데이터 관리 > 한글 검색어 → 영문 CSV 업로드 (있으면 영문 변경, AI 값보다 우선)
   * @Param1 : fileName(String) 파일명
   * @Param2 : csv(byte[]) CSV 내용
   * @Date : 2026. 10. 20.
   * @Writer : 이건상
   * @History : 2026. 10. 20. | 이건상 | 최초작성
   */
  @Override
  public ImportResult importKeywords(String fileName, byte[] csv) throws Exception {
    ImportResult result = new ImportResult(ImportResult.TYPE_KEYWORD_CSV, fileName);
    List<Map<String, String>> rows = CsvUtil.read(csv, "keyword_ko", "keyword_en");
    result.setTotalCount(rows.size());

    for (Map<String, String> row : rows) {
      String rowNo = row.get("_row");
      String keywordKo = KeywordUtil.normalize(row.get("keyword_ko"));
      String keywordEn = row.get("keyword_en") == null ? "" : row.get("keyword_en").trim();

      if (keywordKo.isEmpty() || keywordEn.isEmpty()) {
        result.addError(rowNo, "keyword_ko, keyword_en 은 비울 수 없습니다.");
        continue;
      }
      if (!KeywordUtil.hasHangul(keywordKo)) {
        result.addError(rowNo, "keyword_ko 에 한글이 없습니다: " + keywordKo);
        continue;
      }
      if (KeywordUtil.hasHangul(keywordEn)) {
        result.addError(rowNo, "keyword_en 에 한글이 있습니다: " + keywordEn);
        continue;
      }
      if (keywordKo.length() > 100 || keywordEn.length() > 200) {
        result.addError(rowNo, "keyword_ko 는 100자, keyword_en 은 200자까지입니다.");
        continue;
      }
      if (keywordEn.length() < 3) {
        result.addError(rowNo, "keyword_en 은 3자 이상이어야 합니다 (Fragella 검색 최소 길이).");
        continue;
      }

      SearchKeywordTranslation translation = new SearchKeywordTranslation();
      translation.setKeywordKo(keywordKo);
      translation.setKeywordEn(keywordEn);
      countUpsert(result, adminDataDao.upsertKeywordTranslation(translation));
    }
    return result;
  }

  // ------------------------------------------------------------------ 이력

  /**
   * @Program Name : insertImportLog()
   * @Describe : 관리자 > 데이터 관리 > 업로드 이력 저장
   * @Param1 : result(ImportResult) 업로드 결과
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public void insertImportLog(ImportResult result) throws Exception {
    String message = result.getMessage();
    if (message == null && !result.getErrors().isEmpty()) {
      List<String> errors = result.getErrors();
      message = String.join("\n", errors.subList(0, Math.min(MAX_LOG_ERRORS, errors.size())));
    }

    AdminImportLog adminImportLog = new AdminImportLog();
    adminImportLog.setImportType(result.getImportType());
    adminImportLog.setFileName(result.getFileName());
    adminImportLog.setTotalCount(result.getTotalCount());
    adminImportLog.setInsertedCount(result.getInsertedCount());
    adminImportLog.setUpdatedCount(result.getUpdatedCount());
    adminImportLog.setFailedCount(result.getFailedCount());
    adminImportLog.setMessage(message);
    adminDataDao.insertImportLog(adminImportLog);
  }

  /**
   * @Program Name : selectImportLogList()
   * @Describe : 관리자 > 데이터 관리 > 최근 업로드 이력
   * @Param1 : limit(int) 조회 건수 (1~100)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public List<AdminImportLog> selectImportLogList(int limit) throws Exception {
    return adminDataDao.selectImportLogList(Math.max(1, Math.min(limit, 100)));
  }
}
