package hong;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * hong.json 기반 설정 저장소 구현
 * (읽기는 관대한 파서로 현재 파일을 수용하고, 쓰기는 표준 JSON으로 정규화 + 백업 + 원자적 교체)
 */
@Slf4j
public class JsonSettingRepository implements SettingRepository {

	// 설정 파일 경로
	private final String settingFilePath;

	// 루트 구조 (type/header/property 순서 유지)
	private Map<String, Object> root;

	// type 블록 (중복 키 추적 가능)
	private DuplicateTrackingMap typeMap;

	// property 블록 (업체 규칙)
	private Map<String, Object> propertyMap;

	// 생성자: 즉시 로드
	public JsonSettingRepository(String settingFilePath) {
		this.settingFilePath = settingFilePath;
		load();
	}

	// 파일 로드 (json-simple 관대한 파서 + 순서/중복 보존)
	@SuppressWarnings("unchecked")
	private void load() {
		try (Reader reader = new InputStreamReader(new FileInputStream(settingFilePath), StandardCharsets.UTF_8)) {

			JSONParser parser = new JSONParser();
			ContainerFactory factory = new ContainerFactory() {
				@Override
				public Map createObjectContainer() {
					return new DuplicateTrackingMap();
				}

				@Override
				public List creatArrayContainer() {
					return new ArrayList<>();
				}
			};

			root = (Map<String, Object>) parser.parse(reader, factory);
			typeMap = (DuplicateTrackingMap) root.get(Constants.JSON_KEY_TYPE);
			propertyMap = (Map<String, Object>) root.get(Constants.JSON_KEY_PROPERTY);

		} catch (Exception e) {
			log.error("설정 파일 로드 오류: {}", settingFilePath, e);
			throw new IllegalStateException("설정 파일을 불러올 수 없습니다: " + settingFilePath, e);
		}
	}

	// 업체 목록 (property 키)
	@Override
	public List<String> listVendors() {
		return new ArrayList<>(propertyMap.keySet());
	}

