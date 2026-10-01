package com.swissas.quickfix;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.util.PsiLiteralUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import com.swissas.util.TranslationHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
        this.sharedProperties = new Properties();
        this.sharedProperties.putAll(SwissAsStorage.getInstance().getShareProperties());
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
        Set<String> keysInProperties = properties.getNamesMap().keySet();
        String fullKey = properties.getNamesMap().entrySet()
                                   .stream()
                                   .filter(e -> propertyValue.equals(e.getValue()))
                                   .map(Entry::getKey)
                                   .findFirst()
                                   .orElse(null);
        if(fullKey == null){
            fullKey = generateFullKeyForAlreadyAssignedKey(keysInProperties, tmpPropertyValue);
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
    public String generateFullKeyForAlreadyAssignedKey(Set<String> keysInProperties,
                                                       String propertyValue) {
        return TranslationHelper.generateFullKey(keysInProperties, propertyValue, this.ending);
    }
    
    
    private void addImport(PsiClass messageClass, String memberName){
        PsiFile file = this.javaPsiPointer.getElement();
        Collection<PsiImportStaticStatement> psiImportStatements = PsiTreeUtil.collectElementsOfType(file, PsiImportStaticStatement.class);
        if(psiImportStatements.stream().noneMatch(e -> e.getText().contains("._Messages.*"))){
            PsiImportStaticStatement importStaticStatement = JavaPsiFacade.getElementFactory(this.javaPsiPointer.getProject()).createImportStaticStatement(messageClass, memberName);
            PsiImportList importList = Optional.ofNullable(file)
                                               .filter(PsiJavaFile.class::isInstance)
                                               .map(PsiJavaFile.class::cast)
                                               .map(PsiJavaFile::getImportList)
                                               .orElse(null);
            if(importList != null && Stream.of(importList.getImportStaticStatements())
                                           .map(PsiElement::getText)
                                           .noneMatch(e -> e.equals(importStaticStatement.getText()))) {
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
        PsiDirectory currentDirectory = Objects.requireNonNull(this.javaPsiPointer.getElement())
                                               .getContainingDirectory();
        PsiFile file = currentDirectory.findFile("Standard.properties");
        if(file == null){
            file = currentDirectory.createFile("Standard.properties");
        }
        return (PropertiesFile)file;
    }

    private String getPropertyValue(PsiElement element){
        String result;
        if(element instanceof PsiPolyadicExpression){
            StringBuilder stringBuilder = new StringBuilder();
            List<PsiElement> psiElements = getElementsItemsToTranslate(element);
            for (PsiElement psiElement : psiElements) {
                if(psiElement instanceof PsiLiteralExpressionImpl psiLiteralExpression){
                    if(JavaTokenType.STRING_LITERAL.equals(psiLiteralExpression.getLiteralElementType())){
                        stringBuilder.append(PsiLiteralUtil.getStringLiteralContent(psiLiteralExpression));
                    }else {
                        stringBuilder.append("%s");
                    }
                } else {
                    stringBuilder.append("%s");
                }
            }
            result = stringBuilder.toString();
        }else {
            result = PsiLiteralUtil.getStringLiteralContent((PsiLiteralExpression)element);
        }
        result = StringUtil.unescapeStringCharacters(result);
        return autoCorrectCommonMistakes(result);
    }
    
    @NotNull
    private List<PsiElement> getElementsItemsToTranslate(PsiElement element) {
        return Stream.of(element.getChildren()).filter(e -> e instanceof PsiLiteralExpression
                                                     || e instanceof PsiMethodCallExpression
                                                     || e instanceof PsiReferenceExpression)
                     .collect(Collectors.toList());
    }
    
    private List<PsiElement> getElementsToFormat(PsiElement element) {
        return Stream.of(element.getChildren()).filter(e -> e instanceof PsiMethodCallExpression
                                                            || e instanceof PsiReferenceExpression)
                     .collect(Collectors.toList());
    }
    

    private String convertPropertyStringToKey(@NotNull String properpertyString) {
        return TranslationHelper.convertPropertyStringToKey(properpertyString);
    }

    private String autoCorrectCommonMistakes(String sentence){
        return TranslationHelper.autoCorrectCommonMistakes(sentence);
    }

    private String replaceWithKnownKeys(@NotNull String sentence){
        return TranslationHelper.replaceWithKnownKeys(sentence, this.sharedProperties);
    }
}
