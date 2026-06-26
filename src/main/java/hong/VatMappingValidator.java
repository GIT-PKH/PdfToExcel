package hong;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 세금번호 매핑 검증기 (순수 로직 — 중복/형식/참조 무결성)
 */
public class VatMappingValidator {

	// 단건 검증: 오류 메시지 목록 반환(빈 목록 = 정상)
	public List<String> validate(VatMapping target, List<VatMapping> existingMappings, List<String> vendorList) {

		List<String> errors = new ArrayList<>();

		// 1) 세금번호 필수
		if (StringUtils.isBlank(target.getVatKey())) {
			errors.add("세금번호를 입력하세요.");
			return errors;
		}

		// 2) 중복 금지 (정확 일치, 공백 정규화 없음 / 자기 자신 제외)
		for (VatMapping existing : existingMappings) {
			if (existing == target) {
				continue;
			}
			if (StringUtils.equals(existing.getVatKey(), target.getVatKey())) {
				errors.add("이미 등록된 세금번호입니다: " + target.getVatKey());
				break;
			}
		}

		// 3) 참조 무결성: 참조하는 업체가 설정에 존재해야 함
		for (String brand : target.referencedBrands()) {
			if (!vendorList.contains(brand)) {
				errors.add("설정에 없는 업체입니다: " + brand);
			}
		}

		// 4) 키워드 분기인데 항목이 비어있으면 경고
		if (target.getMappingType() == MappingType.TYPE_HONG_01 && target.getBranchEntries().isEmpty()) {
			errors.add("키워드 분기 항목을 1개 이상 입력하세요.");
		}

		return errors;
	}
}
