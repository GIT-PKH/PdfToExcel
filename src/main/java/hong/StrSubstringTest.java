package hong;

import org.apache.commons.lang3.StringUtils;

public class StrSubstringTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String attrKey = "Datum";

		//String text = "Rechnung/Lieferschein Datum:  25.12.2023";

		//String text = "Datum:  25.12.2023";

		//String text = "Datum:  25.12.2023 Rechnung/Lieferschein";

		String text = "bezahlt Datum:  25.12.2023 Rechnung/Lieferschein";

		System.out.println(text.indexOf(attrKey));

		System.out.println(text.indexOf(attrKey) + attrKey.length());

		String rslt = text.substring(text.indexOf(attrKey) + attrKey.length(), text.length());

		System.out.println(StringUtils.replaceEach(rslt, new String[] { ":", "€" }, new String[] { "", "" }));

	}
}
