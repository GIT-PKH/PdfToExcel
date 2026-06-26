package hong;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * PDF 영수증을 읽어 업체별 추출 규칙(hong.json)에 따라 항목을 파싱한 뒤 엑셀로 정리하는 배치 프로그램
 */
@Slf4j
@SuppressWarnings("unchecked")
public class PdfConvert {

	// 엑셀 행 데이터 (key: 행 순번, value: 셀 값 배열)
	static Map<String, Object[]> excelData = new LinkedHashMap<>();

	// 결과 엑셀 파일명 (기본 DEV)
	private static String excelFileNm = Constants.EXCEL_FILE_NM_DEV;

	// PDF 입력 폴더 경로 (기본 DEV)
	private static String pdfFolderPath = Constants.PDF_FOLDER_PATH_DEV;

	// 설정 파일(hong.json) 경로 (기본 DEV)
	private static String settingFilePath = Constants.SETTING_FILE_PATH_DEV;

	// PDF 입력 폴더 경로 설정 (GUI 등 외부에서 주입)
	public static void setPdfFolderPath(String path) {
		pdfFolderPath = path;
	}

	// 설정 파일(hong.json) 경로 설정 (GUI 등 외부에서 주입)
	public static void setSettingFilePath(String path) {
		settingFilePath = path;
	}

	// 결과 엑셀 파일명 설정 (GUI 등 외부에서 주입)
	public static void setExcelFileNm(String fileName) {
		excelFileNm = fileName;
	}

	// 프로그램 진입점 (실행 인자 유무로 DEV/PROD 분기)
	public static void main(String[] args) {

		// 개발, 배포 환경에 따른 파일 경로 설정
		if (ArrayUtils.isNotEmpty(args)) {
			excelFileNm = Constants.EXCEL_FILE_NM_PROD;
			pdfFolderPath = Constants.PDF_FOLDER_PATH_PROD;
			settingFilePath = Constants.SETTING_FILE_PATH_PROD;
			log.info("## 실행모드 .. [PROD]");
		}

		PdfConvert pdfConvert = new PdfConvert();
		pdfConvert.prcssConvert();
	}

	// 전체 변환 처리 (PDF 수집 -> 항목 추출 -> 엑셀 작성), 결과 엑셀 파일 반환
	public File prcssConvert() {

		int index = 0;

		log.info("## 변환시작 ..");
		// 재실행 대비 누적 데이터 초기화
		excelData.clear();
		File[] files = getPdfFiles();

		// pdf -> excel 변환
		prcssPdfToExcel(files);

		Map<String, Object> jsonMap = loadSetting();
		Map<String, Object> typeMap = (Map<String, Object>) jsonMap.get(Constants.JSON_KEY_TYPE);
		Map<String, Object> propertyMap = (Map<String, Object>) jsonMap.get(Constants.JSON_KEY_PROPERTY);
		List<String> headerList = (List<String>) jsonMap.get(Constants.JSON_KEY_HEADER);

		excelData.put(String.valueOf(index++), headerList.toArray());

		for (int k = 0; k < files.length; k++) {

			// PDF ------------------------------------------------------------
			try (PDDocument document = PDDocument.load(files[k]);) {

				String content = new PDFTextStripper().getText(document);
				String contentLine = content.replaceAll(Constants.REPLACE_RN, " ");

				// VAT ID 추출 (PK)
				String vatId = getVatId(typeMap, contentLine);

				log.info("## [{}] 시작 .. {}", StringUtils.replaceIgnoreCase(files[k].getName(), Constants.EXT_DOT_PDF, ""),
						vatId);

				List<String> extraList = new ArrayList<>();
				// excel (파일명)
				extraList.add(files[k].getName());

				if (ObjectUtils.isNotEmpty(typeMap.get(vatId))) {
					String brandId = getBrandId(typeMap, contentLine, vatId);
					// excel (구매처)
					extraList.add(brandId);

					Map<String, Object> brandMap = (Map<String, Object>) propertyMap.get(brandId);

					List<String> itemList = (List<String>) brandMap.get(Constants.JSON_KEY_ITEM);

					for (String itemCode : itemList) {
						String result = StringUtils.EMPTY;
						try {
							result = processItem(files[k], content, contentLine, itemCode, brandMap);
							log.info("# [{}] 결과 [{}]", itemCode, result);
						} catch (Exception e) {
							result = Constants.ERROR_PREFIX + itemCode + Constants.MSG_ITEM_ERROR;
							log.error("# [{}] 추출 오류", itemCode, e);
						}
						// excel (세금번호, 인보이스 발행날짜, 인보이스 번호, 주문번호, 총금액, 부가세 제외, 부가세1, 부가세2, All ADD)
						extraList.add(result);
					}

					// excel (All Text1, All Text2)
					extraList.add(content);
					extraList.add(contentLine);
				} else {
					// excel (구매처)
					extraList.add(vatId);
					// excel (세금번호)
					extraList.add(vatId);
					// excel (인보이스 발행날짜)
					extraList.add(Constants.ERROR_PREFIX + vatId + Constants.MSG_NO_SETTING);
					log.error("## [ERROR] 설정정보를 불러 수 없습니다..");
				}
				excelData.put(String.valueOf(index++), extraList.toArray());

				document.close();

			} catch (Exception e) {
				log.error("## PDF 처리 오류", e);
			}
		}
		log.info("## 결과 엑셀 작성  ..");
		// 엑셀 생성
		writeExcel();
		log.info("## 종료 ..");

		return new File(pdfFolderPath, excelFileNm);
	}

