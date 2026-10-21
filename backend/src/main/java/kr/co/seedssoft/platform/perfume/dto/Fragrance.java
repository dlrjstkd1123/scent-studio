package kr.co.seedssoft.platform.perfume.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * @ClassName : Fragrance
 * @Description : Fragella 향수 응답 (Fragrance Object). 필드명을 Fragella 그대로 주고받는다.
 *                https://api.fragella.com/docs.html (GET /fragrances, /fragrances/similar)
 *                화면은 utils/fragella.js 의 fromFragella() 로 변환해서 쓴다.
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Fragrance {
	@JsonProperty("_id")
	private String id; // 향수ID (slug, 예: Dior-Sauvage)
	@JsonProperty("Name")
	private String name; // 향수명
	@JsonProperty("Brand")
	private String brand; // 브랜드
	@JsonProperty("founder")
	private String founder; // 창립자 · 조향사 (확인 못 하면 빈 문자열)
	@JsonProperty("Year")
	private String year; // 출시연도 (문자열)
	@JsonProperty("rating")
	private String rating; // 평점 (문자열, 예: "4.16")
	@JsonProperty("Country")
	private String country; // 국가
	@JsonProperty("Gender")
	private String gender; // 성별 (men / women / unisex)
	@JsonProperty("OilType")
	private String oilType; // 부향률 (Eau de Toilette 등)
	@JsonProperty("Longevity")
	private String longevity; // 지속력
	@JsonProperty("Sillage")
	private String sillage; // 확산력
	@JsonProperty("Popularity")
	private String popularity; // 인기도
	@JsonProperty("Image URL")
	private String imageUrl; // 향수 이미지
	@JsonProperty("Image URL Transparent")
	private String imageUrlTransparent; // 배경 없는 향수 이미지
	@JsonProperty("Season Ranking")
	private List<FragranceRanking> seasonRanking; // 계절별 점수
	@JsonProperty("Occasion Ranking")
	private List<FragranceRanking> occasionRanking; // 상황별 점수
	@JsonProperty("General Notes")
	private List<String> generalNotes; // 대표 노트
	@JsonProperty("Main Accords")
	private List<String> mainAccords; // 메인 어코드 (강한 순)
	@JsonProperty("Main Accords Percentage")
	private Map<String, String> mainAccordsPercentage; // 어코드별 강도 (Dominant ~ Subtle, 순서 유지)
	@JsonProperty("Notes")
	private FragranceNotes notes; // 노트 피라미드
	@JsonProperty("SimilarityScore")
	private Double similarityScore; // 유사도 (/fragrances/similar 응답에만 있음)
}
