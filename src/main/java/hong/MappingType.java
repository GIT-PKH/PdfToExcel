package hong;

/**
 * 세금번호(VAT) 매핑 유형
 */
public enum MappingType {

	// 단순 매핑 (세금번호 -> 업체 문자열)
	SIMPLE,

	// 키워드 분기 (본문 키워드 -> 업체, H&M/COS·apolux/APO.COM 등)
	TYPE_HONG_01,

	// 단일 브랜드 + KEYWORD 목록으로 추정
	TYPE_HONG_02
}
