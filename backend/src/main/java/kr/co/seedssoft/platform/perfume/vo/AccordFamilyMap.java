package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : AccordFamilyMap
 * @Description : 메인 어코드 → 향 계열 매핑 (accord_family_map)
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccordFamilyMap {
	private Long accordFamilyMapId; // 매핑ID
	private String accordName; // 어코드명 (소문자)
	private String familyCode; // 향 계열 코드 : scent_family
	private String createdAt; // 등록일시
	private String updatedAt; // 수정일시
}
