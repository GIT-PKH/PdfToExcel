package hong;

import java.io.File;

public class FilePathTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		File file = new File("../");

		System.out.println(file.getPath());
		System.out.println(file.getParent());
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getCanonicalPath());

	}
}
