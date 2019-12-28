package com.swissas.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStaticStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPolyadicExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import groovy.json.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Quickfix for translating normal keys
 *
 * @author Tavan Alain
 */

public class TranslateQuickFix implements LocalQuickFix {

    private static final String MESSAGE_CLASS = "_Messages.java";
    
    private final SmartPsiElementPointer<PsiFile> javaPsiPointer;
    private final Properties                      sharedProperties;
    protected String ending;
    protected String className;
    

    public TranslateQuickFix(PsiFile file){
        this.javaPsiPointer = SmartPointerManager.getInstance(file.getProject()).createSmartPsiElementPointer(file);
        this.ending = "_TXT";
        this.className = "MultiLangText";
        this.sharedProperties = SwissAsStorage.getInstance().getShareProperties();
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return ResourceBundle.getBundle("texts").getString("swiss.as");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return ResourceBundle.getBundle("texts").getString("translate");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PropertiesFile properties = getOrCreateProperties();
        PsiFile currentTranslationJavaFile = getOrCreateMessageFile();
        PsiElement element = descriptor.getPsiElement();
        String tmpPropertyValue = getPropertyValue(element);
        String propertyValue = replaceWithKnownKeys(tmpPropertyValue);
        List<String> valuesInProperties = new ArrayList<>(properties.getNamesMap().values());
        String fullKey = properties.getNamesMap().entrySet().stream().filter(e -> e.getValue().equals(propertyValue)).map(Entry::getKey).findFirst().orElse(null);
        if(fullKey == null){
            fullKey = generateFullKeyForAlreadyAssignedKey(valuesInProperties, tmpPropertyValue);
            fillPropertiesAndMessageWith(project, properties, currentTranslationJavaFile,
                                         propertyValue, fullKey);
        }
        replaceTextWithTranslation(element, fullKey);
        PsiClass messageClass = PsiTreeUtil.getChildOfType(currentTranslationJavaFile.getContainingFile(), PsiClass.class);
        addImport(messageClass, fullKey);
    }
    
    public void replaceTextWithTranslation(PsiElement elementToReplace, String translationKey) {
        StringBuilder replacement = new StringBuilder(translationKey);
        if(elementToReplace instanceof PsiPolyadicExpression) {
            List<PsiElement> psiElements = getElementsToFormat(elementToReplace);
            replacement.append(".format(");
            replacement.append(psiElements.stream().map(PsiElement::getText).collect(Collectors.joining(", ")));
            replacement.append(")");
        }
        PsiExpression expressionFromText = JavaPsiFacade
                .getElementFactory(this.javaPsiPointer.getProject()).createExpressionFromText(replacement.toString(), null);
        elementToReplace.replace(expressionFromText);
    }
    
    public void fillPropertiesAndMessageWith(@NotNull Project project,
                                             PropertiesFile properties, PsiFile messageFile,
                                             String propertyValue, String propertyKey) {
        properties.addProperty(propertyKey, propertyValue);
        PsiElement javaTranslation = JavaPsiFacade.getElementFactory(project).createFieldFromText("static final " + this.className + " " + propertyKey
                                                                                                  + " = new " + this.className + "(INSTANCE);\n", null);
        PsiField latestField = PsiTreeUtil.collectElementsOfType(messageFile, PsiField.class).stream().reduce((a, b) -> b).get();
        latestField.getParent().addAfter(javaTranslation, latestField);
    }
    
    @NotNull
    public String generateFullKeyForAlreadyAssignedKey(List<String> valuesInProperties,
                                                       String propertyValue) {
        String translatedKey = convertPropertyStringToKey(propertyValue);
        String fullKey;
        int numberInCaseOfDuplicateKey = 0;
        fullKey = translatedKey + this.ending;
        while(valuesInProperties.contains(fullKey)) {
            numberInCaseOfDuplicateKey++;
            fullKey = translatedKey + "_" + numberInCaseOfDuplicateKey + this.ending;
        }
        return fullKey;
    }
    
    
    private void addImport(PsiClass messageClass, String memberName){
        PsiFile file = this.javaPsiPointer.getElement();
        Collection<PsiImportStaticStatement> psiImportStatements = PsiTreeUtil.collectElementsOfType(file, PsiImportStaticStatement.class);
        if(psiImportStatements.stream().noneMatch(e -> e.getText().contains("._Messages.*"))){
            PsiImportStaticStatement importStaticStatement = JavaPsiFacade.getElementFactory(this.javaPsiPointer.getProject()).createImportStaticStatement(messageClass, memberName);
            PsiImportList importList = ((PsiJavaFile) file).getImportList();
            if(Stream.of(importList.getImportStaticStatements()).map(PsiElement::getText).noneMatch(e -> e.equals(importStaticStatement.getText()))) {
                importList.add(importStaticStatement);
            }
        }
    }

