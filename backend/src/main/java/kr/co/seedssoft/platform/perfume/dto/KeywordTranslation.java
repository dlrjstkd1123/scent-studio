package kr.co.seedssoft.platform.perfume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

/**
 * @ClassName : KeywordTranslation
 * @Description : Gemini 검색어 변환 결과 (Gemini 가 돌려주는 JSON 그대로)
 *                예) {"english": "Dior Sauvage", "isFragrance": true}
 * @author : 이건상
 * @since : 2026. 10. 20.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeywordTranslation {
	private String english; // 영문 검색어 (브랜드 · 향수 공식 영문명)
	private Boolean isFragrance; // 향수 · 브랜드 검색어인지 (false 면 english 는 빈 값)
}
