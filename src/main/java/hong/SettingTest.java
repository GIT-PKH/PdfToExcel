package hong;

import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unchecked")
public class SettingTest {

	private static final String SETTING_FILE_PATH = "../config/hong_test2.json";

	public static void main(String[] args) {

		String vatId = "DE339819462";
		//String vatId = "DE116475353";

		SettingTest settingTest = new SettingTest();
		Map<String, Object> map = settingTest.getSetting(vatId);
		
		System.out.println(Collections.singletonList(map));

	}

	public Map<String, Object> getSetting(String vatId) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser = new JSONParser();
			Map<String, Object> jsonRoot = (Map<String, Object>) jsonParser.parse(reader);

			List<String> headerArray = (List<String>) jsonRoot.get("header");
			map.put("headerCd", headerArray);

			Map<String, Object> typeObject = (Map<String, Object>) jsonRoot.get("type");
			if (typeObject.get(vatId) == null) {
				return map;
			}
			String typeCd = typeObject.get(vatId).toString();
			map.put("typeCd", typeCd);

			Map<String, Object> object = (Map<String, Object>) jsonRoot.get("property");
			Map<String, Object> vatObject = (Map<String, Object>) object.get(typeCd);

			List<String> itemArray = (List<String>) vatObject.get("item");
			map.put("itemCd", itemArray);

			Map<String, Object> optionObject = (Map<String, Object>) vatObject.get("option");

			// 옵션추가
			for (int k = 0; k < itemArray.size(); k++) {
				map.put(itemArray.get(k).toString(), optionObject.get(itemArray.get(k)));
			}
			//System.out.println(Collections.singletonList(map));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
