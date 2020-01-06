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
import java.util.stream.Stream;

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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.InheritanceUtil;
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
	
	private final List<JCheckBox>                getterCheckboxes = new ArrayList<>();
	private       PsiJavaFile                    dtoFile;
	private       PsiJavaFile                    rpcFile;
	private final JavaCodeStyleManager           codeStyleManager;
	private final PsiDocumentManager             documentManager;
	private final Project                        project;
	private final PsiFileFactory                 psiFileFactory;
	private       Map<String, PsiClass>          allRpcInterfaces;
	private       PsiClass                       selectedRpcInterfaceClass;
	private       List<PsiMethod>                getters;
	private       PsiJavaFile                    boFile;
	private       List<String>                   selectedGetters;
	private       PsiDirectory                   rpcDir;
	private final Map<String, PsiClass>          boMap;  
	private       JBCheckBox                     pkGetter;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSplitPane splitPane;
	private EditorTextField boSourceFile;
	private JCheckBox generateMappersCheckBox;
	private JCheckBox entityTagCheckbox;
	private JCheckBox selectAllGettersCheckBox;
	private JPanel getterPanel;
	private JBTextField nameTextField;
	private EditorTextField rpcImplementation;
	private JBTabbedPane tabbedPane;
	private EditorTextField dtoEditor;
	private EditorTextField rpcEditor;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public DtoGeneratorForm(Project project, Map<String, PsiClass> boMap) {
		super(project, false);
		this.boMap = Collections.unmodifiableMap(boMap);
		this.project = project;
		this.psiFileFactory = PsiFileFactory.getInstance(this.project);
		this.codeStyleManager = JavaCodeStyleManager.getInstance(this.project);
		this.documentManager = PsiDocumentManager.getInstance(this.project);
		initUI();
	}
	
	public DtoGeneratorForm(PsiJavaFile boFile, List<PsiMethod> getters) {
		super(boFile.getProject(), false);
		this.boMap = null;
		this.project = boFile.getProject();
		this.psiFileFactory = PsiFileFactory.getInstance(this.project);
		this.codeStyleManager = JavaCodeStyleManager.getInstance(this.project);
		this.documentManager = PsiDocumentManager.getInstance(this.project);
		this.boFile = boFile;
		this.getters = Collections.unmodifiableList(getters);
		initUI();
	}
	
	public void initUI() {
		setTitle("Create Dto From Bo");
		setResizable(false);
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
						fillNameField();
						fillGetterPanel();
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
				WriteCommandAction.runWriteCommandAction(DtoGeneratorForm.this.project,
				                                         () -> refreshPreview());
			}
		});
	}
	
	private void fillNameField() {
		String dtoName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName())
		                            .replace("BO", "").concat("Dto");
		this.nameTextField.setText(dtoName);
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
				this.pkGetter = checkBox;
			}else {
				if (isBoReturned(getter)) {
					checkBox.setForeground(JBColor.ORANGE);
					checkBox.setToolTipText(
							"<html>Warning this getter return a BO<br>Some TODO will be added to the generated code");
				}
				this.getterCheckboxes.add(checkBox);	
			}
		});
		
		this.getterCheckboxes.forEach(this.getterPanel::add);
	}
	
	private void clearGetterList() {
		this.getterPanel.removeAll();
		this.getterCheckboxes.clear();
	}
	
	private boolean isBoReturned(PsiMethod getterMethod) {
		return InheritanceUtil.isInheritor(getterMethod.getReturnType(),
		                                   "amos.share.databaseAccess.bo.AbstractAmosBusinessObject");
	}
	
	private void refreshPreview() {
		if(this.selectedRpcInterfaceClass != null) {
			this.pkGetter.setSelected(true);
			this.pkGetter.setEnabled(false);
		}else {
			this.pkGetter.setEnabled(true);
		}
		String classContent = "package " + this.boFile.getPackageName()
		                                              .replace("share.bo.", "share.dto.") + ";\n\n"
		                      + "import amos.share.system.transport.rpc.AmosDto;\n"
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
		
		
		addSelectedCheckboxesToDto();
		this.codeStyleManager.shortenClassReferences(this.dtoFile);
		this.dtoEditor.setDocument(this.documentManager.getDocument(this.dtoFile));
		if(this.selectedRpcInterfaceClass != null) {
			this.rpcDir = this.selectedRpcInterfaceClass.getContainingFile().getContainingDirectory();
			addMapperToRpcInterface();
			this.codeStyleManager.shortenClassReferences(this.rpcFile);
			this.rpcEditor.setDocument(this.documentManager.getDocument(this.rpcFile));
		}
	}
	
	private void addMapperToRpcInterface() {
		this.rpcFile  =
				(PsiJavaFile) PsiFileFactory.getInstance(this.project)
				                            .createFileFromText(this.selectedRpcInterfaceClass.getContainingFile().getName(),
			JavaLanguage.INSTANCE,  this.selectedRpcInterfaceClass.getContainingFile().getText());
		String boName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName());
		String dtoName = StringUtils.getInstance().removeJavaEnding(this.dtoFile.getName());
		
		PsiMethod findBy = PsiHelper.getInstance().getFindByIdMethod(this.project, this.boFile.getClasses()[0], this.pkGetter.getText());
		
		
		List<PsiMethod> gettersToInclude = this.getters.stream().filter(getter -> this.selectedGetters
				.contains(getter.getName())).collect(Collectors.toList());
		PsiHelper.getInstance().addStaticInnerMappingClass(this.rpcFile.getClasses()[0], gettersToInclude, 
		                                                   findBy, this.pkGetter.getText() , boName,
		                                                   dtoName, SwissAsStorage.getInstance().getFourLetterCode(),
		                                                   this.entityTagCheckbox.isSelected());
	}
	
	
	private void addSelectedCheckboxesToDto() {
		PsiClass dtoClass = this.dtoFile.getClasses()[0];
		if (this.entityTagCheckbox.isSelected()) {
			PsiHelper.getInstance().addFieldGetterAndSetter(this.project, dtoClass,
			                                                "amos.share.databaseAccess.occ.EntityTag",
			                                                "getEntityTag", false);
		}
		this.selectedGetters = this.getterCheckboxes.stream().filter(
				AbstractButton::isSelected).map(AbstractButton::getText).collect(Collectors.toList());
		for (PsiMethod getter : this.getters) {
			if (this.selectedGetters.contains(getter.getName())) {
				PsiHelper.getInstance().addFieldGetterAndSetter(this.project, dtoClass,
				                                                getter.getReturnType().getCanonicalText(),
				                                                getter.getName(),
				                                                isBoReturned(getter));
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
					                                         () -> refreshPreview());
				}else {
					DtoGeneratorForm.this.selectedRpcInterfaceClass = null;
					DtoGeneratorForm.this.tabbedPane.setEnabledAt(1, false);
				}
			}
		});
		
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
			"[grow,fill]"));

		this.boSourceFile.setBackground(new Color(69, 73, 74));
		panel1.add(this.boSourceFile, "pad 0,cell 0 0,aligny center,grow 100 0");

		this.generateMappersCheckBox.setText("Generate Mappers");
		this.generateMappersCheckBox.addActionListener(e -> generateMappersCheckBoxActionPerformed(e));
		panel1.add(this.generateMappersCheckBox, "cell 0 2,align left center,grow 0 0");

		this.entityTagCheckbox.setText("EntityTag");
		panel1.add(this.entityTagCheckbox, "cell 0 4,align left center,grow 0 0");

		this.selectAllGettersCheckBox.setText("Select all getters");
		panel1.add(this.selectAllGettersCheckBox, "cell 0 5,align left center,grow 0 0");

		getterScrollPane.setBorder(null);

		this.getterPanel.setLayout(new BoxLayout(this.getterPanel, BoxLayout.Y_AXIS));
		getterScrollPane.setViewportView(this.getterPanel);
		panel1.add(getterScrollPane, "cell 0 7,grow");

		this.nameTextField.setToolTipText("Name of the Dto");
		panel1.add(this.nameTextField, "cell 0 1,aligny center,grow 100 0");

		this.rpcImplementation.setBackground(new Color(69, 73, 74));
		panel1.add(this.rpcImplementation, "cell 0 3,aligny center,grow 100 0");

		label1.setFont(label1.getFont().deriveFont(Font.BOLD));
		label1.setText("Getters");
		panel1.add(label1, "cell 0 6,align left center,grow 0 0");
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
		if(!this.nameTextField.getText().isEmpty() && !this.selectedGetters.isEmpty()) { 
			Module shared = Stream.of(ModuleManager.getInstance(this.project).getModules())
			                      .filter(e -> e.getName().contains("shared")).findFirst()
			                      .orElseThrow();
			PsiDirectory dtoDir = PsiHelper.getInstance()
			                               .findOrCreateDirectory(this.project, shared,
			                                                      this.dtoFile.getPackageName());
			PsiHelper.getInstance().addFileInDirectory(dtoDir, this.dtoFile);
			if (this.rpcFile != null) {
				PsiHelper.getInstance().addFileInDirectory(this.rpcDir, this.rpcFile);
			}
		}
	}
}