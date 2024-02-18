package hong;

import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VatIdTest {

	private static final String SETTING_FILE_PATH = "../config/hong.json";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JSONObject typeObject = null;

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);

			typeObject = (JSONObject) jsonRoot.get("type");

		} catch (Exception e) {
			e.printStackTrace();
		}

		String content = "A. INVOICE invoice no.: DE038046578 date: 30.01.24 payment method: mastercard shipping method: DHL standard billing address shipping address first name: BM & T GmbH first name: BM & T GmbH last name: LEEJONGSEO last name: LEEJONGSEO address line: In der Au 17 GSP39449 address line: In der Au 17 GSP39449 c/o company: BM & T GmbH c/o company: BM & T GmbH postcode: 61440 postcode: 61440 city: Oberursel city: Oberursel country: Germany country: Germany phone: 061719893156 phone: 061719893156 email: daniel8256@naver.com items ordered item no. item unit price quantity subtotal P00828141 C.P. Company knits  € 140,00 1 € 140,00 size: 50 SALE € 250,00 shipment: € 5,95 GRAND TOTAL: € 145,95 incl. VAT 19 %: € 23,30 net total: € 122,65 paid with  mastercard: € 145,95 mytheresa.com GmbH Contact Phone +49 89 127695 -100 Fax +49 89 127695-200 Email customercare@mytheresa.com HypoVereinsbank München  ·  Kto.: 666866142  ·  BLZ: 70020270  ·  IBAN: DE85700202700666866142  ·  SWIFT-BIC: HYVEDEMM  Registered of ce: Munich, Germany  ·  Commercial registry: Munich HRB 135 658  ·  VAT registration number: DE213277271  Managing Directors Michael Kliger, Dr. Martin Beer, Sebastian Dietzmann, Gareth Locke, Isabel May  Office Einsteinring 9 85609 Aschheim/München Germany *DE038046578* ";

		List<Map.Entry<String, Object>> vatIdList = (List<Entry<String, Object>>) typeObject.entrySet().stream().collect(Collectors.toList());

		for (java.util.Map.Entry<String, Object> entry : vatIdList) {
			Pattern pattern = Pattern.compile(entry.getKey());
			Matcher matcher = pattern.matcher(content);

			System.out.println(matcher.find());
		}

		//
		//		String vatIdNames[] = { "VAT registration number" };
		//
		//		for (String vatIdName : vatIdNames) {
		//			Pattern pattern = Pattern.compile(vatIdName);
		//			Matcher matcher = pattern.matcher(content);
		//
		//			System.out.println(matcher.find());
		//
		//		}
		//
		//		content = content.replaceAll(" ", "");

		//		
		//		
		//		
		//		
		//		
		//		
		//		
		//		
		//		
		//		
		//		String vatId = StringUtils.EMPTY;
		//		Pattern pattern = Pattern.compile("DE[0-9]{9}");
		//		Matcher matcher = pattern.matcher(content);
		//
		//		while(matcher.find()) {
		//			vatId = matcher.group();
		//			System.out.println("## vatId : " + vatId);
		//		}
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
