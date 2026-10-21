package kr.co.seedssoft.platform.perfume.dto;

import lombok.*;

/**
 * @ClassName : IngredientAliasList
 * @Description : 향료 별칭 + 가리키는 향료 영문명 (노트 사전 · 별칭 CSV 업로드)
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IngredientAliasList {
	private String aliasName; // 별칭 (영문)
	private String ingredientNameEn; // 향료 영문명 : ingredient
}
