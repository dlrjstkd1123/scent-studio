package kr.co.seedssoft.platform.config;

import kr.co.seedssoft.platform.admin.web.AdminDataController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName : EgovConfigWeb.java
 * @Description : 웹 설정.
 *                개발 중에는 Vite 프록시(/api -> 8090)로 호출하므로 CORS가 필요 없지만,
 *                프론트를 별도 도메인에 배포할 경우를 대비해 허용 주소를 Globals.Allow.Origin 으로 둔다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Configuration
public class EgovConfigWeb implements WebMvcConfigurer {

    @Value("${Globals.Allow.Origin}")
    private String[] allowedOrigins;

    @Value("${Globals.Admin.Key:}")
    private String adminKey;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminKeyInterceptor(adminKey))
            .addPathPatterns(AdminDataController.API.BASE_PATTERN);
    }
}
