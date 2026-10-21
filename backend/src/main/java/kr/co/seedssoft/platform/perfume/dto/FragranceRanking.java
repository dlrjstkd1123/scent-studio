package kr.co.seedssoft.platform.perfume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

/**
 * @ClassName : FragranceRanking
 * @Description : Fragella 향수 응답 > Season Ranking / Occasion Ranking 항목
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FragranceRanking {
	private String name; // 계절(spring ~ winter) 또는 상황(casual, professional, night out)
	private Double score; // 점수 (클수록 잘 어울림)
}
