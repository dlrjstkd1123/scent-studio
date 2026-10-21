package kr.co.seedssoft.platform.cmmn.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @ClassName : KeywordUtil.java
 * @Description : 검색어 정리 · 한글 포함 여부.
 *                한글 검색어 변환 기록(search_keyword_translation.keyword_ko)은 이 규칙으로 정리한 값을 키로 쓴다.
 * @author : 이건상
 * @since : 2026. 10. 20.
 */
public final class KeywordUtil {

    /** 한글 (완성형 + 자음 · 모음) */
    private static final Pattern HANGUL = Pattern.compile("[가-힣ㄱ-ㅎㅏ-ㅣ]");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private KeywordUtil() {
    }

    /** 한글이 한 글자라도 있으면 true */
    public static boolean hasHangul(String text) {
        return text != null && HANGUL.matcher(text).find();
    }

    /** 앞뒤 공백 제거 · 공백 여러 개는 하나로 · 영문은 소문자 ("  디올  Sauvage " → "디올 sauvage") */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return SPACES.matcher(text.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }
}
