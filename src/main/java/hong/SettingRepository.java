package hong;

import java.util.List;

/**
 * 설정(hong.json) 저장소 추상화
 * (현재 구현은 JSON 파일, 추후 SQLite 등으로 무중단 교체 가능)
 */
public interface SettingRepository {

	// 세금번호 매핑 목록 조회 (type 블록)
	List<VatMapping> listVatMappings();

	// 업체(brand) 목록 조회 (property 키, 참조 무결성/콤보용)
	List<String> listVendors();

	// 데이터 진단 (중복 세금번호/참조오류/미사용 업체)
	List<String> diagnose();

	// 세금번호 매핑 목록 저장 (백업 + 원자적 교체)
	void saveVatMappings(List<VatMapping> mappings);
}
