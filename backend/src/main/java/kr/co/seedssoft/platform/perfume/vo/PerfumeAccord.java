package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : PerfumeAccord
 * @Description : 향수별 메인 어코드 (perfume_accord)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PerfumeAccord {
	private Long perfumeId; // 향수ID : perfume
	private String accordName; // 어코드명
	private Integer strength; // 강도 (0~100)
}
