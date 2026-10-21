package kr.co.seedssoft.platform.cmmn.util;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @ClassName : CsvUtil.java
 * @Description : 관리자 업로드용 CSV 읽기.
 *                - 첫 줄은 헤더. 헤더는 소문자/앞뒤 공백 제거로 맞춘다.
 *                - 한글 엑셀에서 저장한 CSV(MS949)와 "CSV UTF-8" 둘 다 읽는다.
 * @author : 이건상
 * @since : 2026. 09. 27.
 */
public final class CsvUtil {

    private static final Charset MS949 = Charset.forName("MS949");

    private CsvUtil() {
    }

    /**
     * @return 행 목록. 각 행은 헤더(소문자) → 값(앞뒤 공백 제거, 빈 값은 null). "_row" 에 엑셀 기준 행 번호.
     */
    public static List<Map<String, String>> read(byte[] bytes, String... requiredHeaders) throws IOException {
        String text = decode(bytes);
        List<Map<String, String>> rows = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = CSVParser.parse(new StringReader(text), format)) {
            List<String> headers = new ArrayList<>();
            for (String h : parser.getHeaderNames()) {
                headers.add(h.trim().toLowerCase(Locale.ROOT));
            }
            for (String required : requiredHeaders) {
                if (!headers.contains(required)) {
                    throw new IllegalArgumentException("CSV 첫 줄(헤더)에 '" + required + "' 컬럼이 없습니다.");
                }
            }

            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                boolean empty = true;
                for (int i = 0; i < headers.size() && i < record.size(); i++) {
                    String value = record.get(i);
                    if (value != null && !value.isEmpty()) {
                        row.put(headers.get(i), value);
                        empty = false;
                    }
                }
                if (!empty) {
                    row.put("_row", String.valueOf(record.getRecordNumber() + 1)); // 헤더가 1행
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /** UTF-8(BOM 포함) 우선, UTF-8 이 아니면 MS949 로 읽는다. */
    private static String decode(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, MS949);
        }
    }
}
