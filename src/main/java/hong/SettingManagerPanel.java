package hong;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 설정 관리 탭 패널 (세금번호↔업체 매핑 조회/진단 + 추가·수정·삭제·저장)
 * v1: 단순 매핑 + 키워드 분기(TYPE_HONG_01) 편집 / 브랜드+키워드(TYPE_HONG_02)는 보존만(삭제 가능)
 */
@Slf4j
public class SettingManagerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// 8px 그리드 기반 여백
	private static final int GAP = 8;

	// 매핑 테이블 컬럼
	private static final String[] COLUMNS = { "세금번호", "업체", "유형" };

	// 분기 편집 테이블 컬럼
	private static final String[] BRANCH_COLUMNS = { "키워드", "업체" };

	// 유형 콤보 항목
	private static final String TYPE_SIMPLE = "단순";
	private static final String TYPE_BRANCH = "키워드 분기";

	// 현재 설정파일 경로 공급자 (변환 탭의 설정파일 필드와 연동)
	private final transient Supplier<String> settingPathSupplier;

	// 저장소 생성 팩토리 (추후 SQLite 등으로 교체 가능)
	private final transient Function<String, SettingRepository> repositoryFactory;

	// 검증기
	private final transient VatMappingValidator validator = new VatMappingValidator();

	// 현재 저장소 / 작업중 매핑 목록 / 업체 목록
	private transient SettingRepository repository;
	private final transient List<VatMapping> workingMappings = new ArrayList<>();
	private final transient List<String> vendors = new ArrayList<>();

	// 진단 표시 영역
	private final JTextArea diagnoseArea = new JTextArea(4, 40);

	// 매핑 테이블 (읽기 전용)
	private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	private final JTable mappingTable = new JTable(tableModel);

	// 편집 폼 컴포넌트
	private final JTextField vatKeyField = new JTextField(16);
	private final JComboBox<String> typeCombo = new JComboBox<>(new String[] { TYPE_SIMPLE, TYPE_BRANCH });
	private final JComboBox<String> vendorCombo = new JComboBox<>();
	private final JPanel vendorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
	private final DefaultTableModel branchModel = new DefaultTableModel(BRANCH_COLUMNS, 0);
	private final JTable branchTable = new JTable(branchModel);
	private final JPanel branchPanel = new JPanel(new BorderLayout(GAP, GAP));
	private final JLabel statusLabel = new JLabel(" ");

	// 현재 편집 대상 인덱스 (-1: 신규)
	private int selectedIndex = -1;

	// 생성자
	public SettingManagerPanel(Supplier<String> settingPathSupplier,
			Function<String, SettingRepository> repositoryFactory) {
		this.settingPathSupplier = settingPathSupplier;
		this.repositoryFactory = repositoryFactory;
		initUi();
		reload();
	}

	// 화면 구성 (상단 진단 / 중앙 테이블 / 하단 편집폼)
	private void initUi() {
		setLayout(new BorderLayout(GAP, GAP));
		setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
		add(buildDiagnosePanel(), BorderLayout.NORTH);
		add(buildTablePanel(), BorderLayout.CENTER);
		add(buildFormPanel(), BorderLayout.SOUTH);
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
		mappingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mappingTable.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				onSelectRow();
			}
		});
		JScrollPane scrollPane = new JScrollPane(mappingTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder("세금번호 ↔ 업체 매핑"));
		return scrollPane;
	}

	// 편집 폼
	private JPanel buildFormPanel() {
		JPanel form = new JPanel();
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(BorderFactory.createTitledBorder("편집"));

		// 1행: 세금번호 + 유형
		JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
		line1.add(new JLabel("세금번호"));
		line1.add(vatKeyField);
		line1.add(new JLabel("유형"));
		typeCombo.addActionListener(event -> updateTypeVisibility());
		line1.add(typeCombo);
		form.add(line1);

		// 2행: 업체(단순 매핑)
		vendorRow.add(new JLabel("업체"));
		vendorRow.add(vendorCombo);
		form.add(vendorRow);

		// 분기 편집 (키워드→업체)
		branchPanel.setBorder(BorderFactory.createTitledBorder("키워드 분기 (키워드 → 업체)"));
		branchTable.setPreferredScrollableViewportSize(new Dimension(360, 70));
		JScrollPane branchScroll = new JScrollPane(branchTable);
		branchPanel.add(branchScroll, BorderLayout.CENTER);
		JPanel branchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
		JButton branchAdd = new JButton("행 추가");
		branchAdd.addActionListener(event -> branchModel.addRow(new Object[] { "", "" }));
		JButton branchDelete = new JButton("행 삭제");
		branchDelete.addActionListener(event -> removeSelectedBranchRow());
		branchButtons.add(branchAdd);
		branchButtons.add(branchDelete);
		branchPanel.add(branchButtons, BorderLayout.SOUTH);
		form.add(branchPanel);

		// 버튼 줄
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
		JButton addButton = new JButton("추가");
		addButton.addActionListener(event -> onAdd());
		JButton updateButton = new JButton("수정");
		updateButton.addActionListener(event -> onUpdate());
		JButton deleteButton = new JButton("삭제");
		deleteButton.addActionListener(event -> onDelete());
		JButton clearButton = new JButton("선택 해제");
		clearButton.addActionListener(event -> clearForm());
		JButton saveButton = new JButton("저장");
		saveButton.addActionListener(event -> onSave());
		buttons.add(addButton);
		buttons.add(updateButton);
		buttons.add(deleteButton);
		buttons.add(clearButton);
		buttons.add(new JLabel("   "));
		buttons.add(saveButton);
		form.add(buttons);

		// 상태 메시지
		JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
		statusRow.add(statusLabel);
		form.add(statusRow);

		updateTypeVisibility();
		return form;
	}

	// 유형에 따라 업체/분기 입력 토글
	private void updateTypeVisibility() {
		boolean branch = TYPE_BRANCH.equals(typeCombo.getSelectedItem());
		vendorRow.setVisible(!branch);
		branchPanel.setVisible(branch);
		revalidate();
		repaint();
	}

	// 설정파일 재로딩 (저장소 로드 + 진단 + 테이블)
	private void reload() {
		String settingPath = settingPathSupplier.get();
		try {
			repository = repositoryFactory.apply(settingPath);

			vendors.clear();
			vendors.addAll(repository.listVendors());
			refreshVendorCombos();

			workingMappings.clear();
			workingMappings.addAll(repository.listVatMappings());
			refreshTable();
			refreshDiagnose();
			clearForm();

		} catch (Exception e) {
			log.error("설정 로드 실패: {}", settingPath, e);
			diagnoseArea.setText("설정 파일을 불러올 수 없습니다: " + settingPath + "\n" + e.getMessage());
		}
	}

	// 업체 콤보 갱신 (단순 매핑용 + 분기 테이블 셀 에디터용)
	private void refreshVendorCombos() {
		vendorCombo.removeAllItems();
		for (String vendor : vendors) {
			vendorCombo.addItem(vendor);
		}
		JComboBox<String> branchEditor = new JComboBox<>();
		for (String vendor : vendors) {
			branchEditor.addItem(vendor);
		}
		branchTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(branchEditor));
	}

	// 진단 표시 갱신
	private void refreshDiagnose() {
		List<String> issues = repository.diagnose();
		if (ObjectUtils.isEmpty(issues)) {
			diagnoseArea.setText("정상 — 발견된 문제 없음");
		} else {
			diagnoseArea.setText("⚠ 문제 " + issues.size() + "건\n" + String.join("\n", issues));
		}
		diagnoseArea.setCaretPosition(0);
	}

	// 매핑 테이블 갱신
	private void refreshTable() {
		tableModel.setRowCount(0);
		for (VatMapping mapping : workingMappings) {
			tableModel.addRow(new Object[] { mapping.getVatKey(), describeBrand(mapping), typeLabel(mapping) });
		}
	}

	// 테이블 행 선택 시 폼에 로드
	private void onSelectRow() {
		int row = mappingTable.getSelectedRow();
		if (row < 0 || row >= workingMappings.size()) {
			return;
		}
		selectedIndex = row;
		VatMapping mapping = workingMappings.get(row);
		vatKeyField.setText(mapping.getVatKey());

		if (mapping.getMappingType() == MappingType.TYPE_HONG_02) {
			// 보존 전용: 편집 불가, 삭제만 허용
			statusLabel.setText("브랜드+키워드(TYPE_HONG_02) 항목은 v1에서 편집 불가 — 삭제만 가능합니다.");
			typeCombo.setSelectedItem(TYPE_SIMPLE);
			return;
		}

		statusLabel.setText(" ");
		if (mapping.getMappingType() == MappingType.TYPE_HONG_01) {
			typeCombo.setSelectedItem(TYPE_BRANCH);
			branchModel.setRowCount(0);
			for (Map.Entry<String, String> entry : mapping.getBranchEntries().entrySet()) {
				branchModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
			}
		} else {
			typeCombo.setSelectedItem(TYPE_SIMPLE);
			vendorCombo.setSelectedItem(mapping.getBrandId());
		}
		updateTypeVisibility();
	}

	// 추가
	private void onAdd() {
		VatMapping mapping = buildFromForm();
		if (validateAndReport(mapping, workingMappings)) {
			workingMappings.add(mapping);
			refreshTable();
			clearForm();
			statusLabel.setText("추가됨 — [저장]을 눌러야 파일에 반영됩니다.");
		}
	}

	// 수정 (선택된 행)
	private void onUpdate() {
		if (selectedIndex < 0) {
			showWarn("수정할 항목을 먼저 선택하세요.");
			return;
		}
		if (workingMappings.get(selectedIndex).getMappingType() == MappingType.TYPE_HONG_02) {
			showWarn("브랜드+키워드 항목은 v1에서 수정할 수 없습니다.");
			return;
		}
		VatMapping mapping = buildFromForm();
		List<VatMapping> others = new ArrayList<>(workingMappings);
		others.remove(selectedIndex);
		if (validateAndReport(mapping, others)) {
			workingMappings.set(selectedIndex, mapping);
			refreshTable();
			statusLabel.setText("수정됨 — [저장]을 눌러야 파일에 반영됩니다.");
		}
	}

	// 삭제 (선택된 행)
	private void onDelete() {
		if (selectedIndex < 0) {
			showWarn("삭제할 항목을 먼저 선택하세요.");
			return;
		}
		workingMappings.remove(selectedIndex);
		refreshTable();
		clearForm();
		statusLabel.setText("삭제됨 — [저장]을 눌러야 파일에 반영됩니다.");
	}

	// 저장 (백업 + 표준 JSON + 원자교체)
	private void onSave() {
		try {
			repository.saveVatMappings(new ArrayList<>(workingMappings));
			showInfo("저장되었습니다.\n(백업 생성 + 표준 JSON 정규화 완료)");
			reload();
		} catch (Exception e) {
			log.error("저장 실패", e);
			showError("저장 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 폼 입력으로 VatMapping 생성
	private VatMapping buildFromForm() {
		VatMapping mapping = new VatMapping();
		mapping.setVatKey(vatKeyField.getText().trim());

		if (TYPE_BRANCH.equals(typeCombo.getSelectedItem())) {
			mapping.setMappingType(MappingType.TYPE_HONG_01);
			Map<String, String> entries = new LinkedHashMap<>();
			stopBranchEditing();
			for (int i = 0; i < branchModel.getRowCount(); i++) {
				String keyword = String.valueOf(branchModel.getValueAt(i, 0)).trim();
				String brand = String.valueOf(branchModel.getValueAt(i, 1)).trim();
				if (StringUtils.isNotBlank(keyword)) {
					entries.put(keyword, brand);
				}
			}
			mapping.setBranchEntries(entries);
		} else {
			mapping.setMappingType(MappingType.SIMPLE);
			Object selected = vendorCombo.getSelectedItem();
			mapping.setBrandId(selected == null ? StringUtils.EMPTY : selected.toString());
		}
		return mapping;
	}

	// 검증 후 오류 표시 (정상이면 true)
	private boolean validateAndReport(VatMapping target, List<VatMapping> existing) {
		List<String> errors = validator.validate(target, existing, vendors);
		if (ObjectUtils.isNotEmpty(errors)) {
			showWarn(String.join("\n", errors));
			return false;
		}
		return true;
	}

	// 분기 테이블 편집중 셀 확정
	private void stopBranchEditing() {
		if (branchTable.isEditing()) {
			branchTable.getCellEditor().stopCellEditing();
		}
	}

	// 분기 테이블 선택 행 삭제
	private void removeSelectedBranchRow() {
		int row = branchTable.getSelectedRow();
		if (row >= 0) {
			branchModel.removeRow(row);
		}
	}

	// 폼 초기화
	private void clearForm() {
		selectedIndex = -1;
		vatKeyField.setText(StringUtils.EMPTY);
		typeCombo.setSelectedItem(TYPE_SIMPLE);
		branchModel.setRowCount(0);
		statusLabel.setText(" ");
		mappingTable.clearSelection();
		updateTypeVisibility();
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

	// 경고/정보/오류 다이얼로그
	private void showWarn(String message) {
		JOptionPane.showMessageDialog(this, message, "확인", JOptionPane.WARNING_MESSAGE);
	}

	private void showInfo(String message) {
		JOptionPane.showMessageDialog(this, message, "완료", JOptionPane.INFORMATION_MESSAGE);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);
	}
}
