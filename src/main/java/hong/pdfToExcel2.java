package hong;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

public class pdfToExcel2 {

	private static final String PDF_FOLDER_PATH = "./config/pdf/";
	
	private static final String PDF_NM = "AMAZON.de";

	public static void main(String[] args) {
		
		System.out.println("##### START ..");

		//Create PDF document
		PdfDocument pdf = new PdfDocument();

		//Load the PDF document from disk.
		pdf.loadFromFile(PDF_FOLDER_PATH + PDF_NM + ".pdf");

		//Save the document
		pdf.saveToFile(PDF_FOLDER_PATH + PDF_NM + ".xlsx", FileFormat.XLSX);
		
		System.out.println("##### END ..");
	}
}
