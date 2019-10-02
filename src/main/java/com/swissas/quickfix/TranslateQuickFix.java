package com.swissas.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.lang.properties.psi.*;
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
    private final Properties sharedProperties;
    private String currentPropertiesPath;
    String ending;
    String className;

    public TranslateQuickFix(PsiFile file){
        this.javaPsiPointer = SmartPointerManager.getInstance(file.getProject()).createSmartPsiElementPointer(file);
        this.ending = "_TXT";
        this.className = "MultiLangText";
        this.sharedProperties = SwissAsStorage.getInstance().getShareProperties();
    }

    @NonNls
    static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

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
        PropertiesFile properties = getOrCreateProperties();
        PsiFile currentTranslationJavaFile = getOrCreateMessageFile();
        PsiElement element = descriptor.getPsiElement();
        
        
        String propertyValue = getPropertyValue(element);
        List<String> valuesInProperties = new ArrayList<>(properties.getNamesMap().values());
        String fullKey = properties.getNamesMap().entrySet().stream().filter(e -> e.getValue().equals(propertyValue)).map(Entry::getKey).findFirst().orElse(null);
        if(fullKey == null){
            String translatedKey = getPropertyKey(element);
            int numberInCaseOfDuplicateKey = 0;
            fullKey = translatedKey + this.ending;
            while(valuesInProperties.contains(fullKey)) {
                numberInCaseOfDuplicateKey++;
                fullKey = translatedKey + "_" + numberInCaseOfDuplicateKey + this.ending;
            }
            properties.addProperty(fullKey, propertyValue);
            PsiElement javaTranslation = JavaPsiFacade.getElementFactory(project).createFieldFromText("static final " + this.className + " " + fullKey + " = new " + this.className + "(INSTANCE);\n", null);
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
        PsiExpression expressionFromText = JavaPsiFacade.getElementFactory(this.javaPsiPointer.getProject()).createExpressionFromText(replacement.toString(), null);
        element.replace(expressionFromText);
        PsiClass messageClass = PsiTreeUtil.getChildOfType(currentTranslationJavaFile.getContainingFile(), PsiClass.class);
        addImport(messageClass, fullKey);
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
                    " * @author  AUTO\n" +
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
            //with something like getMethod1().getMethod2() psiMethodCallExpress will see 2 results. One that contains the full stuff, and one only the ending. We don't need the last one so we filter it out
            Predicate<PsiElement> onlyIncludeFullMethodCode = el -> el.getNextSibling() == null || !".".equals(el.getNextSibling().getText());
            List<PsiElement> psiElements = Stream.of(PsiTreeUtil.collectElements(element, e -> e instanceof PsiLiteralExpression || e instanceof PsiMethodCallExpression || e instanceof  PsiReferenceExpression)).
                    filter(onlyIncludeFullMethodCode).collect(Collectors.toList());
            for (PsiElement psiElement : psiElements) {
                if(psiElement instanceof PsiLiteralExpressionImpl){
                    PsiLiteralExpressionImpl psiLiteralExpression = (PsiLiteralExpressionImpl)psiElement;
                    if(JavaTokenType.STRING_LITERAL.equals(psiLiteralExpression.getLiteralElementType())){
                        stringBuilder.append(psiLiteralExpression.getInnerText());
                        continue;
                    }
                }
                stringBuilder.append("%s");
            }
            result = stringBuilder.toString();
        }else {
            result = ((PsiLiteralExpressionImpl) element).getInnerText();
        }
        result = StringEscapeUtils.unescapeJava(result);
        result = autoCorrectCommonMisstakes(result);
        return replaceWithKnownKeys(result);
        
    }

    private String getPropertyKey(PsiElement element){
        PsiLiteralExpressionImpl stringElement = PsiTreeUtil.collectElementsOfType(element, PsiLiteralExpressionImpl.class).stream()
                .filter(e -> ((PsiJavaTokenImpl)e.getFirstChild()).getTokenType().equals(JavaTokenType.STRING_LITERAL)).findFirst().get();
        String capitalizeFully = Objects.requireNonNull(StringEscapeUtils.unescapeJava(stringElement.getInnerText())).toUpperCase().replaceAll("[^A-Z0-9 ]", "").replaceAll(" ", "_");
        capitalizeFully = StringUtils.removeEnd(capitalizeFully, "_");

        if(capitalizeFully.length() > 36){
            capitalizeFully = capitalizeFully.substring(0, 36);
        }
        return capitalizeFully;
    }

    @NonNls
    private String autoCorrectCommonMisstakes(String sentence){
        return sentence.replaceAll("\\b[wW]ork[ -]?[oO]rder\\b", "@WORKORDER@")
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
            String key = "@" + objectObjectEntry.getKey().toString() + "@";
            String value = "\\b" + objectObjectEntry.getValue().toString() + "\\b";
            result = result.replaceAll(value, key);
        }
        return result;
    }
}