    @NonNls
    private PsiFile getOrCreateMessageFile(){
        PsiDirectory containingDirectory = this.javaPsiPointer.getElement().getContainingDirectory();
        PsiFile messageFile = containingDirectory.findFile(MESSAGE_CLASS);
        if(messageFile == null){
            String classContent = "import amos.share.multiLanguage." + this.className + ";\n" +
                    "import amos.share.multiLanguage.Translateable;\n" +
                    "\n" +
                    "/*****************************************************************************\n" +
                    " * Container for all translateable messages inside this package.\n" +
                    " *\n" +
                    " * @author AUTO\n" +
                    " ****************************************************************************/\n" +
                    "class _Messages extends Translateable {\n" +
                    "\tprivate static final _Messages INSTANCE = new _Messages();\n\n" +
                    "\tstatic {\n" +
                    "\t\tINSTANCE.init();\n" +
                    "\t}\n" +
                    "}";
            messageFile = PsiFileFactory.getInstance(this.javaPsiPointer.getProject()).createFileFromText(MESSAGE_CLASS, JavaLanguage.INSTANCE, classContent);
            containingDirectory.add(messageFile);
            messageFile = containingDirectory.findFile(MESSAGE_CLASS);
        }
        return messageFile;
    }

    private PropertiesFile getOrCreateProperties(){
        PsiFile file = this.javaPsiPointer.getElement().getContainingDirectory().findFile("Standard.properties");
        if(file == null){
            file = this.javaPsiPointer.getElement().getContainingDirectory().createFile("Standard.properties");
        }
        return (PropertiesFile)file;
    }

    private String getPropertyValue(PsiElement element){
        String result;
        if(element instanceof PsiPolyadicExpression){
            StringBuilder stringBuilder = new StringBuilder();
            List<PsiElement> psiElements = getElementsItemsToTranslate(element);
            for (PsiElement psiElement : psiElements) {
                if(psiElement instanceof PsiLiteralExpressionImpl){
                    PsiLiteralExpressionImpl psiLiteralExpression = (PsiLiteralExpressionImpl)psiElement;
                    if(JavaTokenType.STRING_LITERAL.equals(psiLiteralExpression.getLiteralElementType())){
                        stringBuilder.append(psiLiteralExpression.getInnerText());
                    }else {
                        stringBuilder.append("%s");
                    }
                } else {
                    stringBuilder.append("%s");
                }
            }
            result = stringBuilder.toString();
        }else {
            result = ((PsiLiteralExpressionImpl) element).getInnerText();
        }
        result = StringEscapeUtils.unescapeJava(result);
        return autoCorrectCommonMistakes(result);
    }
    
    @NotNull
    private List<PsiElement> getElementsItemsToTranslate(PsiElement element) {
        Predicate<PsiElement> onlyIncludeFullMethodCode = el -> el.getNextSibling() == null || !".".equals(el.getNextSibling().getText());
        return Stream.of(PsiTreeUtil.collectElements(element, e -> e instanceof PsiLiteralExpression
                                                                   || e instanceof PsiMethodCallExpression
                                                                   || e instanceof PsiReferenceExpression)).
                filter(onlyIncludeFullMethodCode).collect(Collectors.toList());
    }
    
    private List<PsiElement> getElementsToFormat(PsiElement element) {
        Predicate<PsiElement> onlyIncludeFullMethodCode = el -> el.getNextSibling() == null || !".".equals(el.getNextSibling().getText());
        return Stream.of(PsiTreeUtil.collectElements(element, e ->    e instanceof PsiMethodCallExpression
                                                                   || e instanceof PsiReferenceExpression
                                                                   || e instanceof PsiLiteralExpression && !JavaTokenType.STRING_LITERAL.equals(((PsiLiteralExpressionImpl)e).getLiteralElementType()))).
                             filter(onlyIncludeFullMethodCode).collect(Collectors.toList());
    }
    

    private String convertPropertyStringToKey(@NotNull String properpertyString) {
        String withoutPercent = properpertyString.replaceAll("(%s)+", "_");
        String capitalizeFully = StringEscapeUtils.unescapeJava(withoutPercent)
                                        .toUpperCase().replaceAll("[^A-Z0-9 ]", "")
                                        .replaceAll(" ", "_");
        capitalizeFully = StringUtils.removeEnd(capitalizeFully, "_");
        if(capitalizeFully.length() > 36){
            capitalizeFully = capitalizeFully.substring(0, 36);
        }
        return capitalizeFully;
    }

    @NonNls
    private String autoCorrectCommonMistakes(String sentence){
        return sentence == null ? null : sentence.replaceAll("\\b[wW]ork[ -]?[oO]rder\\b", "@WORKORDER@")
        .replaceAll("\\bWO\\b", "@WO@")
        .replaceAll("\\baircraft\\b", "@AIRCRAFT@")
        .replaceAll("\\bAC\\b", "@AC@")
        .replaceAll("\\b[pP]art[ -]?[nN]umber\\b", "@PART_NUMBER@")
        .replaceAll("\\bPN\\b", "P/N")
        .replaceAll("\\b[sS]erial[ -]?[nN]umber\\b", "@SERIAL_NUMBER@")
        .replaceAll("\\bSN\\b", "@SN@")
        .replaceAll("\\bAmos\\b", "@AMOS@")
        .replaceAll("[wW]ork[ -]?[pP]ackage\\b", "@WORKPACKAGE@")
        .replaceAll("\\bWP\\b", "@WP@")
        .replaceAll("\\b([aA])nalyze\\b", "$1nalyse")
        .replaceAll("\\bCenter\\b","Centre");
    }

    @NonNls
    private String replaceWithKnownKeys(String sentence){
        String result = sentence;
        for (Map.Entry<Object, Object> objectObjectEntry : this.sharedProperties.entrySet()) {
            String key = "@" + objectObjectEntry.getKey() + "@";
            String value = "\\b" + objectObjectEntry.getValue() + "\\b";
            result = result.replaceAll(value, key);
        }
        return result;
    }
}
