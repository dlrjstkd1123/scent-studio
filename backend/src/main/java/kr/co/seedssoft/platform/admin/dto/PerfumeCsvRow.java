package kr.co.seedssoft.platform.admin.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.*;

/**
 * @ClassName : PerfumeCsvRow
 * @Description : 관리자 > 향수 CSV 한 줄 (향수 + 노트 + 어코드)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PerfumeCsvRow {
	private String brandName; // 브랜드명
	private String perfumeName; // 향수명
	private String concentration; // 농도
	private Integer releaseYear; // 출시연도
	private List<String> topNotes = new ArrayList<>(); // 탑 노트
	private List<String> middleNotes = new ArrayList<>(); // 미들 노트
	private List<String> baseNotes = new ArrayList<>(); // 베이스 노트
	private Map<String, Integer> accords = new LinkedHashMap<>(); // 어코드명 → 강도 (없으면 null)
}
