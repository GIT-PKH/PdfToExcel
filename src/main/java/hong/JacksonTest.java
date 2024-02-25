package hong;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

public class JacksonTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		ObjectMapper objectMapper = new ObjectMapper();

		MapType mapType = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);

		LinkedHashMap<String, Object> jsonMap = objectMapper.readValue(getJsonStr(), mapType);

		// 읽어온 데이터 출력 (키 순서 유지)
		for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}

		LinkedHashMap<String, Object> typeObje = (LinkedHashMap<String, Object>) jsonMap.get("type");

		System.out.println(typeObje.get("DE116475353"));

	}

	public static String getJsonStr() {
		// @formatter:off
		return  "{\n" +
                "    \"type\": {\n" +
                "        \"DE116475353\": \"1ANEUWARE\",\n" +
                "        \"DE 276366762\": \"43EINHALB\",\n" +
                "        \"DE339819462\": \"ABOUT YOU\",\n" +
                "        \"DE345254508\": \"ABU Europe\",\n" +
                "        \"DE129469082\": \"ACNESTUDIOS\",\n" +
                "        \"DE 132490588\": \"ADIDAS\"\n" +
                "    },\n" +
                "    \"header\": [\n" +
                "        \"파일명\",\n" +
                "        \"구매처\",\n" +
                "        \"세금번호\",\n" +
                "        \"인보이스 발행날짜\",\n" +
                "        \"인보이스 번호\",\n" +
                "        \"주문번호\",\n" +
                "        \"총금액\",\n" +
                "        \"부가세 제외\",\n" +
                "        \"부가세1\",\n" +
                "        \"부가세2\",\n" +
                "        \"All ADD\",\n" +
                "        \"All Text1\",\n" +
                "        \"All Text2\"\n" +
                "    ],\n" +
                "    \"property\": {\n" +
                "        \"1ANEUWARE\": {\n" +
                "            \"item\": [\n" +
                "                \"Ust-Nr\",\n" +
                "                \"Ihre Zahlung ist\",\n" +
                "                \"Rechnung\",\n" +
                "                \"Rechnung Nr.\",\n" +
                "                \"Gesamt-Brutto\",\n" +
                "                \"Gesamt ohne MwSt.\",\n" +
                "                \"Mwst. Betrag\",\n" +
                "                \"\",\n" +
                "                \"ADDRESS\"\n" +
                "            ],\n" +
                "            \"option\": {\n" +
                "                \"Ust-Nr\": [\"PDF\", \"03\", \"Ust-Nr:\", \"/ FR72890605520\", [\"Ust-Nr:\", \"/ FR72890605520\"], \"\"],\n" +
                "                \"Ihre Zahlung ist\": [\"PDF\", \"03\", \"Ihre Zahlung ist am\", \"bei uns eingegangen\", [\"Ihre Zahlung ist am\", \"bei uns eingegangen\"], \"\"],\n" +
                "                \"Rechnung\": [\"EXCEL\", [\"16/C\"], [\"Rechnung Nr.\", \":\"], \"\"],\n" +
                "                \"Rechnung Nr.\": [\"EXCEL\", [\"16/C\"], [\"Rechnung Nr.\", \":\"], \"\"],\n" +
                "                \"Gesamt-Brutto\": [\"PDF\", \"01\", [\"EUR\"], \"\"],\n" +
                "                \"Gesamt ohne MwSt.\": [\"PDF\", \"01\", [\"EUR\"], \"\"],\n" +
                "                \"Mwst. Betrag\": [\"PDF\", \"01\", [\"19%\", \"EUR\", \":\", \" \", \"?\"], \"\"],\n" +
                "                \"ADDRESS\": [\"PDF\", \"05\", [\"9\", \"10\"], [], \"\"]\n" +
                "            }\n" +
                "        },\n" +
                "        \"43EINHALB\": {\n" +
                "            \"item\": [\n" +
                "                \"USt.-ID:\",\n" +
                "                \"Invoice date\",\n" +
                "                \"Invoice-No.\",\n" +
                "                \"Order-No.\",\n" +
                "                \"Total gross (Gesamt Brutto)\",\n" +
                "                \"Total without VAT (Gesamt Netto)\",\n" +
                "                \"Tax Amount (MwSt.): 19,00%\",\n" +
                "                \"\",\n" +
                "                \"ADDRESS\"\n" +
                "            ],\n" +
                "            \"option\": {\n" +
                "                \"USt.-ID:\": [\"PDF\", \"01\", [\"\"], \"\"],\n" +
                "                \"Invoice date\": [\"EXCEL\", [\"20/U\"], [\":\"], \"\"],\n" +
                "                \"Invoice-No.\": [\"EXCEL\", [\"22/U\"], [\":\"], \"\"],\n" +
                "                \"Order-No.\": [\"EXCEL\", [\"23/U\"], [\":\"], \"\"],\n" +
                "                \"Total gross (Gesamt Brutto)\": [\"PDF\", \"01\", [\":\", \"€\"], \"\"],\n" +
                "                \"Total without VAT (Gesamt Netto)\": [\"PDF\", \"01\", [\":\", \"€\"], \"\"],\n" +
                "                \"Tax Amount (MwSt.): 19,00%\": [\"PDF\", \"01\", [\"19%\", \"€\"], \"\"],\n" +
                "                \"ADDRESS\": [\"EXCEL\", [\"14/C\", \"15/C\", \"16/C\", \"17/C\"], [\"\"], \"\"]\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
		// @formatter:on
	}
}
