package hong;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 전역 상수 정의 클래스 (파일 경로, JSON 키, 추출 타입 등 매직 문자열 제거용)
 */
public class Constants {

	// 개행 문자 제거용 정규식
	public static final String REPLACE_RN = "(\\r\\n|\\r|\\n|\\n\\r)";

	// === 실행 환경(DEV) 경로 ===
	// 개발용 결과 엑셀 파일명 (시각 suffix 포함)
	public static final String EXCEL_FILE_NM_DEV = "00.HONG_"
			+ LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".xlsx";

	// 개발용 PDF 입력 폴더 경로
	public static final String PDF_FOLDER_PATH_DEV = "config/pdf/";

	// 개발용 설정 파일(hong.json) 경로
	public static final String SETTING_FILE_PATH_DEV = "config/hong.json";

	// === 실행 환경(PROD) 경로 ===
	// 배포용 결과 엑셀 파일명
	public static final String EXCEL_FILE_NM_PROD = "00.HONG.xlsx";

	// 배포용 PDF 입력 폴더 경로
	public static final String PDF_FOLDER_PATH_PROD = "../../";

	// 배포용 설정 파일(hong.json) 경로
	public static final String SETTING_FILE_PATH_PROD = "../hong.json";

	// PDF -> xlsx 변환 사본 저장 폴더명
	public static final String EXCEL_FOLDER_NM = "HONG_EXCEL/";

	// 실행 시각 기반의 결과 엑셀 파일명 생성 (반복 실행 시 덮어쓰기 방지)
	public static String buildExcelFileName() {
		return "00.HONG_" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".xlsx";
	}

	// === hong.json 최상위 키 ===
	// 세금번호 -> 업체 매핑
	public static final String JSON_KEY_TYPE = "type";
	// 업체별 추출 규칙
	public static final String JSON_KEY_PROPERTY = "property";
	// 엑셀 헤더 배열
	public static final String JSON_KEY_HEADER = "header";
	// 추출 항목 코드 배열
	public static final String JSON_KEY_ITEM = "item";
	// 항목별 추출 옵션
	public static final String JSON_KEY_OPTION = "option";

	// === 단일 세금번호에 다중 브랜드가 묶인 경우의 분기 키 ===
	// 본문 키워드(독/영 등)로 브랜드 결정
	public static final String TYPE_HONG_01 = "TYPE_HONG_01";
	// 단일 브랜드 + VAT 미식별 시 KEYWORD 목록으로 추정
	public static final String TYPE_HONG_02 = "TYPE_HONG_02";
	// 예약(미사용)
	public static final String TYPE_HONG_03 = "TYPE_HONG_03";
	// TYPE_HONG_02 의 브랜드명 키
	public static final String JSON_KEY_BRAND = "BRAND";
	// TYPE_HONG_02 의 키워드 목록 키
	public static final String JSON_KEY_KEYWORD = "KEYWORD";

	// === 추출 소스 구분 ===
	// PDF 본문 텍스트에서 추출
	public static final String SOURCE_PDF = "PDF";
	// 변환된 엑셀 셀에서 추출
	public static final String SOURCE_EXCEL = "EXCEL";

	// === PDF 추출 방식 타입 코드 ===
	// 키워드 뒤 텍스트
	public static final String EXTRACT_TYPE_01 = "01";
	// 시작/끝 토큰 누적 인덱스
	public static final String EXTRACT_TYPE_02 = "02";
	// 두 토큰 사이
	public static final String EXTRACT_TYPE_03 = "03";
	// 역방향(lastIndexOf)
	public static final String EXTRACT_TYPE_04 = "04";
	// 특정 라인 번호
	public static final String EXTRACT_TYPE_05 = "05";
	// 고정 상수값
	public static final String EXTRACT_TYPE_06 = "06";

	// === 파일 확장자/패턴 ===
	// PDF 확장자(대문자)
	public static final String EXT_PDF = "PDF";
	// 엑셀 확장자
	public static final String EXT_XLSX = "xlsx";
	// 파일명 표시용 .PDF
	public static final String EXT_DOT_PDF = ".PDF";
	// 라인 분리 정규식
	public static final String LINE_SPLIT_REGEX = "\\n";
	// VAT 번호 정규식 (DE + 숫자 9자리)
	public static final String VAT_ID_PATTERN = "DE[0-9]{9}|DE [0-9]{9}";

	// === 오류/안내 메시지 ===
	// 오류 메시지 접두어
	public static final String ERROR_PREFIX = "[ERROR] ";
	// 옵션 항목 부재 메시지
	public static final String MSG_NO_OPTION = " 항목에 해당하는 option 항목이 존재하지 않습니다.";
	// 설정정보 부재 메시지
	public static final String MSG_NO_SETTING = " 설정정보를 불러 수 없습니다.";
	// 항목 추출 오류 메시지
	public static final String MSG_ITEM_ERROR = " 오류 발생";
}
