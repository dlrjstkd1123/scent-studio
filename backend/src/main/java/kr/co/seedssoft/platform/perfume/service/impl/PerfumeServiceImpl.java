package kr.co.seedssoft.platform.perfume.service.impl;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.cmmn.util.KeywordUtil;
import kr.co.seedssoft.platform.perfume.dao.PerfumeDao;
import kr.co.seedssoft.platform.perfume.dto.KeywordTranslation;
import kr.co.seedssoft.platform.perfume.dto.NoteDictionary;
import kr.co.seedssoft.platform.perfume.service.GeminiService;
import kr.co.seedssoft.platform.perfume.service.PerfumeService;
import kr.co.seedssoft.platform.perfume.vo.SearchKeywordTranslation;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @ClassName : PerfumeServiceImpl.java
 * @Description : 향수 탐색 서비스 구현 (노트 사전: 향 계열 · 향료 · 별칭 · 어코드→계열)
 *                한글 검색어 → 영문 검색어 (저장된 변환 우선, 없으면 Gemini)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Service("perfumeService")
public class PerfumeServiceImpl extends EgovAbstractServiceImpl implements PerfumeService {

  /** 한글 검색어 최소 · 최대 길이 (예: "샤넬" 2자) */
  private static final int MIN_HANGUL_LENGTH = 2;
  private static final int MAX_KEYWORD_LENGTH = 100;

  /** Fragella 검색어 최소 길이 (영문 변환 결과가 이보다 짧으면 쓰지 않는다) */
  private static final int MIN_ENGLISH_LENGTH = 3;
  private static final int MAX_ENGLISH_LENGTH = 200;

  @Resource(name = "PerfumeDao")
  private PerfumeDao perfumeDao;

  @Resource(name = "geminiService")
  private GeminiService geminiService;

  /**
   * @Program Name : selectNoteDictionary()
   * @Describe : 향수 탐색 > 노트 사전 (향 계열 · 향료 · 향료 별칭 · 어코드→계열)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   *            2026. 10. 11. | 이건상 | 향료 별칭 · 어코드→계열 추가
   */
  @Override
  public NoteDictionary selectNoteDictionary() throws Exception {
    return new NoteDictionary(
      perfumeDao.selectNoteFamilyList(),
      perfumeDao.selectNoteIngredientList(),
      perfumeDao.selectNoteAliasList(),
      perfumeDao.selectNoteAccordList()
    );
  }

  /**
   * @Program Name : selectSearchKeyword()
   * @Describe : 향수 탐색 > 향수 검색 > Fragella 로 보낼 검색어
   *             한글이 없으면 그대로, 한글이면 저장된 변환 → 없으면 Gemini 로 변환해서 저장 (AI 는 처음 한 번만 호출)
   * @Param1 : keyword(String) 사용자가 입력한 검색어
   * @Date : 2026. 10. 20.
   * @Writer : 이건상
   * @History : 2026. 10. 20. | 이건상 | 최초작성
   */
  @Override
  public String selectSearchKeyword(String keyword) throws Exception {
    if (!KeywordUtil.hasHangul(keyword)) {
      return keyword == null ? "" : keyword.trim();
    }

    String keywordKo = KeywordUtil.normalize(keyword);
    if (keywordKo.length() < MIN_HANGUL_LENGTH) {
      throw new IllegalArgumentException("한글 검색어는 " + MIN_HANGUL_LENGTH + "자 이상 입력하세요.");
    }
    if (keywordKo.length() > MAX_KEYWORD_LENGTH) {
      throw new IllegalArgumentException("검색어는 " + MAX_KEYWORD_LENGTH + "자까지 입력할 수 있습니다.");
    }

    // 1) 저장된 변환 (관리자가 넣은 값 포함)
    SearchKeywordTranslation saved = perfumeDao.selectKeywordTranslation(keywordKo);
    if (saved != null) {
      perfumeDao.updateKeywordTranslationHit(keywordKo);
      return saved.getKeywordEn();
    }

    // 2) Gemini 로 변환 (호출 실패는 예외 그대로 → Controller 가 152)
    KeywordTranslation result = geminiService.translateKeyword(keywordKo);
    String english = result.getEnglish() == null ? "" : result.getEnglish().trim();
    boolean usable = Boolean.TRUE.equals(result.getIsFragrance())
        && english.length() >= MIN_ENGLISH_LENGTH
        && english.length() <= MAX_ENGLISH_LENGTH
        && !KeywordUtil.hasHangul(english);
    if (!usable) {
      // 향수 검색어가 아니거나 변환 결과를 쓸 수 없음 → 저장하지 않음
      return null;
    }

    SearchKeywordTranslation translation = new SearchKeywordTranslation();
    translation.setKeywordKo(keywordKo);
    translation.setKeywordEn(english);
    translation.setSourceType("AI");
    perfumeDao.insertKeywordTranslation(translation);
    return english;
  }
}
