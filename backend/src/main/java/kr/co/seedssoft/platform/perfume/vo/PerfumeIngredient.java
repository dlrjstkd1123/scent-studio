package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : PerfumeIngredient
 * @Description : 향수별 노트 피라미드 (perfume_ingredient)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PerfumeIngredient {
	private Long perfumeId; // 향수ID : perfume
	private Long ingredientId; // 향료ID : ingredient
	private String noteType; // 노트 위치 (TOP / MIDDLE / BASE)
	private Integer sortOrder; // 정렬순서
}
