package kr.co.seedssoft.platform.perfume.vo;

import lombok.*;

/**
 * @ClassName : SearchKeywordTranslation
 * @Description : 한글 향수 검색어 → 영문 검색어 변환 기록 (search_keyword_translation)
 * @author : 이건상
 * @since : 2026. 10. 20.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchKeywordTranslation {
	private Long searchKeywordTranslationId; // 변환ID
	private String keywordKo; // 한글 검색어 (KeywordUtil.normalize 로 정리한 값)
	private String keywordEn; // 영문 검색어 (Fragella 로 보낼 값)
	private String sourceType; // 출처 (AI / ADMIN)
	private Integer hitCount; // 저장된 변환을 다시 쓴 횟수
	private String createdAt; // 등록일시
	private String updatedAt; // 수정일시
}
