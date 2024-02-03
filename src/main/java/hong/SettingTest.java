package hong;

import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SettingTest {

	private static final String SETTING_FILE_PATH = "./config/setting.json";

	public static void main(String[] args) {

		String vatId = "DE 129473557";

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			Map<String, Object> map = new HashMap<String, Object>();

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);
			JSONObject typeObject = (JSONObject) jsonRoot.get("type");

			String typeCd = typeObject.get(vatId).toString();

			map.put("typeCd", typeCd);

			JSONArray headerObject = (JSONArray) jsonRoot.get("header");

			map.put("headerNm", headerObject);

			JSONArray jsonArray = (JSONArray) jsonRoot.get("property");

			for (int j = 0; j < jsonArray.size(); j++) {

				JSONObject object = (JSONObject) jsonArray.get(j);

				if (object.get(typeCd) != null) {

					map.put("propertyCd", object.get(typeCd));

					if (object.get(typeCd + "_OPTION") != null) {

						map.put("optionCd", object.get(typeCd + "_OPTION"));
					}
				}
			}

			System.out.println(Collections.singletonList(map));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
