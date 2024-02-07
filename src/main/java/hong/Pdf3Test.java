package hong;

public class Pdf3Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String contentLine = "Rechnung LU-BIO-04  Amazon EU S.à r.l. - 38 avenue John F. Kennedy, L-1855 Luxembourg  Sitz der Gesellschaft: L-1855 Luxemburg  eingetragen im Luxemburgischen Handelsregister unter R.C.S. B 101818 • Stammkapital: 37.500 EUR  Amazon EU S.à r.l., Niederlassung Deutschland – Marcel-Breuer-Str. 12, D-80807 München, Deutschland  Sitz der Zweigniederlassung: München  eingetragen im Handelsregister des Amtsgerichts München unter HRB 218574 USt-ID : DE 814584193  Amazon EU S.a.r.L., Niederlassung Deutschland ist bei der Stiftung ear für Elektro- und Elektronikgeräte registriert: WEEE-Reg.-Nr. DE 89633988 Seite 1 von 1 USt. %  Zwischensumme (ohne USt.) USt. 19% 536,55 € 101,94 € USt. Gesamt 536,55 € 101,94 € Gesamtpreis 638,49 € Rechnungsdetails Bestelldatum 23 Dezember 2023 Bestellnummer 302-3199561-2304335  Zahlungsreferenznummer 2fmc83Kx24bzBDvWWBDM Verkauft von Amazon EU S.à r.l., Niederlassung Deutschland  USt-IDNr. DE814584193 Rechnungsdatum /Lieferdatum 27 Dezember 2023 Rechnungsnummer DE37ST38DAEUI Zahlbetrag 638,49 € BM & T GMBH FAMBIS  IN DER AU 17 (GSP36990)  OBERURSEL, 61440  DE Um unseren Kundenservice zu kontaktieren, besuche www.amazon.de/contact-us Rechnungsadresse  BM & T GmbH fambis  In der Au 17 (GSP36990)  Oberursel, 61440  DE  Lieferadresse  BM & T GmbH fambis  In der Au 17 (GSP36990)  Oberursel, 61440  DE  Verkauft von  Amazon EU S.à r.l., Niederlassung Deutschland Marcel-Breuer-Str. 12 80807 München Deutschland USt-IDNr. DE814584193  Bestellinformationen Beschreibung Menge Stückpreis (ohne USt.) USt. % Stückpreis (inkl. USt.) Zwischensumme (inkl. USt.) Venta Luftwäscher Aerostyle LW74 WiFi, Luftbefeuchtung und Luftreinigung (bis 10 µm Partikel) für Räume bis 90 qm, Signalschwarz, inkl. WiFi/WLAN-Modul  ASIN: B08KGBRYCP 1 536,55 € 19% 638,49 € 638,49 € Versandkosten 3,35 € 3,99 € 3,99 € Aktionsrabatt -3,35 € -3,99 € -3,99 € ";

		String a = "Ust. Gesamt";
		String b = "€";

		int startIndex = contentLine.indexOf(a);

		System.out.println(startIndex);

		int endIndex = contentLine.indexOf("€", startIndex + a.length());
		System.out.println(endIndex);
		String rslt = contentLine.substring(startIndex, endIndex + b.length());

		System.out.println(rslt);

	}

}
