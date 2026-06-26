package hong;

import java.util.function.Consumer;

import org.apache.commons.lang3.ObjectUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * logback 로그 이벤트를 Swing UI(로그 텍스트 영역)로 전달하는 어펜더
 * (변환 엔진의 log.info/log.error 출력을 화면에 그대로 표시)
 */
public class JTextAreaAppender extends AppenderBase<ILoggingEvent> {

	// UI 로그 출력 리스너 (GUI가 등록)
	private static Consumer<String> logListener;

	// 로그 리스너 등록
	public static void setLogListener(Consumer<String> listener) {
		logListener = listener;
	}

	// 로그 이벤트 발생 시 리스너로 한 줄 전달
	@Override
	protected void append(ILoggingEvent event) {
		if (ObjectUtils.isNotEmpty(logListener)) {
			String line = String.format("%tT %-5s %s%n", event.getTimeStamp(), event.getLevel(),
					event.getFormattedMessage());
			logListener.accept(line);
		}
	}
}
