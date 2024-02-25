package hong;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Pdf04Test {

	public static void main(String[] args) {

		String contentLine = "RECHNUNG Böttcher AG, Stadtrodaer Landstraße 1, DEU-07751 Jena-Zöllnitz BM & T GmbH (KIM TAEGYU) In der Au 17 (GSP 28478) 61440 Oberursel DEUTSCHLAND Rechnung Nr. : 320235304279 Datum : 24.12.2023 Auftrag Nr. : A23005158264 Auftr.-Datum : 24.12.2023 Kunden-Nr. : 43797203 Bearbeiter : Internetauftrag Das Leistungsdatum entspricht dem Rechnungs- / Lieferscheindatum. EUR GesamtpreisSPEArtikel-Nr. EUR Menge ME Einzelpreis Bezeichnung Pos 20420b 1 59,991 STK 59,99V10 Gymnastikmatte Sissel Gym Mat 20420B+ 180 x 60 x 1,5cm, blau, rutschfest t 1 2,991 STK 2,99V20 Verpackungspauschale pro Auftrag 11,97 MwSt 19% MwSt 7% Steuerfrei 62,98 EUR Gesamtwert 74,95 MwSt 16%Netto Netto Netto Netto MwSt 0% Zahlung per PayPal Böttcher AG Stadtrodaer Landstraße 1 07751 Jena-Zöllnitz Vorstand: Udo Böttcher Danilo Frasiak Andreas Ruhland E-Mail: info@bueromarkt-ag.de Internet: www.bueromarkt-ag.de Bio-Kontrollstelle: DE-ÖKO-039 Registergericht: Jena, HRB 209127 Ust-Ident-Nummer: DE 219636488 Aufsichtsratsvorsitzende: Bonita Liebelt ";
		//String contentLine = "A!C@B#C$D%E^D&C*G";

		String rslt = StringUtils.EMPTY;

		List<String> replaceStart = new ArrayList<String>();
		replaceStart.add("Kunden-Nr");
		replaceStart.add("MwSt 19%");

		List<String> replaceEnd = new ArrayList<String>();
		replaceEnd.add(" ");
		replaceEnd.add(" ");

		int startIdx = 0;
		for (int i = 0; i < replaceStart.size(); i++) {
			startIdx = StringUtils.indexOf(contentLine, replaceStart.get(i).toString(), startIdx);

			System.out.println("## startIdx : " + startIdx);
		}

		int endIdx = startIdx;
		for (int i = 0; i < replaceEnd.size(); i++) {
			endIdx = StringUtils.lastIndexOf(contentLine, replaceEnd.get(i).toString(), endIdx)
					- replaceEnd.get(i).toString().length();;
			System.out.println("## endIdx : " + endIdx);
		}

		System.out.println("## endIdx : " + endIdx + " : startIdx : " + startIdx);
		rslt = contentLine.substring(endIdx + 1, startIdx).trim();

		System.out.println("## rslt : " + rslt);

		/*
		int startIdx = 0;
		for (int i = 0; i < replaceStart.size(); i++) {
			startIdx = StringUtils.indexOf(contentLine, replaceStart.get(i).toString(), startIdx)
					+ (((i + 1) == replaceStart.size()) ? 0 : replaceStart.get(i).toString().length());
			
			System.out.println("## startIdx : " + startIdx);
		}
		
		int endIdx = startIdx + 1;
		for (int i = 0; i < replaceEnd.size(); i++) {
			endIdx = StringUtils.lastIndexOf(contentLine, replaceEnd.get(i).toString(), endIdx)
					+ replaceEnd.get(i).toString().length()
					;
			System.out.println("## endIdx : " + endIdx);
		}
		*/

	}
}
