package kr.co.seedssoft.platform.perfume.service;

import java.util.List;

import kr.co.seedssoft.platform.perfume.dto.Fragrance;
import kr.co.seedssoft.platform.perfume.dto.SimilarFragranceList;

/**
 * @ClassName : FragellaService.java
 * @Description : Fragella 향수 API 호출 (DB 사용 안 함).
 *                호출 1번 = Fragella 월 사용량 1회.
 * @author : 이건상
 * @since : 2026. 10. 11.
 */
public interface FragellaService {

  /* 향수 검색 (Fragella) */
  List<Fragrance> selectFragranceList(String keyword) throws Exception;

  /* 비슷한 향수 (Fragella) */
  SimilarFragranceList selectSimilarFragranceList(String name) throws Exception;
}
