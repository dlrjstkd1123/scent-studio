package kr.co.seedssoft.platform.admin.dto;

import lombok.*;

/**
 * @ClassName : ScentFamilyList
 * @Description : 관리자 > 향 계열 목록 (계열 + 소속 향료 수)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScentFamilyList {
	private String familyCode; // 계열코드
	private String familyNameEn; // 계열 영문명
	private String familyNameKo; // 계열 한글명
	private String hexColorCode; // 계열 색상 (#RRGGBB)
	private Integer sortOrder; // 정렬순서
	private Integer ingredientCount; // 소속 향료 수
}
