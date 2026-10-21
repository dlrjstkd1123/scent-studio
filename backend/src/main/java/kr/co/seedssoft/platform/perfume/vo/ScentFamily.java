package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : ScentFamily
 * @Description : 향 계열 (scent_family)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScentFamily {
	private String familyCode; // 계열코드 (CITRUS, WOODY ...)
	private String familyNameEn; // 계열 영문명
	private String familyNameKo; // 계열 한글명
	private String hexColorCode; // 계열 색상 (#RRGGBB)
	private Integer sortOrder; // 정렬순서
}
