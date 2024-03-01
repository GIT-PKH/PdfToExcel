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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unchecked")
public class VatIdTest {

	private static final String SETTING_FILE_PATH = "../config/hong_test2.json";

	public static void main(String[] args) {

		String contentLine = "GmbH • Weldenstraße 1a • 85356 Freising Kaffee.de GmbH · Weldenstraße 1a, D-85356 Freising · HRB 204636, München · Geschäftsführer: Andreas Goclik, Oliver Pflüger Telefon: +49 8161 976259-10 · Fax: +49 8161 976259-19 · E-Mail: info@kaffee.de · Internet: www.kaffee.de Allgemeine Geschäftsbedingungen: www.kaffee.de/agb · Steuer-Nr.: 115/134/60076 · Ust-Id-Nr.: DE815419765 · DE-ÖKO-037 Bank: SPK Freising, BLZ: 700 510 03 · Kontonummer: 25457045 · SWIFT-BIC: BYLADEM1FSI · IBAN: DE85 7005 1003 0025 4570 45 BM & T GmbH GSP26478 Herr BM & T GmbH PARK HYUNG SEO In der Au 17 (GSP26478) 61440 Oberursel Auftragsbestätigung der Kaffee.de GmbH Kundennummer: C-202229007 Auftragsbestätigungsnummer: 20240215005 Datum: 15.02.2024 Sehr geehrte/r Herr PARK HYUNG SEO, nochmals herzlichen Dank für Ihren Auftrag! Anbei darf ich Ihnen die Auftragsbestätigung zu Ihrer Bestellung übermitteln. Ihre Lieferung wird, sofern nicht abweichend vermerkt, unser Lager umgehend verlassen. Pos Anzahl Preis Einheit Beschreibung Netto 2140 # Mahlkönig Bohnenbehälter / Mini Hopper / Trichter für  EK43/S 250 g Mini-Trichter für EK43 und EK43S, ca. 250 g Fassungsvermögen 1 1 102,05 € Stück 102,05 € 9000 # Verpackung und Versand (Nebenleistung mit 19% MwSt.) per Paketdienst (DHL) Gesamtgewicht brutto: 1,00 kg Gesamtgewicht netto: 1,00 kg Gesamtvolumen: 0,0034 m³ Anzahl Pakete: 1 (Schätzung) Die Daten entsprechen den im System der Kaffee.de GmbH gespeicherten Artikel-Eigenschaften und können von den Versanddaten abweichen. 2 1 3,85 € Paket 3,85 € Seite 1 von 2 Zwischensumme netto: 105,90 € 19% MwSt. 19 % (105,90 €): 20,12 € Gesamt brutto: 126,02 € Für Rückfragen stehen wir Ihnen gern jederzeit zur Verfügung; per E-Mail erreichen Sie uns unter info@kaffee.de. Alle Preise verstehen sich, sofern nicht anders ausgewiesen, inkl. Kaffeesteuer und zzgl. Verpackung sowie Versand. Die Kaffeesteuer wird an das Hauptzollamt Landshut entrichtet. Die Kaffee.de GmbH bietet die Möglichkeit, Rechnungen per SEPA-Lastschrifteinzug begleichen zu lassen. Ihre Zustimmung zum SEPA-Lastschriftverfahren benötigen wir in Schriftform - bitte kontaktieren Sie uns dazu! Seite 2 von 2";

		VatIdTest vatIdTest = new VatIdTest();
		Map<String, Object> jsonMap = vatIdTest.getSetting2();
		Map<String, Object> typeMap = (Map<String, Object>) jsonMap.get("type");
		String vatId = vatIdTest.getVatId(typeMap, contentLine);

		System.out.println("ddddddd : " + vatId);

		if (!vatId.equals(StringUtils.EMPTY)) {

			System.out.println(typeMap.get(vatId).getClass());

			if (typeMap.get(vatId) instanceof JSONObject) {

				Map<String, Object> brandMap = (Map<String, Object>) typeMap.get(vatId);

				String brandId = StringUtils.EMPTY;
				if (brandMap.containsKey("TYPE_HONG_01")) {

					Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get("TYPE_HONG_01");

					List<Map.Entry<String, Object>> vatIdList = (List<Entry<String, Object>>) typeHongMap.entrySet().stream()
							.collect(Collectors.toList());

					for (Map.Entry<String, Object> entry : vatIdList) {

						String tmpVatId = entry.getKey();
						//System.out.println(tmpVatId);
						Pattern pattern = Pattern.compile(tmpVatId);
						Matcher matcher = pattern.matcher(contentLine);

						if (matcher.find()) {
							brandId = (String) typeHongMap.get(tmpVatId);
							break;
						}
					}

					//System.out.println("TYPE_HONG_01");

				} else if (brandMap.containsKey("TYPE_HONG_02")) {

					Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get("TYPE_HONG_02");
					brandId = (String) typeHongMap.get("BRAND");

					//System.out.println("TYPE_HONG_02");

				} else if (brandMap.containsKey("TYPE_HONG_03")) {

					//System.out.println("TYPE_HONG_03");
				}

				System.out.println("brandId : " + brandId);

			} else {

			}
		}
	}

	// VAT 번호 추출
	public String getVatId(Map<String, Object> typeMap, String contentLine) {
		String vatId = StringUtils.EMPTY;

		List<Map.Entry<String, Object>> vatIdList = (List<Entry<String, Object>>) typeMap.entrySet().stream()
				.collect(Collectors.toList());

		for (Map.Entry<String, Object> entry : vatIdList) {
			String tmpVatId = entry.getKey();
			Pattern pattern = Pattern.compile(tmpVatId);
			Matcher matcher = pattern.matcher(contentLine);

			if (matcher.find()) {
				vatId = tmpVatId;
				break;
			}
		}

		if (StringUtils.isBlank(vatId)) {
			Pattern pattern = Pattern.compile("DE[0-9]{9}|DE [0-9]{9}");
			Matcher matcher = pattern.matcher(contentLine.replaceAll(" ", ""));
			if (matcher.find()) {
				vatId = matcher.group();
			}
		}

		if (StringUtils.isBlank(vatId)) {
			for (Map.Entry<String, Object> entry : vatIdList) {
				String tmpVatId = entry.getKey();
				if (typeMap.get(tmpVatId) instanceof JSONObject) {
					Map<String, Object> brandMap = (Map<String, Object>) typeMap.get(tmpVatId);
					if (brandMap.containsKey("TYPE_HONG_02")) {
						Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get("TYPE_HONG_02");
						List<String> keywordList = (List<String>) typeHongMap.get("KEYWORD");
						int wordIdx = 0;
						for (String keyword : keywordList) {
							Pattern pattern = Pattern.compile(keyword);
							Matcher matcher = pattern.matcher(contentLine);
							if (matcher.find()) {
								wordIdx++;
							}
							if (keywordList.size() == wordIdx) {
								vatId = tmpVatId;
							}
						}
					}
				}
			}
		}

		return vatId;
	}

	public Map<String, Object> getSetting2() {

		Map<String, Object> jsonMap = null;

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser = new JSONParser();
			jsonMap = (Map<String, Object>) jsonParser.parse(reader);

			//System.out.println(Collections.singletonList(map));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonMap;
	}

}
