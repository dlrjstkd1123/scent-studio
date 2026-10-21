package kr.co.seedssoft.platform.admin.service;

import java.util.List;

import kr.co.seedssoft.platform.admin.dto.AdminSummary;
import kr.co.seedssoft.platform.admin.dto.ImportResult;
import kr.co.seedssoft.platform.admin.dto.ScentFamilyList;
import kr.co.seedssoft.platform.admin.vo.AdminImportLog;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;

/**
 * @ClassName : AdminDataService.java
 * @Description : 관리자 데이터 관리 서비스.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public interface AdminDataService {

  /* 테이블별 데이터 건수 */
  AdminSummary selectSummary() throws Exception;

  /* 향 계열 목록 (향료 수 포함) */
  List<ScentFamilyList> selectFamilyList() throws Exception;

  /* 향 계열 한글명 · 색상 수정 */
  boolean updateFamily(ScentFamily scentFamily) throws Exception;

  /* 향료 CSV 업로드 */
  ImportResult importIngredients(String fileName, byte[] csv) throws Exception;

  /* 향수 CSV 업로드 */
  ImportResult importPerfumes(String fileName, byte[] csv) throws Exception;

  /* 향료 별칭 CSV 업로드 */
  ImportResult importAliases(String fileName, byte[] csv) throws Exception;

  /* 어코드 → 계열 매핑 CSV 업로드 */
  ImportResult importAccords(String fileName, byte[] csv) throws Exception;

  /* 한글 검색어 → 영문 CSV 업로드 (관리자 값이 AI 값보다 우선) */
  ImportResult importKeywords(String fileName, byte[] csv) throws Exception;

  /* 업로드 이력 저장 */
  void insertImportLog(ImportResult result) throws Exception;

  /* 최근 업로드 이력 */
  List<AdminImportLog> selectImportLogList(int limit) throws Exception;
}
