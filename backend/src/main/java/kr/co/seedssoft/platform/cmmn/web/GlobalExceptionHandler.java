package kr.co.seedssoft.platform.cmmn.web;

import kr.co.seedssoft.platform.cmmn.response.CommonResponseBody;
import kr.co.seedssoft.platform.cmmn.response.CommonResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @ClassName : GlobalExceptionHandler
 * @Description : 컨트롤러에서 처리하지 않은 예외를 { code, message, response } 응답으로 바꾼다.
 *                (hiucs 처럼 HTTP 200 + code 로 결과를 알린다)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** DB 오류 (DB 연결 실패로 트랜잭션을 시작하지 못한 경우 포함) */
    @ExceptionHandler({
      DataAccessException.class,
      TransactionException.class
    })
    public ResponseEntity<CommonResponseBody<Void>> handleDataAccess(
      Exception e
    ) {
      LOGGER.error("DB 오류", e);
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_102));
    }

    /** 잘못된 파라미터 */
    @ExceptionHandler({
      IllegalArgumentException.class,
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class
    })
    public ResponseEntity<CommonResponseBody<Void>> handleBadParameter(
      Exception e
    ) {
      String message = e instanceof IllegalArgumentException
          ? e.getMessage()
          : CommonResponseCode.COMMON_103.getMessage();
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_103, message));
    }

    /** 업로드 파일 크기 초과 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponseBody<Void>> handleUploadSize(
      MaxUploadSizeExceededException e
    ) {
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_108, "파일은 10MB 까지 올릴 수 있습니다."));
    }

    /** 그 외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseBody<Void>> handleUnknown(
      Exception e
    ) {
      LOGGER.error("알 수 없는 오류", e);
      return ResponseEntity.ok(new CommonResponseBody<>(CommonResponseCode.COMMON_199));
    }
}
