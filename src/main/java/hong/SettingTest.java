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

public class SettingTest {

	private static final String SETTING_FILE_PATH = "./config/hong.json";

	public static void main(String[] args) {

		String vatId = "DE116475353";

		SettingTest settingTest = new SettingTest();
		settingTest.getSetting(vatId);

	}

	public Map<String, Object> getSetting(String vatId) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);

			JSONObject typeObject = (JSONObject) jsonRoot.get("type");
			String typeCd = typeObject.get(vatId).toString();
			map.put("typeCd", typeCd);

			JSONObject object = (JSONObject) jsonRoot.get("property");
			JSONObject vatObject = (JSONObject) object.get(typeCd);

			JSONArray itemArray = (JSONArray) vatObject.get("item");
			map.put("itemCd", itemArray);

			JSONObject optionObject = (JSONObject) vatObject.get("option");

			// 옵션추가
			for (int k = 0; k < itemArray.size(); k++) {
				map.put(itemArray.get(k).toString(), optionObject.get(itemArray.get(k)));
			}

			System.out.println(Collections.singletonList(map));

		} catch (Exception e) {

			e.printStackTrace();
		}

		return map;
	}
}
