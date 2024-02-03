package hong;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

	//	private static final String EXCEL_FILE_NM = "HONG_" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".xlsx";
	//
	//	private static final String PDF_FOLDER_PATH = "./config/pdf/";
	//
	//	private static final String SETTING_FILE_PATH = "./config/setting.json";

	private static final String EXCEL_FILE_NM = "HONG.xlsx";

	private static final String PDF_FOLDER_PATH = "../../";

	private static final String SETTING_FILE_PATH = "../setting.json";

	public static void main(String[] args) {

		//System.out.println("########## 변환시작 ..");

		//File path = new File(PDF_FOLDER_PATH);

		// System.out.println("############ : " + path.getAbsolutePath());
		// System.out.println("############ : " + path.getCanonicalPath());

		File[] files = getPdfFiles();

		// TODO : 파일이 없을 경우 종료

		int index = 0;

		for (int k = 0; k < files.length; k++) {

			System.out.println("########## [" + files[k].getName() + "] 변환시작 ..");

			try (PDDocument document = PDDocument.load(files[k]);) {

				String content = new PDFTextStripper().getText(document);

				String contentLine = content.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", " ");

				Pattern pattern = Pattern.compile("DE[0-9]{9}|DE [0-9]{9}");
				Matcher matcher = pattern.matcher(content);

				String vatId = StringUtils.EMPTY;

				while (matcher.find()) {

					vatId = matcher.group();
				}

				if (StringUtils.isBlank(vatId)) {

					vatId = "DE 17 594 4429";
				}

				//System.out.println("# 파일경로 : " + files[k] + ", vatId : " + vatId);

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

				if (attrMap == null) {
					System.out.println("# [ERROR] 설정정보를 불러 수 없습니다..");
					return;
				}

				String typeCd = (String) attrMap.get("typeCd");
				JSONArray attrArray = (JSONArray) attrMap.get("propertyCd");
				JSONObject options = (JSONObject) attrMap.get("optionCd");

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
				titleList.add(titleList.size(), "All Text");
				titleList.add(titleList.size(), "All Text Line");

				// excel add
				excelData.put(String.valueOf(index++), titleList.toArray());

				// System.out.println("# titleList : " + titleList);

				List<String> extraList = new ArrayList<String>();
				extraList.add(files[k].getName());
				extraList.add(typeCd);

				// 추출항목 for (12개)
				for (int p = 0; p < attrArray.size(); p++) {

					String attrKey = (String) attrArray.get(p);

					String rslt = StringUtils.EMPTY;

					//System.out.println("# attrKey : " + attrKey);

					// 추출항목 "" 처리
					if (StringUtils.isNotBlank(attrKey)) {

						JSONArray optionArray = null;

						String optionType = "01";

						if (options != null && options.get(attrKey) != null) {

							optionArray = (JSONArray) options.get(attrKey);

							optionType = (String) optionArray.get(0);
						}

						if ("01".equals(optionType)) {

							for (String text : content.split("\\n")) {

								if (text.trim().indexOf(attrKey) != -1) {

									rslt = text.substring(text.indexOf(attrKey) + attrKey.length(), text.length());

									break;
								}
							}

						} else if ("02".equals(optionType)) {

							// TODO

						} else if ("03".equals(optionType)) {

							String startStr = (String) optionArray.get(1);
							String endStr = (String) optionArray.get(2);

							int startIndex = contentLine.indexOf(startStr)
									+ (optionArray.get(3).toString().equals("F") ? startStr.length() : 0);

							int endIndex = contentLine.indexOf(endStr, startIndex)
									+ (optionArray.get(4).toString().equals("F") ? 0 : endStr.length());

							rslt = contentLine.substring(startIndex, endIndex);
						}
					}

					rslt = StringUtils.replaceEach(rslt, new String[] { ":", "€" }, new String[] { "", "" }).trim();

					//System.out.println("# [" + (p + 1) + "] 추출항목 : " + attrKey + " ,추출결과 : " + rslt);

					if (StringUtils.isNoneBlank(attrKey)) {

						System.out.println("# [추출항목] : " + attrKey + ", [추출결과] : " + rslt);
					}

					extraList.add(rslt);

				}

				extraList.add(content);
				extraList.add(contentLine);

				// System.out.println("## extraList.toArray() : " + extraList);

				// excel add
				excelData.put(String.valueOf(index++), extraList.toArray());

				//System.out.println("########## [" + files[k].getName() + "] 변환완료 ..");

				document.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 엑셀 생성
		setExcelMake();

		//System.out.println(stringBuilder.toString());

		//System.out.println("########## 종료 ..");
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

					//System.out.println(Collections.singletonList(map));

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
