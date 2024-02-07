package hong;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class replaceTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String aa = "Nettobetrag 39,29) 7,46 Der Rechnungsbetrag";

		String bb = StringUtils.replaceEach(aa, new String[] { "Nettobetrag", "Der Rechnungsbetrag" }, new String[] { "", "" });

		String cc = RegExUtils.replaceAll(aa, Pattern.compile("[Nettobetrag](.*?)[)]"), StringUtils.EMPTY);
		
		cc = RegExUtils.replaceAll(cc, Pattern.compile("Der Rechnungsbetrag"), StringUtils.EMPTY);

		System.out.println(cc);

	}

}
