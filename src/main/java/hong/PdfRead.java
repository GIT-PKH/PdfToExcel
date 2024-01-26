package hong;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfRead {
	
	private static final String FILE_PATH = "C:\\Users\\KH\\Documents\\카카오톡 받은 파일\\202401_INVOICE\\2106974405.pdf";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		PDDocument document = PDDocument.load(new File(FILE_PATH));
		String content = new PDFTextStripper().getText(document);

		System.out.println(content);
	}

}
