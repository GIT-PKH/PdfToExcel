package hong;

public class IndexOfLastIndexOfTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str = "ABABCACFBDCEFCFAFCFBCFCBCGCBCACBCHCBCIA";
		
		String a = "D";
		String b = "F";
		
		System.out.println(str.indexOf("D"));
		
		System.out.println(str.lastIndexOf("F",str.indexOf("D")));
		
		System.out.println(str.substring(7, 9));
	}
}