	// 세금번호 매핑 목록
	@Override
	public List<VatMapping> listVatMappings() {
		List<VatMapping> result = new ArrayList<>();
		for (Map.Entry<String, Object> entry : typeMap.entrySet()) {
			result.add(toVatMapping(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	// JSON 값 -> VatMapping 변환
	@SuppressWarnings("unchecked")
	private VatMapping toVatMapping(String vatKey, Object value) {
		VatMapping mapping = new VatMapping();
		mapping.setVatKey(vatKey);

		if (value instanceof Map) {
			Map<String, Object> object = (Map<String, Object>) value;

			if (object.containsKey(Constants.TYPE_HONG_01)) {
				mapping.setMappingType(MappingType.TYPE_HONG_01);
				Map<String, Object> branch = (Map<String, Object>) object.get(Constants.TYPE_HONG_01);
				Map<String, String> entries = new LinkedHashMap<>();
				for (Map.Entry<String, Object> entry : branch.entrySet()) {
					entries.put(entry.getKey(), String.valueOf(entry.getValue()));
				}
				mapping.setBranchEntries(entries);

			} else if (object.containsKey(Constants.TYPE_HONG_02)) {
				mapping.setMappingType(MappingType.TYPE_HONG_02);
				Map<String, Object> branch = (Map<String, Object>) object.get(Constants.TYPE_HONG_02);
				mapping.setBrandId(String.valueOf(branch.get(Constants.JSON_KEY_BRAND)));
				Object keywordValue = branch.get(Constants.JSON_KEY_KEYWORD);
				if (keywordValue instanceof List) {
					List<String> keywords = new ArrayList<>();
					for (Object keyword : (List<Object>) keywordValue) {
						keywords.add(String.valueOf(keyword));
					}
					mapping.setKeywords(keywords);
				}
			}
		} else {
			mapping.setMappingType(MappingType.SIMPLE);
			mapping.setBrandId(String.valueOf(value));
		}
		return mapping;
	}

	// 데이터 진단
	@Override
	public List<String> diagnose() {
		List<String> issues = new ArrayList<>();

		// 1) 중복 세금번호
		for (String duplicatedKey : typeMap.getDuplicateKeys()) {
			issues.add("중복 세금번호: " + duplicatedKey + " (마지막 정의만 적용됨)");
		}

		// 2) 참조 무결성 / 미사용 업체
		List<String> vendors = listVendors();
		Set<String> referenced = new LinkedHashSet<>();
		for (VatMapping mapping : listVatMappings()) {
			referenced.addAll(mapping.referencedBrands());
		}
		for (String brand : referenced) {
			if (!vendors.contains(brand)) {
				issues.add("참조 오류: '" + brand + "' 업체 규칙이 없습니다.");
			}
		}
		for (String vendor : vendors) {
			if (!referenced.contains(vendor)) {
				issues.add("미사용 업체: '" + vendor + "'");
			}
		}
		return issues;
	}

	// 매핑 목록 저장
	@Override
	public void saveVatMappings(List<VatMapping> mappings) {

		// 중복 세금번호 방지 (정확 일치)
		Set<String> seenKeys = new HashSet<>();
		for (VatMapping mapping : mappings) {
			if (!seenKeys.add(mapping.getVatKey())) {
				throw new IllegalArgumentException("중복 세금번호는 저장할 수 없습니다: " + mapping.getVatKey());
			}
		}

		// type 블록 재구성 (순서 유지)
		Map<String, Object> typeOut = new LinkedHashMap<>();
		for (VatMapping mapping : mappings) {
			typeOut.put(mapping.getVatKey(), toJsonValue(mapping));
		}
		root.put(Constants.JSON_KEY_TYPE, typeOut);

		writeRoot();
		// 내부 상태 재로딩
		load();
	}

	// VatMapping -> JSON 값 변환
	private Object toJsonValue(VatMapping mapping) {
		if (mapping.getMappingType() == MappingType.TYPE_HONG_01) {
			Map<String, Object> wrapper = new LinkedHashMap<>();
			wrapper.put(Constants.TYPE_HONG_01, new LinkedHashMap<>(mapping.getBranchEntries()));
			return wrapper;
		}
		if (mapping.getMappingType() == MappingType.TYPE_HONG_02) {
			Map<String, Object> branch = new LinkedHashMap<>();
			branch.put(Constants.JSON_KEY_BRAND, mapping.getBrandId());
			branch.put(Constants.JSON_KEY_KEYWORD, new ArrayList<>(mapping.getKeywords()));
			Map<String, Object> wrapper = new LinkedHashMap<>();
			wrapper.put(Constants.TYPE_HONG_02, branch);
			return wrapper;
		}
		return mapping.getBrandId();
	}

	// 루트 구조를 표준 JSON으로 안전하게 기록 (백업 -> 임시작성 -> 검증 -> 원자교체)
	private void writeRoot() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
			DefaultIndenter indenter = new DefaultIndenter("\t", "\n");
			prettyPrinter.indentObjectsWith(indenter);
			prettyPrinter.indentArraysWith(indenter);
			String json = mapper.writer(prettyPrinter).writeValueAsString(root);

			File target = new File(settingFilePath);
			File parent = target.getParentFile();

			// 백업
			if (target.isFile()) {
				String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
				File backup = new File(parent, target.getName() + ".bak_" + stamp);
				Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			// 임시파일 작성
			File temp = new File(parent, target.getName() + ".tmp");
			Files.write(temp.toPath(), json.getBytes(StandardCharsets.UTF_8));

			// 검증 (재파싱 성공해야 함)
			try (Reader reader = new InputStreamReader(new FileInputStream(temp), StandardCharsets.UTF_8)) {
				new JSONParser().parse(reader);
			}

			// 원자적 교체
			Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);

		} catch (Exception e) {
			log.error("설정 파일 저장 오류: {}", settingFilePath, e);
			throw new IllegalStateException("설정 파일을 저장할 수 없습니다.", e);
		}
	}

	/**
	 * 중복 키를 추적하는 LinkedHashMap (순서 유지 + 중복 세금번호 검출용)
	 */
	private static class DuplicateTrackingMap extends LinkedHashMap<String, Object> {

		private static final long serialVersionUID = 1L;

		// 중복으로 들어온 키 목록
		private final List<String> duplicateKeys = new ArrayList<>();

		@Override
		public Object put(String key, Object value) {
			if (containsKey(key)) {
				duplicateKeys.add(key);
			}
			return super.put(key, value);
		}

		// 중복 키 목록 반환
		public List<String> getDuplicateKeys() {
			return duplicateKeys;
		}
	}
}
