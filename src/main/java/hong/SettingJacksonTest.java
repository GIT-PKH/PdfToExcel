package hong;

import java.io.File;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

public class SettingJacksonTest {

	private static final String SETTING_FILE_PATH = "../config/hong.json";

	public static void main(String[] args) {

		String vatId = "DE116475353";

		SettingJacksonTest settingTest = new SettingJacksonTest();
		settingTest.getSetting(vatId);

	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getSetting(String vatId) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser2 = new JSONParser();
			Map<String, Object> jsonMap = (Map<String, Object>) jsonParser2.parse(reader);

			//System.out.println(Collections.singletonList(jsonRoot2));

			Map<String, Object> typeMap = (Map<String, Object>) jsonMap.get("type");

			String vatMap = (String) typeMap.get("DE116475353");

			System.out.println(vatMap);

			Map<String, Object> propertyMap = (Map<String, Object>) jsonMap.get("property");

			Map<String, Object> brandMap = (Map<String, Object>) propertyMap.get(vatMap);

			List<String> itemList = (List<String>) brandMap.get("item");
			
			System.out.println(itemList);

			for (String item : itemList) {

				List<String> optionList = (List<String>) ((Map<String, Object>) brandMap.get("option")).get(item);
				
				System.out.println(item + " = " + optionList);
			}

			

		} catch (Exception e) {

			e.printStackTrace();
		}

		return map;
	}
}
