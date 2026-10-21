package kr.co.seedssoft.platform.perfume.web;

import java.util.List;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.cmmn.response.CommonResponseBody;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseCode;
import kr.co.seedssoft.platform.cmmn.util.KeywordUtil;
import kr.co.seedssoft.platform.perfume.dto.Fragrance;
import kr.co.seedssoft.platform.perfume.dto.FragranceSearchList;
import kr.co.seedssoft.platform.perfume.dto.NoteDictionary;
import kr.co.seedssoft.platform.perfume.dto.SimilarFragranceList;
import kr.co.seedssoft.platform.perfume.service.FragellaService;
import kr.co.seedssoft.platform.perfume.service.PerfumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * @ClassName : PerfumeController.java
 * @Description : 향수 탐색 (공개 API).
 *                향수 검색 · 비슷한 향수는 Fragella API 를 대신 호출한다 (호출 1번 = Fragella 월 사용량 1회).
 *                한글 검색어는 영문으로 바꿔서 검색한다 (저장된 변환 → 없으면 Gemini).
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@RestController
public class PerfumeController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PerfumeController.class);

  @Resource(name = "perfumeService")
  private PerfumeService perfumeService;

  @Resource(name = "fragellaService")
  private FragellaService fragellaService;

  public static class API {
    public static final String BASE = "/api/perfume";
    public static final String BASE_PATTERN = BASE + "/**";

    /* 노트 사전 */
    public static final String GET_NOTE_DICTIONARY = BASE + "/note-dictionary"; // 노트 사전

    /* 향수 검색 · 추천 (Fragella, 호출 1번 = 월 사용량 1회) */
    public static final String GET_FRAGRANCE_LIST = BASE + "/fragrances"; // 향수 검색 (한글 · 영문)
    public static final String GET_SIMILAR_FRAGRANCE_LIST = BASE + "/fragrances/similar"; // 검색한 향수와 비슷한 향수
  }

  /**
   * @Program Name : selectNoteDictionary()
   * @Describe : 향수 탐색 > 노트 사전 (향 계열 색상 · 향료→계열 · 향료 별칭 · 어코드→계열)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   *            2026. 10. 11. | 이건상 | 향료 별칭 · 어코드→계열 추가
   */
  @GetMapping(API.GET_NOTE_DICTIONARY)
  public ResponseEntity<CommonResponseBody<NoteDictionary>> selectNoteDictionary(
  ) throws Exception {
    NoteDictionary noteDictionary = perfumeService.selectNoteDictionary();
    return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_100, noteDictionary));
  }

  /**
   * @Program Name : selectFragranceList()
   * @Describe : 향수 탐색 > 향수 검색 (한글이면 영문 변환 후 Fragella 호출)
   *             101 결과 없음 · 150 Fragella 실패 · 151 Fragella 월 사용량 초과 · 152 한글 → 영문 변환 실패
   * @Param1 : keyword(String) 검색어 (향수명 · 브랜드, 영문 3자 · 한글 2자 이상)
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   *            2026. 10. 20. | 이건상 | 한글 검색 (Gemini 변환), 응답을 FragranceSearchList 로 변경
   */
  @GetMapping(API.GET_FRAGRANCE_LIST)
  public ResponseEntity<CommonResponseBody<FragranceSearchList>> selectFragranceList(
    @RequestParam String keyword
  ) throws Exception {
    // 1) 한글이면 영문으로 (저장된 변환 → 없으면 Gemini)
    String searchKeyword;
    try {
      searchKeyword = perfumeService.selectSearchKeyword(keyword);
    } catch (RestClientException | IllegalStateException e) {
      // Gemini 호출 실패 · 사용량 초과 · 키 없음
      LOGGER.error("한글 검색어 변환 실패: {}", keyword, e);
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_152));
    }
    FragranceSearchList searchList = new FragranceSearchList();
    searchList.setKeyword(keyword);
    searchList.setSearchKeyword(searchKeyword);
    searchList.setTranslated(KeywordUtil.hasHangul(keyword));
    if (searchKeyword == null) {
      // 향수 검색어가 아님 (Fragella 호출 안 함)
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_101, searchList));
    }

    // 2) Fragella 검색
    List<Fragrance> fragranceList;
    try {
      fragranceList = fragellaService.selectFragranceList(searchKeyword);
    } catch (HttpClientErrorException.TooManyRequests e) {
      // Fragella 월 사용량 초과
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_151));
    } catch (RestClientException e) {
      // 키 오류(401 · 403), Fragella 서버 오류(5xx), 연결 실패
      LOGGER.error("Fragella 검색 실패", e);
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_150));
    }
    searchList.setFragrances(fragranceList);
    CommonResponseCode responseCode = fragranceList.isEmpty()
        ? CommonResponseCode.COMMON_101
        : CommonResponseCode.COMMON_100;
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode, searchList));
  }

  /**
   * @Program Name : selectSimilarFragranceList()
   * @Describe : 향수 탐색 > 검색한 향수와 비슷한 향수 (Fragella 호출, 150 · 151 동일)
   * @Param1 : name(String) 기준 향수 이름 (영문 3자 이상)
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @GetMapping(API.GET_SIMILAR_FRAGRANCE_LIST)
  public ResponseEntity<CommonResponseBody<SimilarFragranceList>> selectSimilarFragranceList(
    @RequestParam String name
  ) throws Exception {
    SimilarFragranceList similarList;
    try {
      similarList = fragellaService.selectSimilarFragranceList(name);
    } catch (HttpClientErrorException.TooManyRequests e) {
      // Fragella 월 사용량 초과
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_151));
    } catch (RestClientException e) {
      // 키 오류(401 · 403), Fragella 서버 오류(5xx), 연결 실패
      LOGGER.error("Fragella 비슷한 향수 조회 실패", e);
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_150));
    }
    CommonResponseCode responseCode = similarList.getSimilarFragrances().isEmpty()
        ? CommonResponseCode.COMMON_101
        : CommonResponseCode.COMMON_100;
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode, similarList));
  }
}
