package com.swissas.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.DocumentEvent;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.swissas.ui.JavaEditorTextField;
import com.swissas.util.PsiHelper;
import com.swissas.util.StringUtils;
import com.swissas.util.SwissAsStorage;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * The form of the BO to DTO generator
 * @author Tavan Alain
 */
public class DtoGeneratorForm extends DialogWrapper {
	
	private final List<JCheckBox>       getterCheckboxes = new ArrayList<>();
	private       PsiJavaFile           dtoFile;
	private       PsiJavaFile           mapperFile;
	private final PsiDocumentManager    documentManager;
	private final Project                   project;
	private final PsiFileFactory            psiFileFactory;
	private       Map<String, PsiClass>     allRpcInterfaces;
	private       PsiClass                  selectedRpcInterfaceClass;
	private       List<PsiMethod>           getters;
	private       PsiJavaFile               boFile;
	private       List<String>              selectedGetters;
	private       PsiDirectory              rpcDir;
	private final Map<String, PsiClass>     boMap;  
	private       JBCheckBox                pkGetter;
	private       String                    ddPkColumn;
	private       Pair<PsiClass, PsiMethod> finder;
	private String lastFileExistCheck;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSplitPane splitPane;
	private EditorTextField boSourceFile;
	private JCheckBox generateMappersCheckBox;
	private JCheckBox entityTagCheckbox;
	private JCheckBox selectAllGettersCheckBox;
	private JPanel getterPanel;
	private JBTextField nameTextField;
	private EditorTextField rpcImplementation;
	public JBTextField getterSearchField;
	private JBTabbedPane tabbedPane;
	private EditorTextField dtoEditor;
	private EditorTextField rpcEditor;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public DtoGeneratorForm(Project project, Map<String, PsiClass> boMap) {
		super(project, false);
		this.boMap = Collections.unmodifiableMap(boMap);
		this.project = project;
		this.psiFileFactory = PsiFileFactory.getInstance(this.project);
		this.documentManager = PsiDocumentManager.getInstance(this.project);
		initUI();
	}
	
	public DtoGeneratorForm(PsiJavaFile boFile, List<PsiMethod> getters) {
		super(boFile.getProject(), false);
		this.boMap = null;
		this.project = boFile.getProject();
		this.psiFileFactory = PsiFileFactory.getInstance(this.project);
		this.documentManager = PsiDocumentManager.getInstance(this.project);
		this.boFile = boFile;
		this.getters = Collections.unmodifiableList(getters);
		initUI();
	}
	
	public void initUI() {
		setTitle("Create Dto From Bo");
		setModal(false);
		setSize(705, 435);
		initComponents();
		initFurtherUIComponents();
		this.entityTagCheckbox.addActionListener(
				e -> WriteCommandAction.runWriteCommandAction(this.project,
				                                              this::refreshPreview));
		this.selectAllGettersCheckBox.addActionListener(e -> {
			    boolean isSelected = this.selectAllGettersCheckBox.isSelected();
			    this.getterCheckboxes.forEach(checkBox -> checkBox.setSelected(isSelected));
				WriteCommandAction.runWriteCommandAction(this.project,
			                                         this::refreshPreview);
		});
		init();
		setOKActionEnabled(false);
	}
	
	@Override
	protected void doOKAction() {
		WriteCommandAction.runWriteCommandAction(this.project,
		                                         this::saveFiles);
		super.doOKAction();
	}
	
