package kr.co.seedssoft.platform.health.web;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.cmmn.response.CommonResponseBody;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseCode;
import kr.co.seedssoft.platform.health.dto.HealthStatus;
import kr.co.seedssoft.platform.health.service.HealthService;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName : HealthController.java
 * @Description : 프론트-백엔드-DB 연결 확인용 엔드포인트.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@RestController
public class HealthController {

  @Resource(name = "healthService")
  private HealthService healthService;

  public static class API {
    public static final String BASE = "/api";
    public static final String BASE_PATTERN = BASE + "/**";

    /* 연결 확인 */
    public static final String GET_HEALTH = BASE + "/health"; // 서버 · DB 연결 상태
  }

  /**
   * @Program Name : selectHealth()
   * @Describe : 연결 확인 > 서버 · DB 연결 상태 (DB 연결 안 되면 102 + 상태 정보)
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @GetMapping(API.GET_HEALTH)
  public ResponseEntity<CommonResponseBody<HealthStatus>> selectHealth(
  ) {
    HealthStatus healthStatus = new HealthStatus();
    healthStatus.setServer("UP");

    CommonResponseCode responseCode = CommonResponseCode.COMMON_100;
    try {
      healthStatus.setDbVersion(healthService.selectDbVersion());
      healthStatus.setDb("UP");
    } catch (Exception e) {
      healthStatus.setDb("DOWN");
      healthStatus.setError(NestedExceptionUtils.getMostSpecificCause(e).getMessage());
      responseCode = CommonResponseCode.COMMON_102;
    }
    return ResponseEntity.ok(new CommonResponseBody<>(responseCode, healthStatus));
  }
}
