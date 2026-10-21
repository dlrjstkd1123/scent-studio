package kr.co.seedssoft.platform.admin.dao;

import java.util.List;

import kr.co.seedssoft.platform.admin.dto.AdminSummary;
import kr.co.seedssoft.platform.admin.dto.PerfumeCsvRow;
import kr.co.seedssoft.platform.admin.dto.ScentFamilyList;
import kr.co.seedssoft.platform.admin.dto.UpsertResult;
import kr.co.seedssoft.platform.admin.vo.AdminImportLog;
import kr.co.seedssoft.platform.perfume.dto.IngredientAliasList;
import kr.co.seedssoft.platform.perfume.vo.AccordFamilyMap;
import kr.co.seedssoft.platform.perfume.vo.Ingredient;
import kr.co.seedssoft.platform.perfume.vo.PerfumeAccord;
import kr.co.seedssoft.platform.perfume.vo.PerfumeIngredient;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;
import kr.co.seedssoft.platform.perfume.vo.SearchKeywordTranslation;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : AdminDataDao.java
 * @Description : 관리자 데이터 관리 DAO (SQL: mapper/platform/admin/AdminData_SQL_postgres.xml)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Mapper("AdminDataDao")
public interface AdminDataDao {

  AdminSummary selectSummary();

  List<ScentFamilyList> selectFamilyList();

  List<String> selectFamilyCodeList();

  int updateFamily(ScentFamily scentFamily);

  UpsertResult upsertIngredient(Ingredient ingredient);

  UpsertResult findOrInsertIngredient(Ingredient ingredient);

  UpsertResult upsertPerfume(PerfumeCsvRow perfume);

  int deletePerfumeNoteList(Long perfumeId);

  int deletePerfumeAccordList(Long perfumeId);

  int insertPerfumeNote(PerfumeIngredient perfumeIngredient);

  int insertPerfumeAccord(PerfumeAccord perfumeAccord);

  List<String> selectIngredientNameList();

  UpsertResult upsertIngredientAlias(IngredientAliasList ingredientAlias);

  UpsertResult upsertAccordFamilyMap(AccordFamilyMap accordFamilyMap);

  UpsertResult upsertKeywordTranslation(SearchKeywordTranslation searchKeywordTranslation);

  int insertImportLog(AdminImportLog adminImportLog);

  List<AdminImportLog> selectImportLogList(int limit);
}
