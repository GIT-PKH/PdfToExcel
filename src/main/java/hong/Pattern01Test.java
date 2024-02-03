package hong;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Pattern01Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String text = "Hello, my phone number is 123-456-7890. Please call me back \n"
				+ " My email is example123@gmail.com. DE 17 594 4429 Please contact me.\n"
				+ "Umsatzsteuer-Identifikationsnummer asdfasdfasdf";

		Pattern pattern = Pattern.compile("DE [0-9]{2} [0-9]{3} [0-9]{4}");
		Matcher matcher = pattern.matcher(text);

		String str = StringUtils.EMPTY;
		while (matcher.find()) {

			str = matcher.group();
		}

		System.out.println(str);
	}
}
