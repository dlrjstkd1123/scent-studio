package kr.co.seedssoft.platform.perfume.service.impl;

import javax.annotation.Resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.co.seedssoft.platform.perfume.dto.KeywordTranslation;
import kr.co.seedssoft.platform.perfume.service.GeminiService;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName : GeminiServiceImpl.java
 * @Description : Google Gemini API 호출 서비스 구현 (한글 향수 검색어 → 영문)
 *                POST /models/{model}:generateContent, 응답은 JSON 스키마로 고정 ({english, isFragrance})
 *                https://ai.google.dev/api/generate-content
 *                트랜잭션 대상에서 제외되어 있다 (EgovConfigAppTransaction).
 * @author : 이건상
 * @since : 2026. 10. 20.
 */
@Service("geminiService")
public class GeminiServiceImpl extends EgovAbstractServiceImpl implements GeminiService {

  /** Gemini 에게 주는 지시문 (예시로 한글 표기 → 공식 영문 표기를 알려준다) */
  private static final String INSTRUCTION = String.join("\n",
    "You convert a Korean perfume search query into the English search text used by"
      + " international fragrance databases such as Fragrantica.",
    "Korean users write brand and fragrance names in Hangul transliteration. Examples:",
    "- 디올 소바쥬 -> Dior Sauvage",
    "- 소바쥬 -> Sauvage",
    "- 블랙 오퓸 -> Black Opium",
    "- 입생로랑 블랙 오퓸 -> Yves Saint Laurent Black Opium",
    "- 르라보 상탈 33 -> Le Labo Santal 33",
    "- 딥디크 도손 -> Diptyque Do Son",
    "- 샤넬 -> Chanel",
    "Rules:",
    "- Translate only what the user typed. Do not add a brand or any word the user did not type.",
    "- Use the official original spelling, including accents (e.g. Lumière).",
    "- Output only the brand and/or fragrance name. Drop concentration (오드퍼퓸, EDP, EDT),"
      + " volume (100ml) and generic words (향수, 추천, 남자, 여자).",
    "- Keep any English already in the query.",
    "- If the query is not about a fragrance or fragrance brand, set isFragrance to false"
      + " and english to an empty string."
  );

  @Resource(name = "geminiRestTemplate")
  private RestTemplate geminiRestTemplate;

  @Resource(name = "jacksonObjectMapper")
  private ObjectMapper objectMapper;

  @Value("${Globals.Gemini.Model}")
  private String model;

  @Value("${Globals.Gemini.ApiKey:}")
  private String apiKey;

  /**
   * @Program Name : translateKeyword()
   * @Describe : 향수 탐색 > 향수 검색 > 한글 검색어를 Gemini 로 영문 변환
   * @Param1 : keyword(String) 한글 검색어
   * @Date : 2026. 10. 20.
   * @Writer : 이건상
   * @History : 2026. 10. 20. | 이건상 | 최초작성
   */
  @Override
  public KeywordTranslation translateKeyword(String keyword) throws Exception {
    if (apiKey.trim().isEmpty()) {
      throw new IllegalStateException("Gemini API 키가 설정되지 않았습니다. (Globals.Gemini.ApiKey)");
    }
    JsonNode response = geminiRestTemplate.postForObject(
      "/models/{model}:generateContent",
      requestBody(keyword),
      JsonNode.class,
      model
    );
    return parse(response);
  }

  /** generateContent 요청: 지시문 + 검색어 + 응답 JSON 스키마 */
  private ObjectNode requestBody(String keyword) {
    ObjectNode body = objectMapper.createObjectNode();
    body.putObject("systemInstruction").putArray("parts").addObject().put("text", INSTRUCTION);
    body.putArray("contents").addObject()
      .put("role", "user")
      .putArray("parts").addObject().put("text", keyword);

    ObjectNode schema = objectMapper.createObjectNode();
    schema.put("type", "OBJECT");
    ObjectNode properties = schema.putObject("properties");
    properties.putObject("english").put("type", "STRING");
    properties.putObject("isFragrance").put("type", "BOOLEAN");
    schema.putArray("required").add("english").add("isFragrance");

    ObjectNode config = body.putObject("generationConfig");
    config.put("responseMimeType", "application/json");
    config.set("responseSchema", schema);
    return body;
  }

  /** 응답의 candidates[0].content.parts[].text 를 이어 붙여 JSON 으로 읽는다 */
  private KeywordTranslation parse(JsonNode response) {
    JsonNode parts = response == null ? null : response.path("candidates").path(0).path("content").path("parts");
    if (parts == null || !parts.isArray() || parts.size() == 0) {
      throw new RestClientException("Gemini 응답에 결과가 없습니다: " + response);
    }
    StringBuilder text = new StringBuilder();
    for (JsonNode part : parts) {
      text.append(part.path("text").asText(""));
    }
    try {
      return objectMapper.readValue(text.toString(), KeywordTranslation.class);
    } catch (JsonProcessingException e) {
      throw new RestClientException("Gemini 응답 JSON 형식 오류: " + text, e);
    }
  }
}
