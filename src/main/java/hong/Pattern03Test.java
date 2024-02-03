package hong;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Pattern03Test {

	private static final String PDF_FOLDER_PATH = "./config/pdf/";

	private static final String FILE_NM = "BESTSECRET.pdf";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		PDDocument document = PDDocument.load(new File(PDF_FOLDER_PATH + FILE_NM));
		String content = new PDFTextStripper().getText(document);

		content = content.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", " ");
		
		System.out.println(content);
		
		
		String str01 = "Haewon Park";
		
		String str02 = "Germany";
		
		
		int a01 = content.indexOf(str01);
		
		int a02 = content.indexOf(str02, a01);
		
		
		System.out.println("######## STR : " + a01);
		
		System.out.println("######## STR : " + a02);
		
		String b01 = content.substring(a01 + str01.length(), a02 + str02.length());
		
		String b02 = content.substring(a01 + str01.length(), a02);
		
		String b03 = content.substring(a01, a02);
		
		System.out.println("######## STR01 : " + StringUtils.replaceEach(b01, new String[] { ":", "€" }, new String[] { "", "" }).trim());
		
		System.out.println("######## STR02 : " + StringUtils.replaceEach(b02, new String[] { ":", "€" }, new String[] { "", "" }).trim());
		
		System.out.println("######## STR02 : " + StringUtils.replaceEach(b03, new String[] { ":", "€" }, new String[] { "", "" }).trim());

	}

}