	// 단일 항목(itemCode) 값을 추출 규칙(option)에 따라 추출
	private String processItem(File file, String content, String contentLine, String itemCode,
			Map<String, Object> brandMap) {

		String result = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(itemCode)) {
			Map<String, Object> optionMap = (Map<String, Object>) brandMap.get(Constants.JSON_KEY_OPTION);
			List<Object> optionArray = (List<Object>) optionMap.get(itemCode);
			if (ObjectUtils.isNotEmpty(optionArray)) {
				String sourceType = (String) optionArray.get(0);

				List<String> deleteWordList = null;
				if (Constants.SOURCE_PDF.equals(sourceType)) {
					String type = optionArray.get(1).toString();

					if (Constants.EXTRACT_TYPE_01.equals(type)) {
						for (String text : content.split(Constants.LINE_SPLIT_REGEX)) {
							if (StringUtils.contains(text, itemCode)) {
								result = text.substring(text.indexOf(itemCode) + itemCode.length(), text.length()).trim();
								break;
							}
						}
						deleteWordList = (List<String>) optionArray.get(2);

					} else if (Constants.EXTRACT_TYPE_02.equals(type)) {
						List<String> replaceStart = (List<String>) optionArray.get(2);
						List<String> replaceEnd = (List<String>) optionArray.get(3);
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
						result = contentLine.substring(startIdx, endIdx).trim();
						deleteWordList = (List<String>) optionArray.get(4);

					} else if (Constants.EXTRACT_TYPE_03.equals(type)) {
						int startIndex = contentLine.indexOf(optionArray.get(2).toString());
						int endIndex = contentLine.indexOf(optionArray.get(3).toString(), startIndex);
						result = contentLine.substring(startIndex, endIndex);
						deleteWordList = (List<String>) optionArray.get(4);

					} else if (Constants.EXTRACT_TYPE_04.equals(type)) {
						List<String> replaceStart = (List<String>) optionArray.get(2);
						List<String> replaceEnd = (List<String>) optionArray.get(3);
						int startIdx = 0;
						for (int i = 0; i < replaceStart.size(); i++) {
							startIdx = StringUtils.indexOf(contentLine, replaceStart.get(i).toString(), startIdx);
						}
						int endIdx = startIdx;
						for (int i = 0; i < replaceEnd.size(); i++) {
							endIdx = StringUtils.lastIndexOf(contentLine, replaceEnd.get(i).toString(), endIdx)
									- replaceEnd.get(i).toString().length();
						}
						result = contentLine.substring(endIdx + 1, startIdx).trim();
						deleteWordList = (List<String>) optionArray.get(4);

					} else if (Constants.EXTRACT_TYPE_05.equals(type)) {
						StringBuilder lineBuilder = new StringBuilder();
						List<String> readLines = (List<String>) optionArray.get(2);
						int idx = 1;
						for (String text : content.split(Constants.LINE_SPLIT_REGEX)) {
							for (String lineNumber : readLines) {
								if (idx == Integer.parseInt(lineNumber)) {
									lineBuilder.append(text.replaceAll(Constants.REPLACE_RN, "")).append(" ");
								}
							}
							idx++;
						}
						result = lineBuilder.toString().trim();
						deleteWordList = (List<String>) optionArray.get(3);

					} else if (Constants.EXTRACT_TYPE_06.equals(type)) {
						result = optionArray.get(2).toString();
					}

					// 단어삭제
					result = getDeleteText(result, deleteWordList);

				} else if (Constants.SOURCE_EXCEL.equals(sourceType)) {

					try (FileInputStream fileInputStream = new FileInputStream(new File(pdfFolderPath
							+ Constants.EXCEL_FOLDER_NM
							+ StringUtils.replaceIgnoreCase(file.getName(), Constants.EXT_PDF, Constants.EXT_XLSX)))) {

						XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
						XSSFSheet sheet = workbook.getSheetAt(0);

						List<String> rowCellArray = (List<String>) optionArray.get(1);
						List<String> deleteArray = (List<String>) optionArray.get(2);

						for (int z = 0; z < rowCellArray.size(); z++) {
							String[] rowCells = rowCellArray.get(z).toString().split("/");
							XSSFRow row = sheet.getRow(Integer.parseInt(rowCells[0]) - 1);
							if (ObjectUtils.isNotEmpty(row)) {
								XSSFCell cell = row.getCell(getCellConvert(rowCells[1]));
								if (ObjectUtils.isNotEmpty(cell)) {
									if (z == 0) {
										result = cell.toString().trim();
									} else {
										result = result + " " + cell.toString().trim();
									}
								} else {
									result = Constants.ERROR_PREFIX + itemCode + Constants.MSG_NO_OPTION;
								}
							}
							// 단어삭제
							result = getDeleteText(result, deleteArray);
						}
						workbook.close();
					} catch (Exception e) {
						log.error("# [{}] 엑셀 추출 오류", itemCode, e);
					}
				}
			} else {
				result = Constants.ERROR_PREFIX + itemCode + Constants.MSG_NO_OPTION;
			}
		}

