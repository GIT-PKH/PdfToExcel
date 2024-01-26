package hong;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PdfToExcel {

	static Map<String, Object[]> excelData = new LinkedHashMap<>();

	private static final String PDF_FOLDER_PATH = "../../";

	private static final String EXCEL_FILE_NM = "HONG.xlsx";

	private static final String SETTING_FILE_PATH = "../setting.json";

	public static void main(String[] args) {

		System.out.println("########## 변환시작 ..");

		// File path = new File(PDF_FOLDER_PATH);

		// System.out.println("############ : " + path.getAbsolutePath());
		// System.out.println("############ : " + path.getCanonicalPath());

		StringBuilder stringBuilder = new StringBuilder();

		File[] files = getPdfFiles();

		// TODO : 파일이 없을 경우 종료

		int index = 0;

		for (int k = 0; k < files.length; k++) {

			try (PDDocument document = PDDocument.load(files[k]);) {

				String content = new PDFTextStripper().getText(document);

				Pattern pattern = Pattern.compile("DE[0-9]{9}|DE [0-9]{9}");
				Matcher matcher = pattern.matcher(content);

				String vatId = StringUtils.EMPTY;

				while (matcher.find()) {
					vatId = matcher.group();
				}

				// H&M, COS
				if (vatId.equals("DE118569718")) {
					Pattern patternDuplication = Pattern.compile("Umsatzsteuer-Identifikationsnummer");
					Matcher matcherDuplication = patternDuplication.matcher(content);

					vatId = matcherDuplication.find() ? vatId + "H&M" : vatId + "COS";
				}

				// System.out.println("# vatId : " + vatId);

				// 설정정보 조회
				Map<String, Object> attrMap = getSetting(vatId);

				// TODO : null 일경우 에러

				String typeCd = (String) attrMap.get("typeCd");
				JSONArray attrArray = (JSONArray) attrMap.get("propertyCd");

				// 엑셀 제목
//				if (k == 0) {
//					Object headerNm[] = ((JSONArray) attrMap.get("headerNm")).toArray();
//					// excel add
//					excelData.put(String.valueOf(index++), headerNm);
//				}

				// 구매처별 제목
				List<String> titleList = new ArrayList<String>();
				titleList.add(0, typeCd);
				titleList.add(1, vatId);
				titleList.addAll(attrArray);

				// excel add
				excelData.put(String.valueOf(index++), titleList.toArray());

				// System.out.println("# titleList : " + titleList);

				List<String> extraList = new ArrayList<String>();
				extraList.add(files[k].getName());
				extraList.add(typeCd);

				for (int p = 0; p < attrArray.size(); p++) {

					String attrKey = (String) attrArray.get(p);

					// System.out.println("# attrKey : " + attrKey);

					for (String text : content.split("\\n")) {

						text = text.trim();

						if (text.startsWith(attrKey)) {

							if (StringUtils.isNotBlank(attrKey)) {

								String rslt = text.substring(attrKey.length(), text.length());

								rslt = StringUtils.replace(StringUtils.replace(rslt, ":", ""), "€", "").trim();

								stringBuilder.append(files[k].getName()).append(",");
								stringBuilder.append(typeCd).append(",");
								stringBuilder.append(attrKey).append(",");
								stringBuilder.append(rslt).append("\n");

								extraList.add(rslt);

							} else {

								extraList.add(StringUtils.EMPTY);
								break;
							}

						} else {
							// 문서에 존재하지 않음
						}
					}

				}

				// System.out.println("## extraList.toArray() : " + extraList);
				// excel add
				excelData.put(String.valueOf(index++), extraList.toArray());

				System.out.println("########## [" + files[k].getName() + "] 변환완료 ..");

				document.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 엑셀 생성
		setExcelMake();

		// System.out.println(stringBuilder.toString());

		System.out.println("########## 종료 ..");
	}

	public static void setExcelMake() {

		XSSFWorkbook workbook = new XSSFWorkbook();

		XSSFSheet sheet = workbook.createSheet("HONG");

		Set<String> keyset = excelData.keySet();
		int rownum = 0;

		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = excelData.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Integer) {
					cell.setCellValue((Integer) obj);
				}
			}
		}

		try (FileOutputStream out = new FileOutputStream(new File(PDF_FOLDER_PATH, EXCEL_FILE_NM));) {

			workbook.write(out);
			workbook.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Object> getSetting(String vatId) {

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			Map<String, Object> map = new HashMap<String, Object>();

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);
			JSONObject typeObject = (JSONObject) jsonRoot.get("type");

			JSONArray headerObject = (JSONArray) jsonRoot.get("header");

			String typeCd = typeObject.get(vatId).toString();

			map.put("typeCd", typeCd);

			map.put("headerNm", headerObject);

			JSONArray jsonArray = (JSONArray) jsonRoot.get("property");

			for (int j = 0; j < jsonArray.size(); j++) {

				JSONObject object = (JSONObject) jsonArray.get(j);

				if (object.get(typeCd) != null) {

					map.put("propertyCd", object.get(typeCd));

					return map;
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	public static File[] getPdfFiles() {

		File dir = new File(PDF_FOLDER_PATH);
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isFile() && file.getName().toUpperCase().endsWith("PDF")) {
					return true;
				} else {
					return false;
				}
			}
		});

		return files;
	}

}
