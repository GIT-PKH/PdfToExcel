package hong;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

@SuppressWarnings("unchecked")
public class PdfConvert3 {

	private static final String REPLACE_RN = "(\\r\\n|\\r|\\n|\\n\\r)";

	static Map<String, Object[]> excelData = new LinkedHashMap<>();

	private static final String EXCEL_FILE_NM = "00.HONG_" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))
			+ ".xlsx";

	private static final String PDF_FOLDER_PATH = "../config/pdf/";

	private static final String SETTING_FILE_PATH = "../config/hong.json";

	//			private static final String EXCEL_FILE_NM = "00.HONG.xlsx";
	//		
	//			private static final String PDF_FOLDER_PATH = "../../";
	//		
	//			private static final String SETTING_FILE_PATH = "../hong.json";

	private static final String EXCEL_FOLDER_NM = "HONG_EXCEL/";

	public static void main(String[] args) {
		PdfConvert3 pdfConvert = new PdfConvert3();
		pdfConvert.prcssConvert();
	}

	public void prcssConvert() {

		System.out.println("## 변환시작 ..");
		File[] files = getPdfFiles();
		// pdf -> excel 변환
		prcssPdfToExcel(files);

		int index = 0;
		for (int k = 0; k < files.length; k++) {

			// PDF ------------------------------------------------------------
			try (PDDocument document = PDDocument.load(files[k]);) {

				String content = new PDFTextStripper().getText(document);
				String contentLine = content.replaceAll(REPLACE_RN, " ");

				// VAT ID 추출 (PK)
				String vatId = getVatId(contentLine);

				System.out.println("## [" + StringUtils.replaceIgnoreCase(files[k].getName(), ".PDF", "") + "] 시작 .. " + vatId);

				// 설정정보 조회
				Map<String, Object> attrMap = getSetting(vatId);

				// 최초한번헤더세팅
				if (k == 0) {

					List<String> headerArray = (List<String>) attrMap.get("headerCd");
					excelData.put(String.valueOf(index++), headerArray.toArray());
				}

				List<String> extraList = new ArrayList<String>();
				// add
				extraList.add(files[k].getName());

				if (attrMap.get("typeCd") != null) {
					// add
					extraList.add((String) attrMap.get("typeCd"));

					List<String> itemArray = (List<String>) attrMap.get("itemCd");

					for (String item : itemArray) {
						String rslt = StringUtils.EMPTY;
						// add
						try {
							rslt = processItem(files[k], content, contentLine, item, attrMap);
							System.out.println("# [" + item + "] 결과 [" + rslt + "]");
						} catch (Exception e) {
							rslt = "[ERROR] " + item + " 오류 발생";
							e.printStackTrace();
						}
						extraList.add(rslt);
					}

					// pdf 전체내용
					extraList.add(content);
					extraList.add(contentLine);
				} else {
					// add
					extraList.add(vatId);
					extraList.add("[ERROR] " + vatId + " 설정정보를 불러 수 없습니다.");
					System.out.println("## [ERROR] 설정정보를 불러 수 없습니다..");
				}
				// System.out.println(content);
				// System.out.println(contentLine);

				excelData.put(String.valueOf(index++), extraList.toArray());

				document.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("## 결과 엑셀 작성  ..");
		// 엑셀 생성
		writeExcel();
		System.out.println("## 종료 ..");

	}

	private String processItem(File file, String content, String contentLine, String item, Map<String, Object> attrMap) {

		String rslt = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(item)) {
			JSONArray optionArray = (JSONArray) attrMap.get(item);
			if (optionArray != null) {

				String pdfExcel = (String) optionArray.get(0);

				int delStrIndx = 4;
				if (pdfExcel.equals("PDF")) {
					String type = optionArray.get(1).toString();

					if (type.equals("01")) {
						for (String text : content.split("\\n")) {
							if (text.indexOf(item) != -1) {
								rslt = text.substring(text.indexOf(item) + item.length(), text.length()).trim();
								break;
							}
						}
						delStrIndx = 2;
					} else if (type.equals("02")) {
						JSONArray replaceStart = (JSONArray) optionArray.get(2);
						JSONArray replaceEnd = (JSONArray) optionArray.get(3);
						int startIdx = 0;
						for (int i = 0; i < replaceStart.size(); i++) {
							startIdx = StringUtils.indexOf(contentLine, replaceStart.get(i).toString(), startIdx)
									+ replaceStart.get(i).toString().length();
						}
						int endIdx = startIdx + 1;
						for (int i = 0; i < replaceEnd.size(); i++) {
							endIdx = StringUtils.indexOf(contentLine, replaceEnd.get(i).toString(), endIdx)
									+ replaceEnd.get(i).toString().length();
						}
						//System.out.println("## item : " + item + "## startIdx : " + startIdx + " : endIdx : " + endIdx);
						rslt = contentLine.substring(startIdx, endIdx).trim();
						delStrIndx = 4;

					} else if (type.equals("03")) {
						int startIndex = contentLine.indexOf(optionArray.get(2).toString());
						int endIndex = contentLine.indexOf(optionArray.get(3).toString(), startIndex);
						rslt = contentLine.substring(startIndex, endIndex);
						delStrIndx = 4;
					} else if (type.equals("04")) {
						JSONArray replaceStart = (JSONArray) optionArray.get(2);
						JSONArray replaceEnd = (JSONArray) optionArray.get(3);
						int startIdx = 0;
						for (int i = 0; i < replaceStart.size(); i++) {
							startIdx = StringUtils.indexOf(contentLine, replaceStart.get(i).toString(), startIdx);
						}
						int endIdx = startIdx;
						for (int i = 0; i < replaceEnd.size(); i++) {
							endIdx = StringUtils.lastIndexOf(contentLine, replaceEnd.get(i).toString(), endIdx)
									- replaceEnd.get(i).toString().length();
							;
						}
						rslt = contentLine.substring(endIdx + 1, startIdx).trim();
						delStrIndx = 4;

					} else if (type.equals("05")) {
						StringBuilder sb = new StringBuilder();
						List<String> readLines = (List<String>) optionArray.get(2);
						int idx = 1;
						for (String text : content.split("\\n")) {
							for (String lineNumber : readLines) {
								if (idx == Integer.parseInt(lineNumber)) {
									sb.append(text.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "")).append(" ");
								}
							}
							idx++;
						}
						rslt = sb.toString().trim();
						delStrIndx = 3;
					}

					// 단어삭제
					rslt = getDeleteText(rslt, ((JSONArray) optionArray.get(delStrIndx)));

				} else if (pdfExcel.equals("EXCEL")) {

					try (FileInputStream fileInputStream = new FileInputStream(new File(
							PDF_FOLDER_PATH + EXCEL_FOLDER_NM + StringUtils.replaceIgnoreCase(file.getName(), "PDF", "xlsx")))) {

						XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
						XSSFSheet sheet = workbook.getSheetAt(0);

						JSONArray rowCellArray = (JSONArray) optionArray.get(1);
						JSONArray deleteArray = (JSONArray) optionArray.get(2);

						for (int z = 0; z < rowCellArray.size(); z++) {
							String rowCells[] = rowCellArray.get(z).toString().split("/");
							XSSFRow row = sheet.getRow(Integer.parseInt(rowCells[0]) - 1);
							if (row != null) {
								XSSFCell cell = row.getCell(getCellConvert(rowCells[1]));
								if (cell != null) {
									if (z == 0) {
										rslt = cell.toString().trim();
									} else {
										rslt = rslt + " " + cell.toString().trim();
									}
								} else {
									rslt = "[ERROR] " + item + " 항목에 해당하는 option 항목이 존재하지 않습니다.";
								}
							}
							// 단어삭제
							rslt = getDeleteText(rslt, deleteArray);
						}
						workbook.close();
					} catch (Exception e) {

						e.printStackTrace();
					}
				}

			} else {
				rslt = "[ERROR] " + item + " 항목에 해당하는 option 항목이 존재하지 않습니다.";
			}
		}

		return rslt;
	}

	public void writeExcel() {
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

	// VAT 번호 추출
	public String getVatId(String contentLine) {
		String vatId = StringUtils.EMPTY;
		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);
			JSONObject typeObject = (JSONObject) jsonRoot.get("type");

			List<Map.Entry<String, Object>> vatIdList = (List<Entry<String, Object>>) typeObject.entrySet().stream()
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

			if (vatId.equals("DE118569718")) {
				Pattern patternDuplication = Pattern.compile("Umsatzsteuer-Identifikationsnummer");
				Matcher matcherDuplication = patternDuplication.matcher(contentLine);
				vatId = matcherDuplication.find() ? vatId + "H&M" : vatId + "COS";
			}

			if (StringUtils.isBlank(vatId)) {
				vatId = "DE175944429";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return vatId;
	}

	// pdf 파일 excel 변환
	public void prcssPdfToExcel(File[] files) {
		int cnt = 1;
		for (File file : files) {
			PdfDocument pdf = new PdfDocument();
			pdf.loadFromFile(file.getPath());
			pdf.saveToFile(PDF_FOLDER_PATH + EXCEL_FOLDER_NM + StringUtils.replaceIgnoreCase(file.getName(), "PDF", "xlsx"),
					FileFormat.XLSX);
			System.out.println("# " + StringUtils.replaceIgnoreCase(file.getName(), "PDF", "xlsx") + " 완료[" + (cnt++) + "] .. ");
		}
	}

	// pdf 파일 목록 조회
	public File[] getPdfFiles() {
		File dir = new File(PDF_FOLDER_PATH);
		return dir.listFiles((FileFilter) file -> file.isFile() && file.getName().toUpperCase().endsWith("PDF"));
	}

	// 세팅정보 조회
	public Map<String, Object> getSetting(String vatId) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try (Reader reader = new FileReader(SETTING_FILE_PATH);) {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonRoot = (JSONObject) jsonParser.parse(reader);

			JSONArray headerArray = (JSONArray) jsonRoot.get("header");
			map.put("headerCd", headerArray);

			JSONObject typeObject = (JSONObject) jsonRoot.get("type");
			if (typeObject.get(vatId) == null) {
				return map;
			}
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
			//System.out.println(Collections.singletonList(map));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	// 단어삭제
	public String getDeleteText(String text, JSONArray deleteArray) {
		if (deleteArray != null && deleteArray.size() > 0) {
			for (int i = 0; i < deleteArray.size(); i++) {
				String delText = deleteArray.get(i).toString();
				// @formatter:off
				text = text.replace(delText, StringUtils.EMPTY)
						   .replaceAll("\\r\\n", StringUtils.EMPTY)
						   .replaceAll(" ", StringUtils.EMPTY) // 아스키코드 (160번)
						   .trim();
				// @formatter:on
			}
		}
		return text;
	}

	public int getCellConvert(String cellIdx) {
		// @formatter:off
		int cellNumber = 0;
		switch (cellIdx) {
		case "A":cellNumber = 0;break;	case "B":cellNumber = 1;break;	case "C":cellNumber = 2;break;	case "D":cellNumber = 3;break;	case "E":cellNumber = 4;break;
		case "F":cellNumber = 5;break;	case "G":cellNumber = 6;break;	case "H":cellNumber = 7;break;	case "I":cellNumber = 8;break;	case "J":cellNumber = 9;break;
		case "K":cellNumber = 10;break;	case "L":cellNumber = 11;break;	case "M":cellNumber = 12;break;	case "N":cellNumber = 13;break;	case "O":cellNumber = 14;break;
		case "P":cellNumber = 15;break;	case "Q":cellNumber = 16;break;	case "R":cellNumber = 17;break;	case "S":cellNumber = 18;break;	case "T":cellNumber = 19;break;
		case "U":cellNumber = 20;break;	case "V":cellNumber = 21;break;	case "W":cellNumber = 22;break;	case "X":cellNumber = 23;break;	case "Y":cellNumber = 24;break;
		case "Z":cellNumber = 25;break;	case "AA":cellNumber = 26;break;case "AB":cellNumber = 27;break;case "AC":cellNumber = 28;break;case "AD":cellNumber = 29;break;
		case "AE":cellNumber = 30;break;case "AF":cellNumber = 31;break;case "AG":cellNumber = 32;break;case "AH":cellNumber = 33;break;case "AI":cellNumber = 34;break;
		case "AJ":cellNumber = 35;break;case "AK":cellNumber = 36;break;case "AL":cellNumber = 37;break;case "AM":cellNumber = 38;break;case "AN":cellNumber = 39;break;
		case "AO":cellNumber = 40;break;case "AP":cellNumber = 41;break;case "AQ":cellNumber = 42;break;case "AR":cellNumber = 43;break;case "AS":cellNumber = 44;break;
		case "AT":cellNumber = 45;break;case "AU":cellNumber = 46;break;case "AV":cellNumber = 47;break;case "AW":cellNumber = 48;break;case "AX":cellNumber = 49;break;
		case "AY":cellNumber = 50;break;case "AZ":cellNumber = 51;break;}
		return cellNumber;
		// @formatter:on
	}
}
