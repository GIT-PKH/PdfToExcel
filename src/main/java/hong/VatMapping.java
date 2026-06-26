package hong;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * hong.json type 블록의 세금번호 매핑 1건을 표현하는 모델
 */
@Data
public class VatMapping {

	// 세금번호 키 (PDF에 찍힌 표기 그대로, 공백 정규화 금지)
	private String vatKey;

	// 매핑 유형
	private MappingType mappingType = MappingType.SIMPLE;

	// 단순 매핑 또는 TYPE_HONG_02 의 브랜드명
	private String brandId;

	// TYPE_HONG_01: 키워드 -> 업체 (순서 유지)
	private Map<String, String> branchEntries = new LinkedHashMap<>();

	// TYPE_HONG_02: 식별 키워드 목록
	private List<String> keywords = new ArrayList<>();

	// 이 매핑이 참조하는 업체(brand) 목록 (참조 무결성 검사용)
	public List<String> referencedBrands() {
		List<String> brands = new ArrayList<>();
		if (mappingType == MappingType.TYPE_HONG_01) {
			brands.addAll(branchEntries.values());
		} else {
			if (StringUtils.isNotBlank(brandId)) {
				brands.add(brandId);
			}
		}
		return brands;
	}
}
