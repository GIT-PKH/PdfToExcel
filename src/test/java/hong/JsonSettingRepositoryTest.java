package hong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * JsonSettingRepository 단위 테스트 (로드/조회/진단/저장)
 */
class JsonSettingRepositoryTest {

	// 테스트용 임시 디렉터리 (JUnit5 주입)
	@TempDir
	Path tempDir;

	// 테스트 대상 설정 파일 경로
	private String settingPath;

	// 중복 키(DE222...)·미사용 업체(VendorD) 포함 픽스처
	private static final String FIXTURE = """
			{
				"type": {
					"DE111111111": "VendorA",
					"DE222222222": "VendorA",
					"DE222222222": "VendorB",
					"DE 333 333 333": "VendorC",
					"DE444444444": {"TYPE_HONG_01": {"keyfoo": "VendorA", "keybar": "VendorC"}}
				},
				"header": ["파일명", "구매처"],
				"property": {
					"VendorA": {"item": [], "option": {}},
					"VendorB": {"item": [], "option": {}},
					"VendorC": {"item": [], "option": {}},
					"VendorD": {"item": [], "option": {}}
				}
			}
			""";

	@BeforeEach
	void setUp() throws IOException {
		File file = tempDir.resolve("hong.json").toFile();
		Files.write(file.toPath(), FIXTURE.getBytes(StandardCharsets.UTF_8));
		settingPath = file.getAbsolutePath();
	}

	@Test
	void 업체목록을_조회한다() {
		JsonSettingRepository repository = new JsonSettingRepository(settingPath);
		List<String> vendors = repository.listVendors();
		assertEquals(List.of("VendorA", "VendorB", "VendorC", "VendorD"), vendors);
	}

	@Test
	void 매핑목록을_조회하고_유형을_구분한다() {
		JsonSettingRepository repository = new JsonSettingRepository(settingPath);
		List<VatMapping> mappings = repository.listVatMappings();

		// 중복 DE222222222 는 마지막(VendorB)만 남아 4건
		assertEquals(4, mappings.size());

		VatMapping simple = find(mappings, "DE222222222");
		assertEquals(MappingType.SIMPLE, simple.getMappingType());
		assertEquals("VendorB", simple.getBrandId());

		VatMapping branch = find(mappings, "DE444444444");
		assertEquals(MappingType.TYPE_HONG_01, branch.getMappingType());
		assertEquals(2, branch.getBranchEntries().size());
		assertEquals("VendorC", branch.getBranchEntries().get("keybar"));
	}

	@Test
	void 공백이_다른_세금번호는_별개로_취급한다() {
		JsonSettingRepository repository = new JsonSettingRepository(settingPath);
		List<VatMapping> mappings = repository.listVatMappings();
		assertNotNull(find(mappings, "DE 333 333 333"));
	}

	@Test
	void 진단이_중복과_미사용업체를_보고한다() {
		JsonSettingRepository repository = new JsonSettingRepository(settingPath);
		List<String> issues = repository.diagnose();

		assertTrue(issues.stream().anyMatch(s -> s.contains("DE222222222")), "중복 보고 필요");
		assertTrue(issues.stream().anyMatch(s -> s.contains("VendorD")), "미사용 업체 보고 필요");
	}

	@Test
	void 저장하면_변경이_유지되고_백업이_생성된다() {
		JsonSettingRepository repository = new JsonSettingRepository(settingPath);
		List<VatMapping> mappings = repository.listVatMappings();

		// 신규 매핑 추가 (미사용이던 VendorD 참조)
		VatMapping added = new VatMapping();
		added.setVatKey("DE555555555");
		added.setMappingType(MappingType.SIMPLE);
		added.setBrandId("VendorD");
		mappings.add(added);

		repository.saveVatMappings(mappings);

		// 새 인스턴스로 재로딩하여 영속성 확인
		JsonSettingRepository reloaded = new JsonSettingRepository(settingPath);
		assertNotNull(find(reloaded.listVatMappings(), "DE555555555"));
		// property(업체) 보존
		assertEquals(4, reloaded.listVendors().size());
		// VendorD 가 더 이상 미사용이 아님
		assertFalse(reloaded.diagnose().stream().anyMatch(s -> s.contains("VendorD")));

		// 백업 파일 존재
		File[] backups = tempDir.toFile().listFiles((dir, name) -> name.contains(".bak_"));
		assertNotNull(backups);
		assertTrue(backups.length >= 1, "백업 파일이 생성되어야 함");
	}

	// 헬퍼: vatKey 로 매핑 찾기
	private VatMapping find(List<VatMapping> mappings, String vatKey) {
		return mappings.stream().filter(m -> vatKey.equals(m.getVatKey())).findFirst().orElse(null);
	}
}
