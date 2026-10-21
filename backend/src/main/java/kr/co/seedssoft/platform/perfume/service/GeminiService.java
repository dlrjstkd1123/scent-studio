package kr.co.seedssoft.platform.perfume.service;

import kr.co.seedssoft.platform.perfume.dto.KeywordTranslation;

/**
 * @ClassName : GeminiService.java
 * @Description : Google Gemini API 호출 (DB 사용 안 함). 한글 향수 검색어 → 영문 변환.
 * @author : 이건상
 * @since : 2026. 10. 20.
 */
public interface GeminiService {

  /* 한글 향수 검색어 → 영문 검색어 (키가 없으면 IllegalStateException, 호출 실패는 RestClientException) */
  KeywordTranslation translateKeyword(String keyword) throws Exception;
}
