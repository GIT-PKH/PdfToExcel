package hong;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Constants {

	public static final String REPLACE_RN = "(\\r\\n|\\r|\\n|\\n\\r)";

	public static final String EXCEL_FILE_NM_DEV = "00.HONG_"
			+ LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".xlsx";

	public static final String PDF_FOLDER_PATH_DEV = "../config/pdf/";

	public static final String SETTING_FILE_PATH_DEV = "../config/hong.json";

	public static final String EXCEL_FILE_NM_PROD = "00.HONG.xlsx";

	public static final String PDF_FOLDER_PATH_PROD = "../../";

	public static final String SETTING_FILE_PATH_PROD = "../hong.json";

	public static final String EXCEL_FOLDER_NM = "HONG_EXCEL/";
}
