package hong;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

public class PdfToOtherTest {

	private static final String PDF_FOLDER_PATH = "./config/pdf/";

	private static final String PDF_NM = "ASPHALTGOLD";

	public static void main(String[] args) {

		System.out.println("##### START ..");

		//Create PDF document
		PdfDocument pdf = new PdfDocument();

		//Load the PDF document from disk.
		pdf.loadFromFile(PDF_FOLDER_PATH + PDF_NM + ".PDF");

		//Save the document
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".XLSX", FileFormat.XLSX);
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".DOCX", FileFormat.DOCX);
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".HTML", FileFormat.HTML);
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".POSTSCRIPT", FileFormat.POSTSCRIPT);
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".PPTX", FileFormat.PPTX);

		System.out.println("##### END ..");
	}

}
