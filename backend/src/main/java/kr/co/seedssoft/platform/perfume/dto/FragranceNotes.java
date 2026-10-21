package kr.co.seedssoft.platform.perfume.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * @ClassName : FragranceNotes
 * @Description : Fragella 향수 응답 > Notes (탑 · 미들 · 베이스)
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FragranceNotes {
	@JsonProperty("Top")
	private List<FragranceNote> top; // 탑 노트
	@JsonProperty("Middle")
	private List<FragranceNote> middle; // 미들 노트
	@JsonProperty("Base")
	private List<FragranceNote> base; // 베이스 노트
}
