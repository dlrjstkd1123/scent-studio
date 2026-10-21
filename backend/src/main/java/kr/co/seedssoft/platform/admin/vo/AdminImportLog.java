package kr.co.seedssoft.platform.admin.vo;

import lombok.*;

/**
 * @ClassName : AdminImportLog
 * @Description : 관리자 데이터 업로드 이력 (admin_import_log)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminImportLog {
	private Long logId; // 이력ID
	private String importType; // 업로드 종류 (INGREDIENT_CSV / PERFUME_CSV / ALIAS_CSV / ACCORD_CSV / KEYWORD_CSV)
	private String fileName; // 파일명
	private Integer totalCount; // 전체 건수
	private Integer insertedCount; // 추가 건수
	private Integer updatedCount; // 갱신 건수
	private Integer failedCount; // 실패 건수
	private String message; // 메시지 (실패 사유 등)
	private String createdAt; // 등록일시 (yyyy-MM-dd HH:mm:ss, KST)
}
