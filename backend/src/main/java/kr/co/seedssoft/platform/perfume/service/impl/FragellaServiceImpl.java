package kr.co.seedssoft.platform.perfume.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.perfume.dto.Fragrance;
import kr.co.seedssoft.platform.perfume.dto.SimilarFragranceList;
import kr.co.seedssoft.platform.perfume.service.FragellaService;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName : FragellaServiceImpl.java
 * @Description : Fragella 향수 API 호출 서비스 구현 (검색 · 비슷한 향수)
 *                Fragella 응답 상태 처리
 *                - 404 (결과 없음)     → 빈 목록 (Controller 가 101)
 *                - 400 (파라미터 오류) → IllegalArgumentException (103)
 *                - 429 · 401 · 403 · 5xx · 연결 실패 → RestTemplate 예외를 그대로 던진다 (Controller 가 151 · 150)
 *                트랜잭션 대상에서 제외되어 있다 (EgovConfigAppTransaction).
 * @author : 이건상
 * @since : 2026. 10. 11.
 */
@Service("fragellaService")
public class FragellaServiceImpl extends EgovAbstractServiceImpl implements FragellaService {

  /** Fragella 검색어 최소 길이 */
  private static final int MIN_KEYWORD_LENGTH = 3;
  private static final int MAX_KEYWORD_LENGTH = 100;

  /** 한 번에 받는 개수 (Fragella 최대 10, 개수와 상관없이 호출 1회로 센다) */
  private static final int LIMIT = 10;

  @Resource(name = "fragellaRestTemplate")
  private RestTemplate fragellaRestTemplate;

  /**
   * @Program Name : selectFragranceList()
   * @Describe : 향수 탐색 > 향수 검색 (Fragella /fragrances, 결과 없으면 빈 목록)
   * @Param1 : keyword(String) 검색어 (영문 3자 이상)
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @Override
  public List<Fragrance> selectFragranceList(String keyword) throws Exception {
    String search = checkKeyword(keyword, "검색어");
    Fragrance[] list = call(
      () -> fragellaRestTemplate.getForObject(
        "/fragrances?search={search}&limit={limit}",
        Fragrance[].class,
        search,
        LIMIT
      )
    );
    return list == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(list));
  }

  /**
   * @Program Name : selectSimilarFragranceList()
   * @Describe : 향수 탐색 > 비슷한 향수 (Fragella /fragrances/similar, 결과 없으면 빈 목록)
   * @Param1 : name(String) 기준 향수 이름 (영문 3자 이상)
   * @Date : 2026. 10. 11.
   * @Writer : 이건상
   * @History : 2026. 10. 11. | 이건상 | 최초작성
   */
  @Override
  public SimilarFragranceList selectSimilarFragranceList(String name) throws Exception {
    String baseName = checkKeyword(name, "향수 이름");
    SimilarFragranceList similar = call(
      () -> fragellaRestTemplate.getForObject(
        "/fragrances/similar?name={name}&limit={limit}",
        SimilarFragranceList.class,
        baseName,
        LIMIT
      )
    );
    if (similar == null) {
      similar = new SimilarFragranceList();
    }
    if (similar.getSimilarFragrances() == null) {
      similar.setSimilarFragrances(new ArrayList<>());
    }
    return similar;
  }

  private static String checkKeyword(String value, String label) {
    String trimmed = value == null ? "" : value.trim();
    if (trimmed.length() < MIN_KEYWORD_LENGTH) {
      throw new IllegalArgumentException(label + "는 영문 " + MIN_KEYWORD_LENGTH + "자 이상 입력하세요.");
    }
    if (trimmed.length() > MAX_KEYWORD_LENGTH) {
      throw new IllegalArgumentException(label + "는 " + MAX_KEYWORD_LENGTH + "자까지 입력할 수 있습니다.");
    }
    return trimmed;
  }

  /** Fragella 호출. 404 는 null (결과 없음), 400 은 103. 나머지 오류는 그대로 Controller 로 */
  private static <T> T call(Supplier<T> request) {
    try {
      return request.get();
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    } catch (HttpClientErrorException.BadRequest e) {
      throw new IllegalArgumentException("검색 조건이 올바르지 않습니다.");
    }
  }
}
