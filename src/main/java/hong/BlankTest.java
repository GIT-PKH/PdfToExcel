package hong;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;

public class BlankTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String content = "Rechnungsbetrag 41,49 €";
		
		content = content.substring(16);
		
		System.out.println("## content : " + content);
		
		List<String> deleteArray = new ArrayList<String>();
		deleteArray.add("€");
		deleteArray.add(" ");
		
		content = getDeleteText(content, deleteArray);
		
		System.out.println("## content : " + content);
		
		for (int i = 0; i < content.length(); i++) {
			char tmp = content.charAt(i);
			System.out.println("## char : " + (int)tmp);
		}
	}

	public static String getDeleteText(String text, List<String> deleteArray) {
		if (deleteArray != null && deleteArray.size() > 0) {
			for (int i = 0; i < deleteArray.size(); i++) {
				String delText = deleteArray.get(i).toString();
				text = RegExUtils
						.replaceAll(text.replace(delText, StringUtils.EMPTY), Pattern.compile("\\r\\n"), StringUtils.EMPTY)
						.trim();
				
				System.out.println("## text : " + text.trim());
			}
		}
		return text;
	}
}
