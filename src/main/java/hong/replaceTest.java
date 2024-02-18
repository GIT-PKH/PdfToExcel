package hong;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class replaceTest {

	public static void main(String[] args) {
		String something = "hello987*-;hi66"; // 비교할 문자열

        Pattern pattern = Pattern.compile("[0-9]"); // 정규표현식 문자열로 패턴 객체 생성
        Matcher matcher = pattern.matcher(something); // 패턴 객체로 문자열을 필터링한뒤 그 결과값들을 담은 매처 객체 생성

        while (matcher.find()) {
            System.out.println(matcher.group());
            // 루프 1번 : hello987
            // 루프 2번 : hi66
        }
	}

}
