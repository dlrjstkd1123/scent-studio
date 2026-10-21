package kr.co.seedssoft.platform.perfume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

/**
 * @ClassName : FragranceNote
 * @Description : Fragella 향수 응답 > Notes 의 노트 하나
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FragranceNote {
	private String name; // 노트명 (영문)
	private String imageUrl; // 노트 이미지
}
