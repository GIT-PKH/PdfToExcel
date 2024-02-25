package hong;

import java.util.List;
import java.util.Map;

public class SettingVO {

	private Map<String, String> type;
	private List<String> header;

	public Map<String, String> getType() {
		return type;
	}

	public void setType(Map<String, String> type) {
		this.type = type;
	}

	public List<String> getHeader() {
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return "Data{" + "type=" + type + ", header=" + header + '}';
	}
}
