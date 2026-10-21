package kr.co.seedssoft.platform.perfume.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * @ClassName : SimilarFragranceList
 * @Description : Fragella 비슷한 향수 응답 (GET /fragrances/similar)
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimilarFragranceList {
	@JsonProperty("similar_to")
	private String similarTo; // 기준이 된 향수명 (이름 유사 검색으로 고른 것)
	@JsonProperty("similar_fragrances")
	private List<Fragrance> similarFragrances = new ArrayList<>(); // 비슷한 향수 (SimilarityScore 포함)
}
