package kr.co.seedssoft.platform.perfume.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

/**
 * @ClassName : FragranceSearchList
 * @Description : 향수 검색 응답 (입력한 검색어 · 실제로 Fragella 에 보낸 검색어 · 결과 목록)
 *                한글로 검색하면 searchKeyword 가 영문으로 바뀐 값이라 화면에 "○○로 검색했어요" 를 보여줄 수 있다.
 * @author : 이건상
 * @since : 2026. 10. 20.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FragranceSearchList {
	private String keyword; // 사용자가 입력한 검색어
	private String searchKeyword; // Fragella 에 보낸 검색어 (한글이면 영문 변환 값, 변환 실패 시 null)
	private Boolean translated; // 한글 → 영문으로 바꿨는지
	private List<Fragrance> fragrances = new ArrayList<>(); // 검색 결과 (Fragella 응답 그대로)
}
