package kr.co.seedssoft.platform.config;

import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @ClassName : EgovConfigAppProperties.java
 * @Description : globals.properties 로딩 + EgovPropertyService (기존 context-properties.xml 역할).
 *                globals.properties 의 값은 다른 설정 클래스에서 @Value("${Globals.xxx}") 로 꺼내 쓴다.
 *                뒤에 선언한 globals-local.properties(선택, git 제외)가 같은 키를 덮어쓴다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
@PropertySource(value = "classpath:/egovframework/egovProps/globals.properties", encoding = "UTF-8")
@PropertySource(value = "classpath:/egovframework/egovProps/globals-local.properties", encoding = "UTF-8",
        ignoreResourceNotFound = true)
public class EgovConfigAppProperties {

    @Bean
    public EgovPropertyServiceImpl propertiesService() {
        Map<String, String> properties = new HashMap<>();
        properties.put("pageUnit", "10");
        properties.put("pageSize", "10");

        EgovPropertyServiceImpl propertiesService = new EgovPropertyServiceImpl();
        propertiesService.setProperties(properties);
        return propertiesService;
    }
}
