package kr.co.seedssoft.platform.perfume.dao;

import java.util.List;

import kr.co.seedssoft.platform.perfume.dto.IngredientAliasList;
import kr.co.seedssoft.platform.perfume.vo.AccordFamilyMap;
import kr.co.seedssoft.platform.perfume.vo.Ingredient;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;
import kr.co.seedssoft.platform.perfume.vo.SearchKeywordTranslation;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : PerfumeDao.java
 * @Description : 향수 탐색 DAO (SQL: mapper/platform/perfume/Perfume_SQL_postgres.xml)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Mapper("PerfumeDao")
public interface PerfumeDao {

  List<ScentFamily> selectNoteFamilyList();

  List<Ingredient> selectNoteIngredientList();

  List<IngredientAliasList> selectNoteAliasList();

  List<AccordFamilyMap> selectNoteAccordList();

  SearchKeywordTranslation selectKeywordTranslation(String keywordKo);

  int insertKeywordTranslation(SearchKeywordTranslation searchKeywordTranslation);

  int updateKeywordTranslationHit(String keywordKo);
}
