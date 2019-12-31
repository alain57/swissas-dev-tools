package com.swissas.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.Query;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.swissas.util.StringUtils;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;


/**
 * The form of the BO to DTO generator
 * @author Tavan Alain
 */
public class DtoGeneratorForm extends JDialog {
	
	private final List<JCheckBox>                 getterCheckboxes = new ArrayList<>();
	private final List<PsiMethod>                 getters;
	private final PsiJavaFile                     boFile;
	private       PsiJavaFile                     dtoFile;
	private final SmartPsiElementPointer<PsiFile> javaPsiPointer;
	private final PsiDocumentManager              documentManager;
	private final Project                         project;
	private       Map<String, PsiClass>           allRpcInterfaces;
	private       PsiClass                        selectedRpcInterface;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel contentPane;
	private JCheckBox generateMappersCheckBox;
	private JCheckBox entityTagCheckbox;
	private JCheckBox selectAllGettersCheckBox;
	private JPanel getterPanel;
	private JBTextField nameTextField;
	private EditorTextField rpcImplementation;
	private JTabbedPane tabbedPane;
	private EditorTextField editor;
	private EditorTextField rpcEditor;
	private JButton buttonOK;
	private JButton buttonCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public DtoGeneratorForm(Project project, PsiJavaFile boFile, List<PsiMethod> getters) {
		this.project = project;
		this.javaPsiPointer = SmartPointerManager.getInstance(this.project)
		                                         .createSmartPsiElementPointer(boFile);
		this.documentManager = PsiDocumentManager.getInstance(this.project);
		this.boFile = boFile;
		this.getters = Collections.unmodifiableList(getters);
		initComponents();
		setContentPane(this.contentPane);
		setModal(true);
		setTitle("Create Dto From Bo");
		getRootPane().setDefaultButton(this.buttonOK);
		
		this.buttonOK.addActionListener(e -> onOK());
		
		this.buttonCancel.addActionListener(e -> onCancel());
		
		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		
		// call onCancel() on ESCAPE
		this.contentPane.registerKeyboardAction(e -> onCancel(),
		                                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
		                                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		initFurtherUIComponents();
		this.entityTagCheckbox.addActionListener(
				e -> WriteCommandAction.runWriteCommandAction(this.project,
				                                              this::refreshPreview));
		this.selectAllGettersCheckBox.addActionListener(e ->
				                                                this.getterCheckboxes.forEach(
						                                                checkBox -> checkBox
								                                                .setSelected(
										                                                this.selectAllGettersCheckBox
												                                                .isSelected())));
	}
	
	private void onOK() {
		// add your code here
		dispose();
	}
	
	private void onCancel() {
		dispose();
	}
	
	private void initFurtherUIComponents() {
		String dtoName = StringUtils.getInstance().removeJavaEnding(this.boFile.getName())
		                            .replace("BO", "").concat("Dto");
		this.nameTextField.setText(dtoName);
		this.nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				WriteCommandAction.runWriteCommandAction(DtoGeneratorForm.this.project,
				                                         () -> refreshPreview());
			}
		});
		this.editor.setFileType(StdFileTypes.JAVA);
		this.rpcEditor.setFileType(StdFileTypes.JAVA);
		this.getters.forEach(getter -> {
			JBCheckBox checkBox = new JBCheckBox(getter.getName());
			checkBox.addActionListener(
					e -> WriteCommandAction.runWriteCommandAction(this.project,
					                                              this::refreshPreview));
			if (isBoReturned(getter)) {
				checkBox.setForeground(JBColor.ORANGE);
				checkBox.setToolTipText(
						"<html>Warning this getter return a BO<br>Some TODO will be added to the generated code");
			}
			this.getterPanel.add(checkBox);
			this.getterCheckboxes.add(checkBox);
		});
	}
	
	private boolean isBoReturned(PsiMethod getterMethod) {
		return InheritanceUtil.isInheritor(getterMethod.getReturnType(),
		                                   "amos.share.databaseAccess.bo.AbstractAmosBusinessObject");
	}
	
	private void refreshPreview() {
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
		
		this.dtoFile = (PsiJavaFile) PsiFileFactory.getInstance(this.project)
		                                           .createFileFromText(
				                                           this.nameTextField.getText() + ".java",
				                                           JavaLanguage.INSTANCE, classContent);
		this.editor.setDocument(this.documentManager.getDocument(this.dtoFile));
		if(this.selectedRpcInterface != null) {
			this.tabbedPane.setEnabledAt(1, true);
			this.rpcEditor.setDocument(this.documentManager.getDocument(this.selectedRpcInterface.getContainingFile()));
		}else {
			this.tabbedPane.setEnabledAt(1, false);
		}
		addSelectedCheckboxesToDto();
	}
	
	private void addSelectedCheckboxesToDto() {
		if (this.entityTagCheckbox.isSelected()) {
			addField("EntityTag", "getEntityTag", false);
		}
		List<String> selectedGetters = this.getterCheckboxes.stream().filter(
				AbstractButton::isSelected)
		                                                    .map(AbstractButton::getText)
		                                                    .collect(Collectors.toList());
		for (PsiMethod getter : this.getters) {
			if (selectedGetters.contains(getter.getName())) {
				addField(getter.getReturnType().getCanonicalText(), getter.getName(),
				         isBoReturned(getter));
				
			}
		}
		JavaCodeStyleManager.getInstance(this.project)
		                    .shortenClassReferences(this.dtoFile);
		PsiDocumentManager.getInstance(this.project)
		                  .doPostponedOperationsAndUnblockDocument(this.editor.getDocument());
	}
	
	
	private void addField(String objectType, String getterName, boolean addTodo) {
		String variable = StringUtils.getInstance().removeGetterPrefix(getterName);
		
		PsiElementFactory factory = JavaPsiFacade.getElementFactory(this.project);
		PsiComment todoComment = factory
				.createCommentFromText("//TODO: remove the BO here !!!", null);
		PsiField field = factory
				.createFieldFromText("private " + objectType + " " + variable + ";", null);
		if (addTodo) {
			field.add(todoComment);
		}
		PsiClass parent = this.dtoFile.getClasses()[0];
		
		parent.add(field);
		
		String setterName = "set" + variable.substring(0, 1).toUpperCase() + variable.substring(1);
		PsiMethod getter = factory
				.createMethodFromText("public " + objectType + " " + getterName + "() {"
				                      + "return " + variable + ";"
				                      + "}", null);
		PsiMethod setter = factory.createMethodFromText("public void " + setterName + "("
		                                                + objectType + " " + variable + "){"
		                                                + "this." + variable + " = " + variable
		                                                + ";"
		                                                + "}", null);
		if (addTodo) {
			getter.add(todoComment);
			setter.add(todoComment);
		}
		parent.add(getter);
		parent.add(setter);
	}
	
	public static DtoGeneratorForm showDialog(Project project, PsiJavaFile boFile,
	                                          List<PsiMethod> getters) {
		DtoGeneratorForm dialog = new DtoGeneratorForm(project, boFile, getters);
		dialog.setPreferredSize(new Dimension(600, 500));
		dialog.refreshPreview();
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		return dialog;
	}
	
	private void createUIComponents() {
		// TODO: place custom component creation code here
		this.getterPanel = new JPanel();
		this.getterPanel.setLayout(new BoxLayout(this.getterPanel, BoxLayout.Y_AXIS));
		JavaPsiFacade instance = JavaPsiFacade.getInstance(this.project);
		PsiPackage aPackage = instance.findPackage("amos.share.system.transport.rpc");
		PsiClass rpcInterface = Stream.of(aPackage.getClasses())
		                        .filter(psiClass -> "RpcInterface".equals(psiClass.getName()))
		                        .findFirst().orElseThrow();
		Query<PsiClass> search = ClassInheritorsSearch
				.search(rpcInterface, GlobalSearchScope.projectScope(this.project), true);
		this.allRpcInterfaces = search.findAll().stream()
		                              .filter(e -> !e.isInterface())
		                              .collect(Collectors.toMap(PsiNamedElement::getName, Function
				                              .identity()));
		List<String> rpcNames = this.allRpcInterfaces.values().stream().map(PsiNamedElement::getName)
		                                             .collect(Collectors.toList());
		StringsCompletionProvider rpcInterfaceCompletionProvider = new StringsCompletionProvider(rpcNames, null);
		
		this.rpcImplementation = new TextFieldWithCompletion(this.project, rpcInterfaceCompletionProvider
		,"", false, true, true,  false);
		this.rpcImplementation.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void documentChanged(
					@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
				String selectedValue = DtoGeneratorForm.this.rpcImplementation.getText();
				if(rpcNames.contains(selectedValue)){
					DtoGeneratorForm.this.selectedRpcInterface = DtoGeneratorForm.this.allRpcInterfaces
							.get(selectedValue);
				}
			}
		});
		
		//VirtualFile virtualFile = LocalFileSystem.getInstance().f.findFileByPath("amos.share.system.transport.rpc.RpcInterface");
		
	}

	private void generateMappersCheckBoxActionPerformed(ActionEvent e) {
		if(this.generateMappersCheckBox.isSelected()) {
			this.rpcImplementation.setEnabled(true);
		}else {
			this.rpcImplementation.setText("");
			this.rpcImplementation.setEnabled(false);
		}
	}
	

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		createUIComponents();

		this.contentPane = new JPanel();
		var splitPane1 = new JSplitPane();
		var panel1 = new JPanel();
		this.generateMappersCheckBox = new JCheckBox();
		this.entityTagCheckbox = new JCheckBox();
		this.selectAllGettersCheckBox = new JCheckBox();
		var scrollPane1 = new JScrollPane();
		this.nameTextField = new JBTextField();
		var label1 = new JLabel();
		this.tabbedPane = new JTabbedPane();
		var panel2 = new JPanel();
		this.editor = new EditorTextField();
		var panel3 = new JPanel();
		this.rpcEditor = new EditorTextField();
		var panel4 = new JPanel();
		var hSpacer1 = new Spacer();
		var panel5 = new JPanel();
		this.buttonOK = new JButton();
		this.buttonCancel = new JButton();

		{
			this.contentPane.setMinimumSize(new Dimension(234, 267));
			this.contentPane.setPreferredSize(new Dimension(234, 267));
			this.contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));

			{
				splitPane1.setDividerLocation(154);
				splitPane1.setDividerSize(0);

				{
					panel1.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));

					this.generateMappersCheckBox.setText("Generate Mappers");
					this.generateMappersCheckBox.addActionListener(
							this::generateMappersCheckBoxActionPerformed);
					panel1.add(this.generateMappersCheckBox, new GridConstraints(1, 0, 1, 1,
						GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					this.entityTagCheckbox.setText("EntityTag");
					panel1.add(this.entityTagCheckbox, new GridConstraints(3, 0, 1, 1,
						GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					this.selectAllGettersCheckBox.setText("Select all getters");
					panel1.add(this.selectAllGettersCheckBox, new GridConstraints(4, 0, 1, 1,
						GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					{

						{
							this.getterPanel.setLayout(new BoxLayout(this.getterPanel, BoxLayout.Y_AXIS));
						}
						scrollPane1.setViewportView(this.getterPanel);
					}
					panel1.add(scrollPane1, new GridConstraints(6, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						null, null, null));

					this.nameTextField.setToolTipText("Name of the Dto");
					panel1.add(this.nameTextField, new GridConstraints(0, 0, 1, 1,
						GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					this.rpcImplementation.setEnabled(false);
					panel1.add(this.rpcImplementation, new GridConstraints(2, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						null, null, null));

					label1.setFont(label1.getFont().deriveFont(Font.BOLD));
					label1.setText("Getters");
					panel1.add(label1, new GridConstraints(5, 0, 1, 1,
						GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_FIXED,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));
				}
				splitPane1.setLeftComponent(panel1);

				{

					{
						panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
						panel2.add(this.editor, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							null, null, null));
					}
					this.tabbedPane.addTab("Dto", panel2);

					{
						panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
						panel3.add(this.rpcEditor, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							null, null, null));
					}
					this.tabbedPane.addTab("Mapper", panel3);
				}
				splitPane1.setRightComponent(this.tabbedPane);
			}
			this.contentPane.add(splitPane1, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				null, new Dimension(200, 200), null));

			{
				panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
				panel4.add(hSpacer1, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
					GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK,
					null, null, null));

				{
					panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));

					this.buttonOK.setText("OK");
					panel5.add(this.buttonOK, new GridConstraints(0, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					this.buttonCancel.setText("Cancel");
					panel5.add(this.buttonCancel, new GridConstraints(0, 1, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));
				}
				panel4.add(panel5, new GridConstraints(0, 1, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			this.contentPane.add(panel4, new GridConstraints(1, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK,
				null, null, null));
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	
}
