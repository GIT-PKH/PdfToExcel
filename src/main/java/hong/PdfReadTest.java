package hong;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfReadTest {

	private static final String PDF_FOLDER_PATH = "./config/pdf/";

	private static final String FILE_NM = "APO.COM.pdf";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		PDDocument document = PDDocument.load(new File(PDF_FOLDER_PATH + FILE_NM));
		String content = new PDFTextStripper().getText(document);

		System.out.println(content);
	}

}
