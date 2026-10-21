package kr.co.seedssoft.platform.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @ClassName : EgovConfigAppMapper.java
 * @Description : MyBatis (기존 context-mapper.xml 역할).
 *                - 각 업무의 dao 패키지 인터페이스(@Mapper)를 찾아 구현을 자동으로 만든다.
 *                - SQL XML 의 namespace = DAO 인터페이스 전체 경로, SQL id = 메서드 이름.
 *                - SQL 파일은 resources/egovframework/mapper/ 아래 *_{DbType}.xml 로 만든다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
@MapperScan(basePackages = "kr.co.seedssoft.platform.*.dao")
public class EgovConfigAppMapper {

    @Value("${Globals.DbType}")
    private String dbType;

    @Bean(name = "sqlSession")
    public SqlSessionFactoryBean sqlSession(
      DataSource dataSource
    ) throws IOException {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

      SqlSessionFactoryBean sqlSession = new SqlSessionFactoryBean();
      sqlSession.setDataSource(dataSource);
      sqlSession.setConfigLocation(
          resolver.getResource("classpath:/egovframework/mapper/config/mapper-config.xml")
      );
      sqlSession.setMapperLocations(
          resolver.getResources("classpath:/egovframework/mapper/**/*_" + dbType + ".xml")
      );
      return sqlSession;
    }
}
