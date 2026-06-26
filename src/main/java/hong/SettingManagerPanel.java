package hong;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 설정 관리 탭 패널 (세금번호↔업체 매핑 조회 + 진단)
 * P2: 매핑 테이블 + 진단 배너 / P3에서 추가·수정·삭제 폼 확장 예정
 */
@Slf4j
public class SettingManagerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// 8px 그리드 기반 여백
	private static final int GAP = 8;

	// 테이블 컬럼
	private static final String[] COLUMNS = { "세금번호", "업체", "유형" };

	// 현재 설정파일 경로 공급자 (변환 탭의 설정파일 필드와 연동)
	private final transient Supplier<String> settingPathSupplier;

	// 저장소 생성 팩토리 (추후 SQLite 등으로 교체 가능)
	private final transient Function<String, SettingRepository> repositoryFactory;

	// 진단 결과 표시 영역
	private final JTextArea diagnoseArea = new JTextArea(4, 40);

	// 매핑 테이블 모델 (읽기 전용)
	private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	// 생성자
	public SettingManagerPanel(Supplier<String> settingPathSupplier,
			Function<String, SettingRepository> repositoryFactory) {
		this.settingPathSupplier = settingPathSupplier;
		this.repositoryFactory = repositoryFactory;
		initUi();
		reload();
	}

	// 화면 구성
	private void initUi() {
		setLayout(new BorderLayout(GAP, GAP));
		setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
		add(buildDiagnosePanel(), BorderLayout.NORTH);
		add(buildTablePanel(), BorderLayout.CENTER);
	}

	// 진단 배너 + 새로고침
	private JPanel buildDiagnosePanel() {
		JPanel panel = new JPanel(new BorderLayout(GAP, GAP));

		diagnoseArea.setEditable(false);
		diagnoseArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(diagnoseArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("진단"));
		panel.add(scrollPane, BorderLayout.CENTER);

		JButton reloadButton = new JButton("새로고침");
		reloadButton.addActionListener(event -> reload());
		panel.add(reloadButton, BorderLayout.EAST);

		return panel;
	}

	// 매핑 테이블
	private JScrollPane buildTablePanel() {
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("세금번호 ↔ 업체 매핑"));
		return scrollPane;
	}

	// 설정파일 재로딩 (진단 + 테이블 갱신)
	private void reload() {
		tableModel.setRowCount(0);
		String settingPath = settingPathSupplier.get();

		try {
			SettingRepository repository = repositoryFactory.apply(settingPath);

			// 진단
			List<String> issues = repository.diagnose();
			if (ObjectUtils.isEmpty(issues)) {
				diagnoseArea.setText("정상 — 발견된 문제 없음");
			} else {
				diagnoseArea.setText("⚠ 문제 " + issues.size() + "건\n" + String.join("\n", issues));
			}
			diagnoseArea.setCaretPosition(0);

			// 테이블 채우기
			for (VatMapping mapping : repository.listVatMappings()) {
				tableModel.addRow(new Object[] { mapping.getVatKey(), describeBrand(mapping), typeLabel(mapping) });
			}

		} catch (Exception e) {
			log.error("설정 로드 실패: {}", settingPath, e);
			diagnoseArea.setText("설정 파일을 불러올 수 없습니다: " + settingPath + "\n" + e.getMessage());
		}
	}

	// 업체 표시 문자열
	private String describeBrand(VatMapping mapping) {
		if (mapping.getMappingType() == MappingType.TYPE_HONG_01) {
			return mapping.getBranchEntries().entrySet().stream()
					.map(entry -> entry.getKey() + "→" + entry.getValue()).collect(Collectors.joining(", "));
		}
		if (mapping.getMappingType() == MappingType.TYPE_HONG_02) {
			return mapping.getBrandId() + " (+키워드)";
		}
		return mapping.getBrandId();
	}

	// 유형 라벨
	private String typeLabel(VatMapping mapping) {
		switch (mapping.getMappingType()) {
		case TYPE_HONG_01:
			return "키워드 분기";
		case TYPE_HONG_02:
			return "브랜드+키워드";
		default:
			return "단순";
		}
	}
}
