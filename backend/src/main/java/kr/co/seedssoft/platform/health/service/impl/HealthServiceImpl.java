package kr.co.seedssoft.platform.health.service.impl;

import javax.annotation.Resource;

import kr.co.seedssoft.platform.health.dao.HealthDao;
import kr.co.seedssoft.platform.health.service.HealthService;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @ClassName : HealthServiceImpl.java
 * @Description : 연결 상태 확인 서비스 구현 (DB 버전 조회)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Service("healthService")
public class HealthServiceImpl extends EgovAbstractServiceImpl implements HealthService {

  @Resource(name = "HealthDao")
  private HealthDao healthDao;

  /**
   * @Program Name : selectDbVersion()
   * @Describe : 연결 확인 > DB 버전 조회
   * @Date : 2026. 09. 27.
   * @Writer : 이건상
   * @History : 2026. 09. 27. | 이건상 | 최초작성
   */
  @Override
  public String selectDbVersion() throws Exception {
    return healthDao.selectDbVersion();
  }
}
