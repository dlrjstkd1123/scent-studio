package kr.co.seedssoft.platform.perfume.dto;

import java.util.List;
import kr.co.seedssoft.platform.perfume.vo.AccordFamilyMap;
import kr.co.seedssoft.platform.perfume.vo.Ingredient;
import kr.co.seedssoft.platform.perfume.vo.ScentFamily;

import lombok.*;

/**
 * @ClassName : NoteDictionary
 * @Description : 향수 탐색 > 노트 사전 (향 계열 색상 + 향료→계열 + 별칭 + 어코드→계열)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NoteDictionary {
	private List<ScentFamily> families; // 향 계열 목록
	private List<Ingredient> ingredients; // 향료 목록
	private List<IngredientAliasList> aliases; // 향료 별칭 목록
	private List<AccordFamilyMap> accords; // 어코드 → 계열 목록
}
