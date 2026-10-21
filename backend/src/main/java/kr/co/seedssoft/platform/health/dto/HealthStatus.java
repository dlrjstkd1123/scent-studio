package kr.co.seedssoft.platform.health.dto;

import lombok.*;

/**
 * @ClassName : HealthStatus
 * @Description : 연결 상태 (서버 · DB)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HealthStatus {
	private String server; // 서버 상태 (UP)
	private String db; // DB 상태 (UP / DOWN)
	private String dbVersion; // DB 버전
	private String error; // DB 연결 실패 사유
}
