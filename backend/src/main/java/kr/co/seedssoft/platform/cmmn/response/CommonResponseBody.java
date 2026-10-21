package kr.co.seedssoft.platform.cmmn.response;

import lombok.Getter;

/**
 * @ClassName : CommonResponseBody
 * @Description : API 응답 객체. 모든 API 는 { code, message, response } 형태로 응답한다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@Getter
public class CommonResponseBody<T> {

    private final String code;
    private final String message;
    private final T response;

    public CommonResponseBody(CommonResponseCode responseCode) {
        this(responseCode, responseCode.getMessage(), null);
    }

    public CommonResponseBody(CommonResponseCode responseCode, T response) {
        this(responseCode, responseCode.getMessage(), response);
    }

    public CommonResponseBody(CommonResponseCode responseCode, String message) {
        this(responseCode, message, null);
    }

    public CommonResponseBody(CommonResponseCode responseCode, String message, T response) {
        this.code = responseCode.getCode();
        this.message = message;
        this.response = response;
    }
}
