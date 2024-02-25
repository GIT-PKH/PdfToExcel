package hong;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Pdf02Test {

	public static void main(String[] args) {

		String contentLine = "Ausdruck Original Stadt Frankfurt(Oder) Rechnungsdatum 29.01.2024 Verkaufsdatum 29.01.2024 Verkäufer: eschuhe.de GmbH Nuhnenstraße 7d 15234 Frankfurt (Oder) USt-IdNr.: DE 815553518 Käufer (16748210): JAYDEN BM n T GmbH In der Au 17 61440 Oberursel USt-IdNr.: RECHNUNG 9875/01/2024/MOD-ESC Bestellnummer DEM000590902-1 Pos. Bezeichnung Größe Menge Maßeinheit NettopreisNettowert USt USt Be- trag Bruttowert 1 VIBRAM-20M7701 BLACK BLACK 49 1 Paar 98,32 98,32 19% 18,68 117,00 Aufstellung USt Nettopreis USt USt Betrag Bruttowert 98,32 19% 18,68 117,00 Zusammen 98,32 18,68 117,00 Gesamtbetrag 117,00 Rechnungsbetrag(Brutto) 117,00 EUR Zahlungsart (PayPal) Angenommen von ............................... eschuhe GmbH Nuhnenstr. 7d 15234 Frankfurt (Oder) Tel.: +49 (0) 335/23386886 E-Mail: info@eschuhe.de Geschäftsführer: Marek Waligóra Eingetragen im Handelsregister des Amtsgerichtes Frankfurt (Oder) unter HRB 15308 FF USt-IdNr.: DE 815553518, Steuernummer: 061/108/04731 Der Administrator personenbezogener Daten ist eschuhe.de GmbH. Weitere Informationen über die Verarbeitung personenbezogener auf: www.eschuhe.de im Bereich Datenschutzerklärung. \r\n"
				+ "";

		String rslt = StringUtils.EMPTY;

		List<String> replaceStart = new ArrayList<String>();
		replaceStart.add("ufer (");
		replaceStart.add(" ");

		List<String> replaceEnd = new ArrayList<String>();
		replaceEnd.add("USt-IdNr.:");

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

		System.out.println("## startIdx : " + startIdx + " : endIdx : " + endIdx);

		rslt = contentLine.substring(startIdx, endIdx).trim();

		System.out.println("## rslt : " + rslt);

	}

}
