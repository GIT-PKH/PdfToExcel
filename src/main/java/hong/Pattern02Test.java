package hong;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pattern02Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String text = "Hello, my phone number is 123-456-7890. Please call me back \n"
				+ " My email is example123@gmail.com. DE 132490588 Please contact me.\n"
				+ "Umsatzsteuer-Identifikationsnummer asdfasdfasdf";

		Pattern pattern = Pattern.compile("Umsatzsteuer-Identifikationsnummer");
		Matcher matcher = pattern.matcher(text);

		System.out.println(matcher.find());
	}
}
