package hong;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatLightLaf;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * PDF -> 엑셀 변환기 Swing 메인 화면
 * (변환 엔진 PdfConvert 를 재사용하며, 진행 로그는 JTextAreaAppender 로 화면에 표시)
 */
@Slf4j
public class PdfConvertApp extends JFrame {

	private static final long serialVersionUID = 1L;

	// 8px 그리드 기반 여백
	private static final int GAP = 8;

	// PDF 입력 폴더 입력 필드
	private final JTextField pdfFolderField = new JTextField();

	// 설정 파일(hong.json) 입력 필드
	private final JTextField settingFileField = new JTextField();

	// 진행 로그 표시 영역
	private final JTextArea logArea = new JTextArea();

	// 변환 시작 버튼
	private final JButton runButton = new JButton("변환 시작");

	// 진행 상태 표시줄
	private final JProgressBar progressBar = new JProgressBar();

	// 프로그램 진입점
	public static void main(String[] args) {
		// 모던 Look&Feel 적용
		FlatLightLaf.setup();
		SwingUtilities.invokeLater(() -> new PdfConvertApp().setVisible(true));
	}

	// 생성자: 기본값 세팅 + 로그 브리지 + 화면 구성
	public PdfConvertApp() {
		super("홍군 칼퇴 프로그램 - PDF 변환기");
		settingFileField.setText(Constants.SETTING_FILE_PATH_DEV);
		initLogBridge();
		initUi();
	}

	// logback 루트 로거에 UI 어펜더를 연결 (엔진 로그 -> 화면)
	private void initLogBridge() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		JTextAreaAppender appender = new JTextAreaAppender();
		appender.setContext(loggerContext);
		appender.start();
		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
		JTextAreaAppender.setLogListener(line -> SwingUtilities.invokeLater(() -> appendLog(line)));
	}

	// 화면 구성 (탭: 변환 / 설정 관리)
	private void initUi() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(780, 560);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("변환", buildConvertPanel());
		tabbedPane.addTab("설정 관리", buildSettingPanel());
		add(tabbedPane, BorderLayout.CENTER);
	}

	// 변환 탭 (상단 입력 / 중앙 로그 / 하단 실행)
	private JComponent buildConvertPanel() {
		JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
		panel.add(buildInputPanel(), BorderLayout.NORTH);
		panel.add(buildLogPanel(), BorderLayout.CENTER);
		panel.add(buildActionPanel(), BorderLayout.SOUTH);
		return panel;
	}

	// 설정 관리 탭 (현재 설정파일을 저장소로 로드, P3에서 편집 폼 확장 예정)
	private JComponent buildSettingPanel() {
		return new SettingManagerPanel(() -> settingFileField.getText().trim(), JsonSettingRepository::new);
	}

	// 상단 입력 패널 (PDF 폴더 / 설정 파일)
	private JPanel buildInputPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, 0, GAP));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// 1행: PDF 폴더
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		panel.add(new JLabel("PDF 폴더"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(pdfFolderField, gbc);
		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton pdfBrowseButton = new JButton("찾아보기");
		pdfBrowseButton.addActionListener(event -> browsePdfFolder());
		panel.add(pdfBrowseButton, gbc);

		// 2행: 설정 파일
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		panel.add(new JLabel("설정 파일"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(settingFileField, gbc);
		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton settingBrowseButton = new JButton("찾아보기");
		settingBrowseButton.addActionListener(event -> browseSettingFile());
		panel.add(settingBrowseButton, gbc);

		return panel;
	}

	// 중앙 로그 패널
	private JComponent buildLogPanel() {
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(logArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("진행 로그"));

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, GAP, 0, GAP));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	// 하단 실행 패널 (진행바 + 변환 버튼)
	private JPanel buildActionPanel() {
		JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
		panel.setBorder(BorderFactory.createEmptyBorder(0, GAP, GAP, GAP));
		panel.add(progressBar, BorderLayout.CENTER);
		runButton.addActionListener(event -> startConvert());
		panel.add(runButton, BorderLayout.EAST);
		return panel;
	}

	// PDF 폴더 선택 다이얼로그
	private void browsePdfFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("PDF 폴더 선택");
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			pdfFolderField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	// 설정 파일(hong.json) 선택 다이얼로그
	private void browseSettingFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("설정 파일(hong.json) 선택");
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			settingFileField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	// 변환 시작 (입력 검증 후 백그라운드 실행)
	private void startConvert() {
		String pdfFolder = pdfFolderField.getText().trim();
		if (StringUtils.isBlank(pdfFolder) || !new File(pdfFolder).isDirectory()) {
			showWarn("올바른 PDF 폴더를 선택하세요.");
			return;
		}

		String settingFile = settingFileField.getText().trim();
		if (StringUtils.isBlank(settingFile) || !new File(settingFile).isFile()) {
			showWarn("설정 파일(hong.json)을 선택하세요.");
			return;
		}

		runButton.setEnabled(false);
		progressBar.setIndeterminate(true);
		logArea.setText("");

		// UI 멈춤 방지를 위해 변환은 백그라운드 스레드에서 실행
		SwingWorker<File, Void> worker = new SwingWorker<File, Void>() {
			@Override
			protected File doInBackground() {
				PdfConvert.setPdfFolderPath(ensureTrailingSeparator(pdfFolder));
				PdfConvert.setSettingFilePath(settingFile);
				PdfConvert.setExcelFileNm(Constants.buildExcelFileName());
				return new PdfConvert().prcssConvert();
			}

			@Override
			protected void done() {
				progressBar.setIndeterminate(false);
				runButton.setEnabled(true);
				try {
					onConvertDone(get());
				} catch (Exception e) {
					log.error("변환 실패", e);
					showError("변환 중 오류가 발생했습니다: " + e.getMessage());
				}
			}
		};
		worker.execute();
	}

	// 변환 완료 처리 (결과 안내 + 폴더 열기)
	private void onConvertDone(File outputFile) {
		if (ObjectUtils.isEmpty(outputFile)) {
			showWarn("결과 파일을 생성하지 못했습니다.");
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this,
				"변환이 완료되었습니다.\n결과: " + outputFile.getAbsolutePath() + "\n\n결과 폴더를 열까요?", "완료",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			openFolder(outputFile.getParentFile());
		}
	}

	// 탐색기로 폴더 열기
	private void openFolder(File folder) {
		try {
			if (Desktop.isDesktopSupported() && ObjectUtils.isNotEmpty(folder)) {
				Desktop.getDesktop().open(folder);
			}
		} catch (Exception e) {
			log.error("폴더 열기 실패", e);
		}
	}

	// 경로 끝에 구분자 보장 (엔진의 경로 결합 규칙과 맞춤)
	private String ensureTrailingSeparator(String path) {
		if (StringUtils.endsWithAny(path, "/", "\\")) {
			return path;
		}
		return path + "/";
	}

	// 로그 한 줄 추가 (EDT 에서 호출)
	private void appendLog(String line) {
		logArea.append(line);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	// 경고 메시지 표시
	private void showWarn(String message) {
		JOptionPane.showMessageDialog(this, message, "확인", JOptionPane.WARNING_MESSAGE);
	}

	// 오류 메시지 표시
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
	}
}
