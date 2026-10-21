package kr.co.seedssoft.platform.perfume.service;

import kr.co.seedssoft.platform.perfume.dto.NoteDictionary;

/**
 * @ClassName : PerfumeService.java
 * @Description : 향수 탐색 서비스.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public interface PerfumeService {

  /* 노트 사전 */
  NoteDictionary selectNoteDictionary() throws Exception;

  /* Fragella 로 보낼 검색어 (한글이면 저장된 변환 → 없으면 Gemini 로 영문 변환, 향수 검색어가 아니면 null) */
  String selectSearchKeyword(String keyword) throws Exception;
}