	private void initFurtherUIComponents() {
		if(this.boMap != null) {
			this.boSourceFile.setVisible(true);
			this.boSourceFile.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void documentChanged(
						@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
					if(DtoGeneratorForm.this.boMap
							.containsKey(DtoGeneratorForm.this.boSourceFile.getText())){
						
						PsiClass psiClass = DtoGeneratorForm.this.boMap
								.get(DtoGeneratorForm.this.boSourceFile.getText());
						DtoGeneratorForm.this.boFile = (PsiJavaFile) psiClass.getContainingFile();
						DtoGeneratorForm.this.getters = PsiHelper.getInstance()
						                                         .getGettersForPsiClass(psiClass);
						fillGetterPanel();
						fillNameField();
					}else if(DtoGeneratorForm.this.boSourceFile.getText().isEmpty()){
						DtoGeneratorForm.this.nameTextField.setText("");
						clearGetterList();
					}
					getContentPanel().revalidate();
					
				}
			});
		}else {
			fillNameField();
			this.boSourceFile.setVisible(false);
			fillGetterPanel();
		}
		this.nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				revalidateNameTextField();
				WriteCommandAction.runWriteCommandAction(DtoGeneratorForm.this.project,
					                                         DtoGeneratorForm.this::refreshPreview);
			}
		});
		this.getterSearchField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				refreshGetterPanel();
			}
		});
	}
	
	private void revalidateNameTextField() {
		ComponentValidator.getInstance(this.nameTextField).ifPresent(
				ComponentValidator::revalidate);
	}
	
	private void fillNameField() {
		String dtoName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName())
		                            .replace("BO", "").concat("Dto");
		installValidator();
		this.nameTextField.setText(dtoName);
		revalidateNameTextField();
		setTitle("Create Dto for " + dtoName);
	}
	
	private void fillGetterPanel() {
		clearGetterList();
		this.getters.forEach(getter -> {
			JBCheckBox checkBox = new JBCheckBox(getter.getName());
			checkBox.addActionListener(
					e -> WriteCommandAction.runWriteCommandAction(this.project,
					                                              this::refreshPreview));
			if(PsiHelper.getInstance().isPrimaryGetter(getter)) {
				checkBox.setForeground(JBColor.GREEN);
				checkBox.setToolTipText("PrimaryKey getter (mandatory in case of mapping)");
				this.getterPanel.add(checkBox);
				this.getterCheckboxes.add(0, checkBox);
				this.ddPkColumn = PsiHelper.getInstance().getDDPkForPsiClass(getter);
				this.pkGetter = checkBox;
			}else {
				if (PsiHelper.getInstance().isBoReturned(getter)) {
					checkBox.setForeground(JBColor.ORANGE);
					checkBox.setToolTipText(
							"<html>Warning this getter return a BO<br>Some TODO will be added to the generated code");
				}
				this.getterCheckboxes.add(checkBox);	
			}
		});
		refreshGetterPanel();
	}
	
	private void refreshGetterPanel() {
		this.getterPanel.removeAll();
		for (JCheckBox checkbox : this.getterCheckboxes) {
			if(this.getterSearchField.getText().isEmpty() || checkbox.getText()
			                      .toLowerCase().contains(this.getterSearchField.getText().toLowerCase())) {
				this.getterPanel.add(checkbox);
			}
		}
		this.getterPanel.validate();
		this.getterPanel.repaint();
	}
	
	private void clearGetterList() {
		this.getterPanel.removeAll();
		this.getterCheckboxes.clear();
	}
	
	
	private void refreshPreview() {
		enablePkGetterIfNeeded();
		String classContent = "package " + this.boFile.getPackageName()
		                                              .replace("server.bo.", "share.dto.") + ";\n\n"
		                      + "import amos.share.system.transport.rpc.AmosDto;\n"
		                      + "import java.util.Objects;\n"
		                      + "/**\n"
		                      + " * Auto generated DTO for " + StringUtils.getInstance()
		                                                                  .removeJavaEnding(
				                                                                  this.boFile
						                                                                  .getName())
		                      + "\n"
		                      + " *\n"
		                      + " * @author " + SwissAsStorage.getInstance().getFourLetterCode()
		                      + "\n"
		                      + " */\n\n"
		                      + "public class " + this.nameTextField.getText()
		                      + " implements AmosDto {"
		                      + "}";
		
		this.dtoFile = (PsiJavaFile) this.psiFileFactory
		                                           .createFileFromText(
				                                           this.nameTextField.getText() + ".java",
				                                           JavaLanguage.INSTANCE, classContent);
		PsiClass dtoClass = this.dtoFile.getClasses()[0];
		PsiClass boClass = this.boFile.getClasses()[0];
		this.finder = PsiHelper.getInstance().getFinderClassAndLastFinder(
				this.project, boClass);
		addSelectedCheckboxesToDto(dtoClass);
		generateEqualsAndHashcodeIfNeeded(dtoClass);
		cleanAndApplyFileToEditor(this.dtoFile, this.dtoEditor);
		if(this.selectedRpcInterfaceClass == null){
			this.rpcEditor.setText("");
		}else {
			this.rpcDir = this.selectedRpcInterfaceClass.getContainingFile().getContainingDirectory();
			generateMapper();
			this.mapperFile.importClass(boClass);
			this.mapperFile.importClass(this.finder.getFirst());
			cleanAndApplyFileToEditor(this.mapperFile, this.rpcEditor);
		}
		setOKActionEnabled(!this.selectedGetters.isEmpty() && !this.nameTextField.getText().isEmpty() && !checkDtoNameExists());
	}
	
	
	private void cleanAndApplyFileToEditor(@NotNull PsiJavaFile fileToCleanup, @NotNull EditorTextField editorToRefresh) {
		fileToCleanup = PsiHelper.getInstance().applyCodeStyleAndReformatImports(fileToCleanup);
		editorToRefresh.setDocument(this.documentManager.getDocument(fileToCleanup));
	}
	
	private void enablePkGetterIfNeeded() {
		if(this.pkGetter != null) {
			if (this.selectedRpcInterfaceClass != null) {
				this.pkGetter.setSelected(true);
				this.pkGetter.setEnabled(false);
			} else {
				this.pkGetter.setEnabled(true);
			}
		}
	}
	
	private void generateEqualsAndHashcodeIfNeeded(PsiClass dtoClass) {
		if(this.pkGetter != null && this.pkGetter.isSelected()) {
			dtoClass.add(PsiHelper.getInstance()
			                      .generateEquals(this.project, this.nameTextField.getText(),
			                                      this.pkGetter.getText()));
			dtoClass.add(PsiHelper.getInstance()
			                      .generateHashcode(this.project, this.pkGetter.getText()));
		}
	}
	
	private boolean checkDtoNameExists() {
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(this.project);
		String name = this.boFile.getPackageName()
		                         .replace("server.bo.", "share.dto.") + "." +
		              this.nameTextField.getText();
		if(!name.equals(this.lastFileExistCheck)) {
			this.lastFileExistCheck = name;
			return javaPsiFacade.findClass(name, GlobalSearchScope.allScope(this.project)) != null;
		}
		return false;
	}
	
	private void installValidator() {
		new ComponentValidator(this.project).withValidator(() -> {
			if(checkDtoNameExists()) {
				return new ValidationInfo("A Dto with the same name already exists", this.nameTextField);
			}
			return null;
		}).installOn(this.nameTextField);
	}
	
	private void generateMapper() {
		String boName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName());
		String dtoName = StringUtils.getInstance().removeJavaEnding(this.dtoFile.getName());
		String ddColumnImport = this.ddPkColumn == null ? "" :
		                        "import amos.server.databaseAccess.tables." + this.ddPkColumn.split("\\.")[0] + ";\n";
		String boFinderClass = this.finder.getFirst() == null ? "" : "import " + this.finder.getFirst().getQualifiedName() + ";\n";
		String mapperClassContent = this.selectedRpcInterfaceClass.getContainingFile().getFirstChild().getText() + "\n\n"
		                            + "import amos.server.databaseAccess.api.AmosTransaction;\n"
		                            + "import amos.server.databaseAccess.bo.DefaultBOList;\n"
		                            + "import amos.share.util.ListUtils;\n"
		                            + "\n"
		                            + "import javax.validation.constraints.NotNull;\n"
		                            + "import java.util.Collections;\n"
		                            + "import java.util.List;\n"
		                            + "import java.util.ArrayList;\n"
		                            + "import java.util.Map;\n"
		                            + "import java.util.function.Function;\n"
		                            + "import java.util.stream.Collectors;\n"
		                            + ddColumnImport
		                            + "import " + this.dtoFile.getClasses()[0].getQualifiedName() + ";\n"
		                            + boFinderClass
									+ "import static amos.server.sol.util.SolHelperFunctions.IN;\n";
		
		this.mapperFile =
				(PsiJavaFile) PsiFileFactory.getInstance(this.project)
				                            .createFileFromText(dtoName + "Mapper.java", JavaLanguage.INSTANCE, mapperClassContent);

		List<PsiMethod> gettersToInclude = this.getters.stream().filter(getter -> this.selectedGetters
				.contains(getter.getName())).collect(Collectors.toList());
		String getterName = this.pkGetter == null ? null : this.pkGetter.getText();
		PsiHelper.getInstance().addMapplingClass(this.mapperFile, gettersToInclude,
		                                         getterName,
		                                         this.finder, boName, dtoName,
		                                         this.entityTagCheckbox.isSelected());
	}
	
	
	private void addSelectedCheckboxesToDto(PsiClass dtoClass) {
		if (this.entityTagCheckbox.isSelected()) {
			PsiHelper.getInstance().addEntityTag(this.project, dtoClass);
		}
		this.selectedGetters = this.getterCheckboxes.stream().filter(
				AbstractButton::isSelected).map(AbstractButton::getText).collect(Collectors.toList());
		for (PsiMethod getter : this.getters) {
			if (this.selectedGetters.contains(getter.getName())) {
				PsiHelper.getInstance().addFieldGetterAndSetterFromPsiMethod(getter, dtoClass);
			}
		}
	}
	
	private void createUIComponents() {
		// TODO: place custom component creation code here
		this.getterPanel = new JPanel();
		this.getterPanel.setLayout(new BoxLayout(this.getterPanel, BoxLayout.Y_AXIS));
		this.allRpcInterfaces = PsiHelper.getInstance().getRpcImplementationMapForProjectUp(this.project);
		Set<String> rpcNames = this.allRpcInterfaces.keySet();
		Set<String> boNames = this.boMap == null ? Collections.emptySet() : this.boMap.keySet();
		this.rpcImplementation = TextFieldWithAutoCompletion.create(this.project, rpcNames, false, null);
		this.rpcImplementation.setToolTipText("Type the RPC implementation where the mapper will be added");
		this.rpcImplementation.setEnabled(false);
		this.boSourceFile = TextFieldWithAutoCompletion.create(this.project, boNames, null, false, null);
		this.boSourceFile.setToolTipText("Type the BO used to create a DTO");
		this.rpcImplementation.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void documentChanged(
					@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
				String selectedValue = DtoGeneratorForm.this.rpcImplementation.getText();
				if(rpcNames.contains(selectedValue)){
					DtoGeneratorForm.this.selectedRpcInterfaceClass = DtoGeneratorForm.this.allRpcInterfaces
							.get(selectedValue);
					DtoGeneratorForm.this.tabbedPane.setEnabledAt(1, true);
					WriteCommandAction.runWriteCommandAction(DtoGeneratorForm.this.project,
					                                         DtoGeneratorForm.this::refreshPreview);
				}else {
					DtoGeneratorForm.this.selectedRpcInterfaceClass = null;
					DtoGeneratorForm.this.tabbedPane.setEnabledAt(1, false);
				}
			}
		});
		this.getterSearchField = new JBTextField();
		this.getterSearchField.getEmptyText().setText("getter search filter");
	}

	private void generateMappersCheckBoxActionPerformed(ActionEvent e) {
		if(this.generateMappersCheckBox.isSelected()) {
			this.rpcImplementation.setEnabled(true);
		}else {
			this.rpcImplementation.setText("");
			this.rpcImplementation.setEnabled(false);
		}
		WriteCommandAction.runWriteCommandAction(this.project,
		                                         this::refreshPreview);
	}
	

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		createUIComponents();

		this.splitPane = new JSplitPane();
		var panel1 = new JPanel();
		this.generateMappersCheckBox = new JCheckBox();
		this.entityTagCheckbox = new JCheckBox();
		this.selectAllGettersCheckBox = new JCheckBox();
		var getterScrollPane = new JBScrollPane();
		this.nameTextField = new JBTextField();
		var label1 = new JLabel();
		this.tabbedPane = new JBTabbedPane();
		this.dtoEditor = new JavaEditorTextField(this.project);
		this.dtoEditor.setOneLineMode(false);
		this.rpcEditor = new JavaEditorTextField(this.project);
		this.rpcEditor.setOneLineMode(false);

		this.splitPane.setDividerLocation(154);
		this.splitPane.setDividerSize(0);
		this.splitPane.setMaximumSize(new Dimension(682, 364));
		this.splitPane.setDoubleBuffered(true);
		this.splitPane.setBorder(null);

		panel1.setLayout(new MigLayout(
			"insets 0,hidemode 3,gap 10 5",
			// columns
			"[grow,fill]",
			// rows
			"[]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[fill]" +
			"[]" +
			"[grow,fill]"));

		this.boSourceFile.setBackground(new Color(69, 73, 74));
		panel1.add(this.boSourceFile, "pad 0,cell 0 0,aligny center,grow 100 0");

		this.generateMappersCheckBox.setText("Generate Mappers");
		this.generateMappersCheckBox.addActionListener(this::generateMappersCheckBoxActionPerformed);
		panel1.add(this.generateMappersCheckBox, "cell 0 2,align left center,grow 0 0");

		this.entityTagCheckbox.setText("EntityTag");
		panel1.add(this.entityTagCheckbox, "cell 0 4,align left center,grow 0 0");

		this.selectAllGettersCheckBox.setText("Select all getters");
		panel1.add(this.selectAllGettersCheckBox, "cell 0 5,align left center,grow 0 0");

		getterScrollPane.setBorder(null);

		this.getterPanel.setLayout(new BoxLayout(this.getterPanel, BoxLayout.Y_AXIS));
		getterScrollPane.setViewportView(this.getterPanel);
		panel1.add(getterScrollPane, "cell 0 8,grow");

		this.nameTextField.setToolTipText("Name of the Dto");
		panel1.add(this.nameTextField, "cell 0 1,aligny center,grow 100 0");

		this.rpcImplementation.setBackground(new Color(69, 73, 74));
		panel1.add(this.rpcImplementation, "cell 0 3,aligny center,grow 100 0");

		label1.setFont(label1.getFont().deriveFont(Font.BOLD));
		label1.setText("Getters");
		panel1.add(label1, "cell 0 6,align left center,grow 0 0");
		panel1.add(this.getterSearchField, "cell 0 7");
		this.splitPane.setLeftComponent(panel1);

		this.tabbedPane.setAutoscrolls(true);
		this.tabbedPane.setTabComponentInsets(new Insets(0, 0, 0, 0));
		this.tabbedPane.setBorder(null);

		this.dtoEditor.setPreferredSize(null);
		this.dtoEditor.setMinimumSize(null);
		this.dtoEditor.setMaximumSize(null);
		this.dtoEditor.setInheritsPopupMenu(true);
		this.dtoEditor.setBorder(BorderFactory.createEmptyBorder());
		this.tabbedPane.addTab("dto", this.dtoEditor);

		this.rpcEditor.setPreferredSize(new Dimension(520, 330));
		this.rpcEditor.setMinimumSize(new Dimension(520, 330));
		this.rpcEditor.setAutoscrolls(true);
		this.rpcEditor.setBorder(null);
		this.tabbedPane.addTab("mapper", this.rpcEditor);
		this.splitPane.setRightComponent(this.tabbedPane);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	
	
	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return this.splitPane;
	}
	
	public void saveFiles() {
		String boName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName());
		PsiDirectory dtoDir = PsiHelper.getInstance()
		                       .findOrCreateDirectoryInShared(this.project,	this.dtoFile.getPackageName());
		if(this.pkGetter != null) {
			PsiHelper.getInstance().generateFindByIdsIfNeeded(this.project, this.finder, boName,
			                                                  this.pkGetter.getText(),
			                                                  this.ddPkColumn);
		}
		PsiHelper.getInstance().addFileInDirectory(dtoDir, this.dtoFile);
		if(this.selectedRpcInterfaceClass != null) {
			PsiHelper.getInstance().addFileInDirectory(this.rpcDir, this.mapperFile);
		}
	}
}