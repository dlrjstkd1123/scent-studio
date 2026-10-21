package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : Ingredient
 * @Description : 향료 (ingredient)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ingredient {
	private Long ingredientId; // 향료ID
	private String ingredientNameEn; // 향료 영문명
	private String ingredientNameKo; // 향료 한글명
	private String familyCode; // 계열코드 : scent_family
	private String defaultNoteType; // 일반적인 휘발도 (TOP / MIDDLE / BASE)
	private String createdAt; // 등록일시
	private String updatedAt; // 수정일시
}