		return result;
	}

	// 누적된 행 데이터를 엑셀 파일로 작성
	public void writeExcel() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("HONG");
		Set<String> keySet = excelData.keySet();
		int rownum = 0;
		for (String key : keySet) {
			Row row = sheet.createRow(rownum++);
			Object[] rowValues = excelData.get(key);
			int cellnum = 0;
			for (Object cellValue : rowValues) {
				Cell cell = row.createCell(cellnum++);
				if (cellValue instanceof String) {
					cell.setCellValue((String) cellValue);
				} else if (cellValue instanceof Integer) {
					cell.setCellValue((Integer) cellValue);
				}
			}
		}
		try (FileOutputStream out = new FileOutputStream(new File(pdfFolderPath, excelFileNm));) {
			workbook.write(out);
			workbook.close();
			out.close();
		} catch (IOException e) {
			log.error("## 엑셀 작성 오류", e);
		}
	}

	// 브랜드 식별 (단일 세금번호에 다중 브랜드가 묶인 경우 TYPE_HONG 규칙으로 분기)
	public String getBrandId(Map<String, Object> typeMap, String contentLine, String vatId) {

		String brandId = StringUtils.EMPTY;
		if (typeMap.get(vatId) instanceof JSONObject) {

			Map<String, Object> brandMap = (Map<String, Object>) typeMap.get(vatId);

			if (brandMap.containsKey(Constants.TYPE_HONG_01)) {

				Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get(Constants.TYPE_HONG_01);

				List<Map.Entry<String, Object>> vatIdList = (List<Entry<String, Object>>) typeHongMap.entrySet()
						.stream().collect(Collectors.toList());

				for (Map.Entry<String, Object> entry : vatIdList) {

					String tmpVatId = entry.getKey();
					Pattern pattern = Pattern.compile(tmpVatId);
					Matcher matcher = pattern.matcher(contentLine);

					if (matcher.find()) {
						brandId = (String) typeHongMap.get(tmpVatId);
						break;
					}
				}
			} else if (brandMap.containsKey(Constants.TYPE_HONG_02)) {

				Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get(Constants.TYPE_HONG_02);
				brandId = (String) typeHongMap.get(Constants.JSON_KEY_BRAND);

			} else if (brandMap.containsKey(Constants.TYPE_HONG_03)) {

				// 예약(미사용)
			}

		} else {
			brandId = (String) typeMap.get(vatId);
		}
		return brandId;
	}

	// VAT 번호(세금번호) 추출 (등록 키 매칭 -> 정규식 -> KEYWORD 추정 순)
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
			Pattern pattern = Pattern.compile(Constants.VAT_ID_PATTERN);
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
					if (brandMap.containsKey(Constants.TYPE_HONG_02)) {
						Map<String, Object> typeHongMap = (Map<String, Object>) brandMap.get(Constants.TYPE_HONG_02);
						List<String> keywordList = (List<String>) typeHongMap.get(Constants.JSON_KEY_KEYWORD);
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

	// 각 PDF 파일을 동일 이름의 xlsx 사본으로 변환 (셀 좌표 추출용)
	public void prcssPdfToExcel(File[] files) {
		// 변환 사본 저장 폴더 생성 (없으면)
		new File(pdfFolderPath, Constants.EXCEL_FOLDER_NM).mkdirs();
		int fileCount = 1;
		for (File file : files) {
			PdfDocument pdf = new PdfDocument();
			pdf.loadFromFile(file.getPath());
			pdf.saveToFile(pdfFolderPath + Constants.EXCEL_FOLDER_NM
					+ StringUtils.replaceIgnoreCase(file.getName(), Constants.EXT_PDF, Constants.EXT_XLSX),
					FileFormat.XLSX);
			log.info("# {} 완료[{}] .. ",
					StringUtils.replaceIgnoreCase(file.getName(), Constants.EXT_PDF, Constants.EXT_XLSX), fileCount++);
		}
	}

	// 입력 폴더에서 PDF 파일 목록 조회 (폴더가 없거나 비어 있으면 빈 배열)
	public File[] getPdfFiles() {
		File dir = new File(pdfFolderPath);
		File[] pdfFiles = dir.listFiles(
				(FileFilter) file -> file.isFile() && file.getName().toUpperCase().endsWith(Constants.EXT_PDF));
		return ObjectUtils.defaultIfNull(pdfFiles, new File[0]);
	}

	// 설정정보(hong.json) 로드
	public Map<String, Object> loadSetting() {

		Map<String, Object> jsonMap = null;

		try (Reader reader = new InputStreamReader(new FileInputStream(settingFilePath), StandardCharsets.UTF_8);) {

			JSONParser jsonParser = new JSONParser();
			jsonMap = (Map<String, Object>) jsonParser.parse(reader);

		} catch (Exception e) {
			log.error("## 설정파일 로드 오류", e);
		}
		return jsonMap;
	}

	// 추출 결과에서 불필요한 단어/공백을 제거
	public String getDeleteText(String text, List<String> deleteArray) {
		if (ObjectUtils.isNotEmpty(deleteArray)) {
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

	// 엑셀 열 문자(A, B, ... AZ)를 0-based 열 인덱스로 변환
	public int getCellConvert(String cellIdx) {
		return CellReference.convertColStringToIndex(cellIdx);
	}
}
