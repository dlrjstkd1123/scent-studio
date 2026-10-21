package kr.co.seedssoft.platform.admin.dto;

import lombok.*;

/**
 * @ClassName : UpsertResult
 * @Description : INSERT ... ON CONFLICT ... RETURNING 결과
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpsertResult {
	private Long id; // 저장된 행의 ID
	private Boolean inserted; // true: 새로 추가, false: 기존 행 갱신
}
