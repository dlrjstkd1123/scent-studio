package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : Perfume
 * @Description : 향수 (perfume)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Perfume {
	private Long perfumeId; // 향수ID
	private String brandName; // 브랜드명
	private String perfumeName; // 향수명
	private String concentration; // 농도 (EDP, EDT ...)
	private Integer releaseYear; // 출시연도
	private String createdAt; // 등록일시
	private String updatedAt; // 수정일시
}
