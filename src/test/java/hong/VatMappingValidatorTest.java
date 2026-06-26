package hong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * VatMappingValidator 단위 테스트 (중복/형식/참조 무결성)
 */
class VatMappingValidatorTest {

	// 검증기
	private final VatMappingValidator validator = new VatMappingValidator();

	// 기존 매핑 / 업체 목록
	private List<VatMapping> existing;
	private List<String> vendors;

	@BeforeEach
	void setUp() {
		vendors = List.of("VendorA", "VendorB");
		existing = new ArrayList<>();
		existing.add(simple("DE111111111", "VendorA"));
	}

	@Test
	void 정상_매핑은_오류가_없다() {
		List<String> errors = validator.validate(simple("DE222222222", "VendorB"), existing, vendors);
		assertTrue(errors.isEmpty());
	}

	@Test
	void 세금번호가_비면_오류() {
		List<String> errors = validator.validate(simple("", "VendorA"), existing, vendors);
		assertFalse(errors.isEmpty());
	}

	@Test
	void 중복_세금번호는_오류() {
		List<String> errors = validator.validate(simple("DE111111111", "VendorA"), existing, vendors);
		assertTrue(errors.stream().anyMatch(s -> s.contains("이미 등록")));
	}

	@Test
	void 공백이_다르면_중복이_아니다() {
		// "DE 111 111 111" 은 "DE111111111" 과 다른 키 (정규화 금지)
		List<String> errors = validator.validate(simple("DE 111 111 111", "VendorA"), existing, vendors);
		assertFalse(errors.stream().anyMatch(s -> s.contains("이미 등록")));
	}

	@Test
	void 없는_업체를_참조하면_오류() {
		List<String> errors = validator.validate(simple("DE222222222", "NotExist"), existing, vendors);
		assertTrue(errors.stream().anyMatch(s -> s.contains("없는 업체")));
	}

	// 헬퍼: 단순 매핑 생성
	private VatMapping simple(String vatKey, String brandId) {
		VatMapping mapping = new VatMapping();
		mapping.setVatKey(vatKey);
		mapping.setMappingType(MappingType.SIMPLE);
		mapping.setBrandId(brandId);
		return mapping;
	}
}
