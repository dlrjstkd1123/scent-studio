package kr.co.seedssoft.platform.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseBody;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseCode;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @ClassName : AdminKeyInterceptor.java
 * @Description : /api/admin/** 요청은 헤더 X-Admin-Key 가 Globals.Admin.Key 와 같아야 통과한다.
 *                Globals.Admin.Key 가 비어 있으면 관리자 API 전체를 막는다.
 *                막을 때는 HTTP 401 + { code: "140", message, response: null }
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public class AdminKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Admin-Key";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final byte[] adminKey;

    public AdminKeyInterceptor(String adminKey) {
        this.adminKey = StringUtils.hasText(adminKey)
            ? adminKey.trim().getBytes(StandardCharsets.UTF_8)
            : null;
    }

    @Override
    public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
    ) throws Exception {
      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return true; // CORS preflight
      }

      String given = request.getHeader(HEADER);
      boolean ok = adminKey != null
          && given != null
          && MessageDigest.isEqual(adminKey, given.trim().getBytes(StandardCharsets.UTF_8));
      if (ok) {
        return true;
      }

      String message = adminKey == null
          ? "관리자 키가 서버에 설정되지 않았습니다."
          : "관리자 키가 올바르지 않습니다.";
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write(
          OBJECT_MAPPER.writeValueAsString(new CommonResponseBody<>(CommonResponseCode.COMMON_140, message))
      );
      return false;
    }
}
