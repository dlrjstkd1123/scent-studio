package kr.co.seedssoft.platform.health.service;

/**
 * @ClassName : HealthService.java
 * @Description : 연결 상태 확인 서비스.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public interface HealthService {

  /* DB 버전 조회 (DB 에 연결되지 않으면 예외) */
  String selectDbVersion() throws Exception;
}
