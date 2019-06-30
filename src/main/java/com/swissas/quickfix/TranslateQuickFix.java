package com.swissas.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SequenceProperties;
import groovy.json.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Quickfix for translating normal keys
 *
 * @author Tavan Alain
 */

public class TranslateQuickFix implements LocalQuickFix {

    private static final String MESSAGE_CLASS = "_Messages.java";

    private SmartPsiElementPointer<PsiFile> smartPsiElementPointer;
    private String currentPropertiesPath;
    String ending;
    String className;

    public TranslateQuickFix(PsiFile file){
        this.smartPsiElementPointer = SmartPointerManager.getInstance(file.getProject()).createSmartPsiElementPointer(file);
        this.ending = "_TXT";
        this.className = "MultiLangText";
    }

    @NonNls
    protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return RESOURCE_BUNDLE.getString("swiss.as");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return RESOURCE_BUNDLE.getString("translate");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        Properties properties = getOrCreateProperties();
        PsiFile currentTranslationJavaFile = getOrCreateMessageFile();
        PsiElement element = descriptor.getPsiElement();
        String propertyValue = getPropertyValue(element);
        String fullKey;
        if(properties.values().contains(propertyValue)){
            fullKey = properties.entrySet().stream().filter(entry -> propertyValue.equals(entry.getValue())).findFirst().map(Map.Entry::getKey).get().toString();
        }else {
            String translatedKey = getPropertyKey(element);
            int numberInCaseOfDuplicateKey = 0;
            fullKey = translatedKey + this.ending;
            while(properties.containsKey(fullKey)) {
                numberInCaseOfDuplicateKey++;
                fullKey = translatedKey + "_" + numberInCaseOfDuplicateKey + this.ending;
            }
            properties.put(fullKey, getPropertyValue(element));
            saveProperties(properties);
            PsiElement javaTranslation = JavaPsiFacade.getElementFactory(project).createFieldFromText("static final " + this.className + " " + fullKey + " = new " + this.className + "(INSTANCE);", null);
            PsiField latestField = PsiTreeUtil.collectElementsOfType(currentTranslationJavaFile, PsiField.class).stream().reduce((a, b) -> b).get();
            latestField.getParent().addAfter(javaTranslation, latestField);
        }


        StringBuilder replacement = new StringBuilder(fullKey);
        if(element instanceof PsiPolyadicExpression) {
            List<PsiElement> elementsToFormat = Stream.of(element.getChildren()).filter(e ->
                    !(e instanceof PsiWhiteSpace) &&
                            !(e instanceof PsiJavaToken) &&
                            !(e instanceof PsiLiteralExpressionImpl && ((PsiLiteralExpressionImpl)e).getLiteralElementType().equals(JavaTokenType.STRING_LITERAL))
            ).collect(Collectors.toList());
            replacement.append(".format(");
            String collect = elementsToFormat.stream().map(PsiElement::getText).collect(Collectors.joining(", "));
            replacement.append(collect);
            replacement.append(")");
        }
        PsiExpression expressionFromText = JavaPsiFacade.getElementFactory(this.smartPsiElementPointer.getProject()).createExpressionFromText(replacement.toString(), null);
        element.replace(expressionFromText);
    }

    protected PsiFile getOrCreateMessageFile(){
        PsiDirectory containingDirectory = this.smartPsiElementPointer.getElement().getContainingDirectory();
        PsiFile messageFile = containingDirectory.findFile(MESSAGE_CLASS);
        if(messageFile == null){
            String classContent = "import amos.share.multiLanguage." + this.className + ";\n" +
                    "import amos.share.multiLanguage.Translateable;\n" +
                    "\n" +
                    "/*****************************************************************************\n" +
                    " * Container for all translateable messages inside this package.\n" +
                    " *\n" +
                    " * @author  AUTO\n" +
                    " ****************************************************************************/\n" +
                    "class _Messages extends Translateable {\n" +
                    "\tprivate static final _Messages INSTANCE = new _Messages();\n\n" +
                    "\tstatic {\n" +
                    "\t\tINSTANCE.init();\n" +
                    "\t}\n" +
                    "}";
            messageFile = PsiFileFactory.getInstance(this.smartPsiElementPointer.getProject()).createFileFromText(MESSAGE_CLASS, JavaLanguage.INSTANCE, classContent);
            containingDirectory.add(messageFile);
        }
        return messageFile;
    }

    private Properties getOrCreateProperties(){
        Properties prop = new SequenceProperties();
        try {
            this.currentPropertiesPath = this.smartPsiElementPointer.getElement().getContainingDirectory().getVirtualFile().findOrCreateChildData(this, "Standard.properties").getPath();
            FileInputStream in = new FileInputStream(this.currentPropertiesPath);
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    private void saveProperties(Properties properties){
        try {
            FileOutputStream out = new FileOutputStream(this.currentPropertiesPath);
            properties.store(out, null);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getPropertyValue(PsiElement element){
        String result;
        if(element instanceof PsiPolyadicExpression){
            result = PsiTreeUtil.collectElementsOfType(element, PsiLiteralExpressionImpl.class).stream().map(e ->
                    e.getLiteralElementType().equals(JavaTokenType.STRING_LITERAL)  ? e.getInnerText() : ""
            ).collect(Collectors.joining("%s"));
        }else {
            result = ((PsiLiteralExpressionImpl) element).getInnerText();
        }
        return StringEscapeUtils.unescapeJava(result);
    }

    private String getPropertyKey(PsiElement element){
        PsiLiteralExpressionImpl stringElement = PsiTreeUtil.collectElementsOfType(element, PsiLiteralExpressionImpl.class).stream()
                .filter(e -> ((PsiJavaTokenImpl)e.getFirstChild()).getTokenType().equals(JavaTokenType.STRING_LITERAL)).findFirst().get();
        String capitalizeFully = StringEscapeUtils.unescapeJava(stringElement.getRawString()).toUpperCase().replaceAll("\"", "").replaceAll(" ", "_");
        capitalizeFully = StringUtils.removeEnd(capitalizeFully, "_");

        if(capitalizeFully.length() > 36){
            capitalizeFully = capitalizeFully.substring(0, 36);
        }
        return capitalizeFully;
    }
}
