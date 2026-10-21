package kr.co.seedssoft.platform.admin.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

/**
 * @ClassName : ImportResult
 * @Description : 관리자 > CSV 업로드 결과
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImportResult {
	private String importType; // 업로드 종류 (TYPE_ 상수)
	private String fileName; // 파일명
	private int totalCount; // 전체 건수
	private int insertedCount; // 추가 건수
	private int updatedCount; // 갱신 건수
	private int failedCount; // 실패 건수
	private int newIngredientCount; // 향수 업로드 시 새로 추가된 향료 수
	private String message; // 파일 전체 실패 사유
	private List<String> errors = new ArrayList<>(); // 행별 실패 사유

	public static final String TYPE_INGREDIENT_CSV = "INGREDIENT_CSV";
	public static final String TYPE_PERFUME_CSV = "PERFUME_CSV";
	public static final String TYPE_ALIAS_CSV = "ALIAS_CSV";
	public static final String TYPE_ACCORD_CSV = "ACCORD_CSV";
	public static final String TYPE_KEYWORD_CSV = "KEYWORD_CSV";

	public ImportResult(String importType, String fileName) {
		this.importType = importType;
		this.fileName = fileName;
	}

	/** 실패한 행 추가 */
	public void addError(String row, String reason) {
		failedCount++;
		errors.add(row + "행: " + reason);
	}
}
