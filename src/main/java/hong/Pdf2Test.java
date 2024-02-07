package hong;

import org.apache.commons.lang3.StringUtils;

public class Pdf2Test {

	public static void main(String args[]) {

		String contentLine = "Proof of purchase 1285934 Order No: 2536544458 Proof of purchase date: 12/29/21   Billing Address Shipping Address BM & T GmbH DONGHWEE KIM In der Au 17 (GSP24868) Oberursel 61440 Germany BM & T GmbH DONGHWEE KIM In der Au 17 (GSP24868) Oberursel 61440 Germany Payment method MasterCard Items Size Item Price Item Tax Total 1 x Fringed wool scarf One  size 100.84 EUR 19.16 EUR 120.00 EUR Subtotal 100.84 EUR Shipping 0.00 EUR Tax 19.16 EUR TOTAL 120.00 EUR   Acne Studios Floragatan 13 114 31 Stockholm Sweden   customercare@acnestudios.com +46 10 888 73 05   VAT: DE129469082   acnestudios.com";

		String replaceStart[] = { "Tax", "Tax" };

		String replaceEnd[] = { "EUR" };

		int startIdx = 0;
		for (int i = 0; i < replaceStart.length; i++) {
			startIdx = StringUtils.indexOf(contentLine, replaceStart[i], startIdx) + replaceStart[i].length();
			System.out.println("# startIdx : " + startIdx);
		}

		int endIdx = startIdx + 1;
		for (int i = 0; i < replaceEnd.length; i++) {
			endIdx = StringUtils.indexOf(contentLine, replaceEnd[i], endIdx) + replaceEnd[i].length();
			System.out.println("# endIdx : " + endIdx);
		}

		String rsltStr = contentLine.substring(startIdx, endIdx).trim();

		System.out.println("# startIdx : " + startIdx + ", endIdx : " + endIdx + ", rsltStr : " + rsltStr);
	}
}
