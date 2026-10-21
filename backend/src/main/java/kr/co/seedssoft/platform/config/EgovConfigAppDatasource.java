package kr.co.seedssoft.platform.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : EgovConfigAppDatasource.java
 * @Description : DataSource (기존 context-datasource.xml 역할).
 *                Globals.DbType 값에 따라 Globals.{DbType}.* 접속정보를 읽는다. (현재 postgres)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
public class EgovConfigAppDatasource {

    @Value("${Globals.${Globals.DbType}.DriverClassName}")
    private String driverClassName;

    @Value("${Globals.${Globals.DbType}.Url}")
    private String url;

    @Value("${Globals.${Globals.DbType}.UserName}")
    private String userName;

    @Value("${Globals.${Globals.DbType}.Password}")
    private String password;

    @Value("${Globals.${Globals.DbType}.MaxPoolSize:5}")
    private int maxPoolSize;

    @Bean(name = "dataSource", destroyMethod = "close")
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(10000);
        return dataSource;
    }
}
