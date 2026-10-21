package kr.co.seedssoft.platform.admin.dto;

import lombok.*;

/**
 * @ClassName : AdminSummary
 * @Description : 관리자 > 데이터 현황 (테이블별 건수)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminSummary {
	private Integer perfumeCount; // 향수 수
	private Integer ingredientCount; // 향료 수
	private Integer unassignedIngredientCount; // 계열 미지정 향료 수
	private Integer perfumeNoteCount; // 향수-노트 연결 수
	private Integer perfumeAccordCount; // 향수-어코드 수
	private Integer ingredientAliasCount; // 향료 별칭 수
	private Integer accordFamilyMapCount; // 어코드→계열 매핑 수
	private Integer keywordTranslationCount; // 한글 검색어 변환 수
	private Integer ifraStandardCount; // IFRA 규제 물질 수
	private Integer familyCount; // 향 계열 수
}
