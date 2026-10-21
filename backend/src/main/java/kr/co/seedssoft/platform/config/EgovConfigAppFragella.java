package kr.co.seedssoft.platform.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName : EgovConfigAppFragella.java
 * @Description : Fragella 향수 API 호출용 RestTemplate.
 *                주소와 키는 globals.properties / globals-local.properties 의 Globals.Fragella.* 값.
 *                모든 요청에 x-api-key 헤더를 붙이므로 서비스에서는 경로만 쓰면 된다. (예: "/fragrances?search={search}")
 *                User-Agent 를 꼭 붙인다: Java 기본값(Java/1.8...)은 Fragella 앞단(Cloudflare)이 403(error code 1010)으로 막는다.
 * @author : 이건상
 * @since : 2026. 10. 11.
 */
@Configuration
public class EgovConfigAppFragella {

    /** 연결 대기 / 응답 대기 시간 */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private static final String USER_AGENT = "ScentStudio/1.0";

    @Value("${Globals.Fragella.BaseUrl}")
    private String baseUrl;

    @Value("${Globals.Fragella.ApiKey:}")
    private String apiKey;

    @Bean(name = "fragellaRestTemplate")
    public RestTemplate fragellaRestTemplate(
      RestTemplateBuilder builder
    ) {
        return builder
            .rootUri(baseUrl)
            .defaultHeader("x-api-key", apiKey.trim())
            .defaultHeader("User-Agent", USER_AGENT)
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(READ_TIMEOUT)
            .build();
    }
}
