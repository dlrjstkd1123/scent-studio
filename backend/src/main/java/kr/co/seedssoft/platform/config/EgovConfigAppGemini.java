package kr.co.seedssoft.platform.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName : EgovConfigAppGemini.java
 * @Description : Google Gemini API 호출용 RestTemplate (한글 향수 검색어 → 영문 변환).
 *                주소 · 키는 globals.properties / globals-local.properties 의 Globals.Gemini.* 값.
 *                모든 요청에 x-goog-api-key 헤더를 붙인다 (키를 URL 에 넣지 않아 로그에 남지 않게).
 * @author : 이건상
 * @since : 2026. 10. 20.
 */
@Configuration
public class EgovConfigAppGemini {

    /** 연결 대기 / 응답 대기 시간 */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);

    @Value("${Globals.Gemini.BaseUrl}")
    private String baseUrl;

    @Value("${Globals.Gemini.ApiKey:}")
    private String apiKey;

    @Bean(name = "geminiRestTemplate")
    public RestTemplate geminiRestTemplate(
      RestTemplateBuilder builder
    ) {
        return builder
            .rootUri(baseUrl)
            .defaultHeader("x-goog-api-key", apiKey.trim())
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(READ_TIMEOUT)
            .build();
    }
}
