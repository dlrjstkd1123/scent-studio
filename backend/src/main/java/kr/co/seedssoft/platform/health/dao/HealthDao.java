package kr.co.seedssoft.platform.health.dao;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * @ClassName : HealthDao.java
 * @Description : 연결 상태 확인 DAO (SQL: mapper/platform/health/Health_SQL_postgres.xml)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Mapper("HealthDao")
public interface HealthDao {

  String selectDbVersion();
}
