package hong;

import org.apache.commons.lang3.StringUtils;

public class StrSubstringTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String str = "A!C@B#C$D%E^D&C*G";

		// D = 8
		System.out.println(str.indexOf("D", -14));
		System.out.println(str.lastIndexOf("C", 8));

	}
}
