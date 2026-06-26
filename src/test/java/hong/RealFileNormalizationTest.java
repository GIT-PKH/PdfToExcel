package hong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 실제 config/hong.json 저장 정규화 통합 테스트
 * (저장 시 표준 JSON으로 정규화되고 데이터가 보존되는지 / 파일 없으면 skip)
 */
class RealFileNormalizationTest {

	// 임시 디렉터리
	@TempDir
	Path tempDir;

	@Test
	void 실제설정파일_저장시_표준JSON으로_정규화되고_데이터가_보존된다() throws Exception {
		File real = new File("config/hong.json");
		Assumptions.assumeTrue(real.isFile(), "config/hong.json 이 없어 테스트를 건너뜀");

		// 실파일 복사본으로 작업 (원본 보호)
		File copy = tempDir.resolve("hong.json").toFile();
		Files.copy(real.toPath(), copy.toPath());

		JsonSettingRepository repository = new JsonSettingRepository(copy.getAbsolutePath());
		int vendorCount = repository.listVendors().size();
		int mappingCount = repository.listVatMappings().size();

		// 변경 없이 저장 → 표준 JSON으로 정규화
		repository.saveVatMappings(repository.listVatMappings());

		// 표준 JSON 검증: Jackson 엄격 파서로 읽혀야 함(103개 문법오류 해소 확인)
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(copy);
		assertTrue(root.has("type") && root.has("property") && root.has("header"), "구조 유지 필요");

		// 데이터 보존 검증
		JsonSettingRepository reloaded = new JsonSettingRepository(copy.getAbsolutePath());
		assertEquals(vendorCount, reloaded.listVendors().size(), "업체 수 보존");
		assertEquals(mappingCount, reloaded.listVatMappings().size(), "매핑 수 보존");
	}
}
