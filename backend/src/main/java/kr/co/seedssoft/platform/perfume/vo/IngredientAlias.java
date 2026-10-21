package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : IngredientAlias
 * @Description : 향료 별칭 (ingredient_alias)
 * @author : 이건상
 * @since : 2026. 10. 11.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IngredientAlias {
	private Long ingredientAliasId; // 별칭ID
	private String aliasName; // 별칭 (영문)
	private Long ingredientId; // 향료ID : ingredient
	private String createdAt; // 등록일시
}
