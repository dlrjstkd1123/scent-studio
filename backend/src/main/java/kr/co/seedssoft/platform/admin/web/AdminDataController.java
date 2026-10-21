package kr.co.seedssoft.platform.admin.web;

import java.util.List;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.admin.dto.AdminSummary;
import kr.co.seedssoft.platform.admin.dto.ImportResult;
import kr.co.seedssoft.platform.admin.dto.ScentFamilyList;
import kr.co.seedssoft.platform.admin.service.AdminDataService;
import kr.co.seedssoft.platform.admin.vo.AdminImportLog;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseBody;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseCode;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName : AdminDataController.java
 * @Description : 관리자 > 데이터 관리.
 *                /api/admin/** 는 AdminKeyInterceptor 가 X-Admin-Key 헤더를 검사한다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@RestController
public class AdminDataController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminDataController.class);

  @Resource(name = "adminDataService")
  private AdminDataService adminDataService;

  public static class API {
    public static final String BASE = "/api/admin";
    public static final String BASE_PATTERN = BASE + "/**";

    /* 관리자 키 */
    public static final String GET_CHECK = BASE + "/check"; // 관리자 키 확인

    /* 데이터 현황 · 향 계열 */
    public static final String GET_SUMMARY = BASE + "/summary"; // 데이터 현황
    public static final String GET_FAMILY_LIST = BASE + "/families"; // 향 계열 목록
    public static final String PUT_FAMILY = BASE + "/families/{familyCode}"; // 향 계열 한글명 · 색상 수정

    /* CSV 업로드 · 이력 */
    public static final String GET_IMPORT_LOG_LIST = BASE + "/import-logs"; // 최근 업로드 이력
    public static final String POST_IMPORT_INGREDIENTS = BASE + "/import/ingredients"; // 향료 CSV 업로드
    public static final String POST_IMPORT_PERFUMES = BASE + "/import/perfumes"; // 향수 CSV 업로드
    public static final String POST_IMPORT_ALIASES = BASE + "/import/aliases"; // 향료 별칭 CSV 업로드
    public static final String POST_IMPORT_ACCORDS = BASE + "/import/accords"; // 어코드 → 계열 매핑 CSV 업로드
    public static final String POST_IMPORT_KEYWORDS = BASE + "/import/keywords"; // 한글 검색어 → 영문 CSV 업로드
  }

  /**
   * @Program Name : check()
   * @Describe : 관리자 > 로그인 > 관리자 키 확인 (인터셉터를 통과하면 정상)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @GetMapping(API.GET_CHECK)
  public ResponseEntity<CommonResponseBody<Void>> check(
  ) {
    return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_100));
  }

  /**
   * @Program Name : selectSummary()
   * @Describe : 관리자 > 데이터 관리 > 데이터 현황 (테이블별 건수)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   *            2026. 10. 11. | 이건상 | 향료 별칭 · 어코드 매핑 건수 추가
   */
  @GetMapping(API.GET_SUMMARY)
  public ResponseEntity<CommonResponseBody<AdminSummary>> selectSummary(
  ) throws Exception {
    AdminSummary summary = adminDataService.selectSummary();
    return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_100, summary));
  }

  /**
   * @Program Name : selectFamilyList()
   * @Describe : 관리자 > 데이터 관리 > 향 계열 목록 (계열별 향료 수 포함)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @GetMapping(API.GET_FAMILY_LIST)
  public ResponseEntity<CommonResponseBody<List<ScentFamilyList>>> selectFamilyList(
  ) throws Exception {
    List<ScentFamilyList> familyList = adminDataService.selectFamilyList();
    CommonResponseCode responseCode = familyList.isEmpty()
      ? CommonResponseCode.COMMON_101
      : CommonResponseCode.COMMON_100;
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode, familyList));
  }

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

  /**
   * @Program Name : selectImportLogList()
   * @Describe : 관리자 > 데이터 관리 > 최근 업로드 이력
   * @Param1 : limit(int) 조회 건수 (기본 20, 최대 100)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @GetMapping(API.GET_IMPORT_LOG_LIST)
  public ResponseEntity<CommonResponseBody<List<AdminImportLog>>> selectImportLogList(
    @RequestParam(defaultValue = "20") int limit
  ) throws Exception {
    List<AdminImportLog> logList = adminDataService.selectImportLogList(limit);
    CommonResponseCode responseCode = logList.isEmpty()
      ? CommonResponseCode.COMMON_101
      : CommonResponseCode.COMMON_100;
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode, logList));
  }

  /**
   * @Program Name : importIngredients()
   * @Describe : 관리자 > 데이터 관리 > 향료 CSV 업로드 (같은 영문명이 있으면 수정)
   * @Param1 : file(MultipartFile) 향료 CSV
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @PostMapping(API.POST_IMPORT_INGREDIENTS)
  public ResponseEntity<CommonResponseBody<ImportResult>> importIngredients(
    @RequestParam("file") MultipartFile file
  ) throws Exception {
    return ResponseEntity.ok(runImport(ImportResult.TYPE_INGREDIENT_CSV, file));
  }

  /**
   * @Program Name : importPerfumes()
   * @Describe : 관리자 > 데이터 관리 > 향수 CSV 업로드 (향수 + 노트 + 어코드)
   * @Param1 : file(MultipartFile) 향수 CSV
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @PostMapping(API.POST_IMPORT_PERFUMES)
  public ResponseEntity<CommonResponseBody<ImportResult>> importPerfumes(
    @RequestParam("file") MultipartFile file
  ) throws Exception {
    return ResponseEntity.ok(runImport(ImportResult.TYPE_PERFUME_CSV, file));
  }

  /**
   * @Program Name : importAliases()
   * @Describe : 관리자 > 데이터 관리 > 향료 별칭 CSV 업로드 (같은 별칭이 있으면 향료 변경)
   * @Param1 : file(MultipartFile) 향료 별칭 CSV
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @PostMapping(API.POST_IMPORT_ALIASES)
  public ResponseEntity<CommonResponseBody<ImportResult>> importAliases(
    @RequestParam("file") MultipartFile file
  ) throws Exception {
    return ResponseEntity.ok(runImport(ImportResult.TYPE_ALIAS_CSV, file));
  }

  /**
   * @Program Name : importAccords()
   * @Describe : 관리자 > 데이터 관리 > 어코드 → 계열 매핑 CSV 업로드 (같은 어코드가 있으면 계열 변경)
   * @Param1 : file(MultipartFile) 어코드 매핑 CSV
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @PostMapping(API.POST_IMPORT_ACCORDS)
  public ResponseEntity<CommonResponseBody<ImportResult>> importAccords(
    @RequestParam("file") MultipartFile file
  ) throws Exception {
    return ResponseEntity.ok(runImport(ImportResult.TYPE_ACCORD_CSV, file));
  }

  /**
   * @Program Name : importKeywords()
   * @Describe : 관리자 > 데이터 관리 > 한글 검색어 → 영문 CSV 업로드 (같은 한글이 있으면 영문 변경, AI 값보다 우선)
   * @Param1 : file(MultipartFile) 한글 검색어 CSV
   * @Date : 2026. 10. 20.
   * @Writer : 이건상
   * @History : 2026. 10. 20. | 이건상 | 최초작성
   */
  @PostMapping(API.POST_IMPORT_KEYWORDS)
  public ResponseEntity<CommonResponseBody<ImportResult>> importKeywords(
    @RequestParam("file") MultipartFile file
  ) throws Exception {
    return ResponseEntity.ok(runImport(ImportResult.TYPE_KEYWORD_CSV, file));
  }

  /**
   * 업로드(한 트랜잭션) 실행 후 결과를 이력에 남긴다.
   * 이력 저장은 별도 트랜잭션이라 업로드가 롤백돼도 실패 이력은 남는다.
   */
  private CommonResponseBody<ImportResult> runImport(
    String importType,
    MultipartFile file
  ) throws Exception {
    String fileName = file.getOriginalFilename();
    if (file.isEmpty()) {
      return new CommonResponseBody<>(CommonResponseCode.COMMON_103, "빈 파일입니다.");
    }

    ImportResult result;
    CommonResponseCode responseCode = CommonResponseCode.COMMON_100;
    String message = responseCode.getMessage();
    try {
      result = importByType(importType, fileName, file.getBytes());
    } catch (IllegalArgumentException e) {
      // CSV 형식 오류 (필수 컬럼 누락 등)
      responseCode = CommonResponseCode.COMMON_103;
      message = e.getMessage();
      result = failedResult(importType, fileName, message);
    } catch (Exception e) {
      LOGGER.error("관리자 업로드 실패: {} {}", importType, fileName, e);
      responseCode = CommonResponseCode.COMMON_102;
      message = "저장 중 오류가 나서 이 파일의 변경은 모두 취소되었습니다: "
          + NestedExceptionUtils.getMostSpecificCause(e).getMessage();
      result = failedResult(importType, fileName, message);
    }

    adminDataService.insertImportLog(result);
    return new CommonResponseBody<>(responseCode, message, result);
  }

  private ImportResult importByType(
    String importType,
    String fileName,
    byte[] csv
  ) throws Exception {
    switch (importType) {
    case ImportResult.TYPE_INGREDIENT_CSV:
      return adminDataService.importIngredients(fileName, csv);
    case ImportResult.TYPE_PERFUME_CSV:
      return adminDataService.importPerfumes(fileName, csv);
    case ImportResult.TYPE_ALIAS_CSV:
      return adminDataService.importAliases(fileName, csv);
    case ImportResult.TYPE_ACCORD_CSV:
      return adminDataService.importAccords(fileName, csv);
    case ImportResult.TYPE_KEYWORD_CSV:
      return adminDataService.importKeywords(fileName, csv);
    default:
      throw new IllegalArgumentException("알 수 없는 업로드 종류입니다: " + importType);
    }
  }

  private static ImportResult failedResult(
    String importType,
    String fileName,
    String message
  ) {
    ImportResult result = new ImportResult(importType, fileName);
    result.setMessage(message);
    return result;
  }
}
