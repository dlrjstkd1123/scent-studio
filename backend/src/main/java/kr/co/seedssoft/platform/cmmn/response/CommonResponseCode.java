package kr.co.seedssoft.platform.cmmn.response;

/**
 * @ClassName : CommonResponseCode
 * @Description : API 처리 결과 코드 (hiucs 와 같은 코드 체계)
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public final class CommonResponseCode {

    // 100~199 : 공통
    public static final CommonResponseCode COMMON_100 = new CommonResponseCode("100", "정상처리 되었습니다.");
    public static final CommonResponseCode COMMON_101 = new CommonResponseCode("101", "일치하는 데이터가 존재하지 않습니다.");
    public static final CommonResponseCode COMMON_102 = new CommonResponseCode("102", "데이터베이스 오류가 발생했습니다.");
    public static final CommonResponseCode COMMON_103 = new CommonResponseCode("103", "파라미터가 올바르지 않습니다.");
    public static final CommonResponseCode COMMON_104 = new CommonResponseCode("104", "필수 값이 누락되었습니다.");
    public static final CommonResponseCode COMMON_105 = new CommonResponseCode("105", "데이터 등록에 실패했습니다.");
    public static final CommonResponseCode COMMON_106 = new CommonResponseCode("106", "데이터 수정에 실패했습니다.");
    public static final CommonResponseCode COMMON_107 = new CommonResponseCode("107", "데이터 삭제에 실패했습니다.");
    public static final CommonResponseCode COMMON_108 = new CommonResponseCode("108", "파일 업로드에 실패했습니다.");
    public static final CommonResponseCode COMMON_132 = new CommonResponseCode("132", "이미 존재하는 데이터입니다.");
    public static final CommonResponseCode COMMON_140 = new CommonResponseCode("140", "접근 권한이 부족합니다.");
    // 150~ : 외부 API (Fragella 등)
    public static final CommonResponseCode COMMON_150 = new CommonResponseCode("150", "외부 API 호출에 실패했습니다.");
    public static final CommonResponseCode COMMON_151 = new CommonResponseCode("151", "외부 API 이번 달 사용량을 모두 사용했습니다.");
    public static final CommonResponseCode COMMON_152 = new CommonResponseCode("152", "한글 검색어를 영문으로 바꾸지 못했습니다.");

    public static final CommonResponseCode COMMON_199 = new CommonResponseCode("199", "알 수 없는 오류가 발생했습니다.");

    private final String code;
    private final String message;

    private CommonResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
