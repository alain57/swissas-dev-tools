package com.swissas.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper for some specific tasks needed in the plugin
 * @author Tavan Alain
 */
public class PsiHelper {
	private static final Logger    LOGGER   = Logger.getInstance("Swiss-as");
	private static       PsiHelper instance = null;
	
	private PsiHelper() {
		
	}
	
	public static PsiHelper getInstance() {
		if(instance == null) {
			instance = new PsiHelper();
		}
		return instance;
	}
	
	public PsiDirectory findOrCreateDirectoryInShared(@NotNull Project project,
	                                                  @NotNull String packageName) {
		PsiDirectory result = null;
		String dirPath = Optional.ofNullable(project.getBasePath())
				.map(path -> VfsUtil.findFile(Paths.get(path), false))
				.flatMap(virtualFile -> Stream.of(virtualFile.getChildren())
				.filter(e -> e.isDirectory() && e.getName().contains("share"))
				.map(VirtualFile::getPath).findFirst()).orElse(null);
		if(dirPath == null) {
			LOGGER.error("your project is not correctly setup ! The project base path is : " + project.getBasePath());
			return null;
		}
		dirPath += "/" + "src" + "/" +
		                 packageName.replaceAll("\\.", "/");
		try {
			result = Optional.ofNullable(VfsUtil.createDirectoryIfMissing(dirPath))
							 .map(vfsDir -> PsiManager.getInstance(project).findDirectory(vfsDir)).orElse(null);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return result;
	}
	
	public List<PsiClass> getRpcImplementationForProjectUp(@NotNull Project project) {
		Set<PsiClass> result = new HashSet<>();
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		List<String> registries = List.of("AmosComponentRpcRegistry", "AmosApnRpcRegistry", "AmosInstantInfoRpcRegistry"
				, "AmosSelectionDialogRpcRegistry", "AmosFilterSelectionDialogRpcRegistry", "AmosReportRpcRegistry"
				, "AmosActionRpcRegistry", "AmosMobileSolDataProviderRpcRegistry");
		for (String registry : registries) {
			PsiClass rpcRegistry = javaPsiFacade
					.findClass("amos.server.system.transport." + registry, GlobalSearchScope.allScope(project));
			if(rpcRegistry == null || rpcRegistry.getConstructors().length == 0) {
				LOGGER.error("there is no constructor found in " + registry);
				continue;
			}
			PsiCodeBlock constructorContent = rpcRegistry.getConstructors()[0].getBody();
			PsiClassObjectAccessExpression[] expressions = PsiTreeUtil
					.collectElementsOfType(constructorContent, PsiClassObjectAccessExpression.class)
					.toArray(PsiClassObjectAccessExpression[]::new);
			Set<PsiClass> psiClasses = IntStream.range(0, expressions.length).filter(i -> i % 2 != 0)
			                                 .mapToObj(i -> expressions[i])
			                                 .map(e -> PsiUtil.resolveClassInType(e.getOperand().getType()))
			                                 .collect(Collectors.toSet());
			result.addAll(psiClasses);
		}
		
		return result.stream()
		             .sorted(Comparator.nullsLast(Comparator.comparing(PsiNamedElement::getName)))
				     .collect(Collectors.toList());
		
	}
	
	public List<PsiClass> getBoClassesForProjectUp(@NotNull Project project) {
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		PsiClass abstractBoClass = Optional.ofNullable(javaPsiFacade.findClass("amos.server.databaseAccess.bo.AbstractAmosBusinessObject",  GlobalSearchScope.allScope(project)))
		                                   .orElseThrow(() -> new IllegalStateException("Could not found the class AbstractAmosBusinessObject"));
				
		Query<PsiClass> search = ClassInheritorsSearch
				.search(abstractBoClass, GlobalSearchScope.projectScope(project), true);
		return search.findAll()
		             .stream()
		             .filter(e -> !e.isInterface() && !e.hasModifier(JvmModifier.ABSTRACT) && e.getQualifiedName() != null)
		             .collect(Collectors.toList());
	}
	
	public Map<String, PsiClass> getRpcImplementationMapForProjectUp(@NotNull Project project) {
		List<PsiClass> list = getRpcImplementationForProjectUp(project);
		return list.stream().collect(Collectors.toMap(PsiNamedElement::getName, Function
				.identity(), (a, b) -> b)); //don't care about duplicate, people should be smart and stop giving the same name for different objects ! 
	}
	
	public Map<String, PsiClass> getBoMapForProjectUp(@NotNull Project project) {
		List<PsiClass> list = getBoClassesForProjectUp(project);
		return list.stream().collect(Collectors.toMap(PsiNamedElement::getName, Function
				.identity(), (a, b) -> b)); //don't care about duplicate, people should be smart and stop giving the same name for different objects ! 
	}
	
	public void addFieldGetterAndSetterFromPsiMethod(PsiMethod psiMethod, PsiClass destinationClass) {
		String variable = StringUtils.getInstance().removeGetterPrefix(psiMethod.getName());
		String objectType =  psiMethod.getReturnType().getCanonicalText();
		String getterName = psiMethod.getName();
		boolean isBoReturned = isBoReturned(psiMethod);
		PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
		PsiComment todoComment = psiElementFactory
				.createCommentFromText("//TODO: remove the BO here !!!", null);
		PsiField field = psiElementFactory
				.createFieldFromText("private " + objectType   + " " + variable + ";", null);
		if (isBoReturned) {
			field.add(todoComment);
		}
		destinationClass.add(field);
		String setterName = "set" + variable.substring(0, 1).toUpperCase() + variable.substring(1);
		PsiMethod getter = psiElementFactory
				.createMethodFromText("public " + objectType + " " + getterName + "() {"
				                      + "return this." + variable + ";"
				                      + "}", null);
		if(SwissAsStorage.getInstance().isUseAmosBeanAnnotationDto()) {
			PsiAnnotation annotation = getAmosBeanAnnotation(psiMethod);
			if (annotation != null) {
				PsiModifierList list = getter.getModifierList();
				if (!annotation.getText().endsWith("\n")) {
					PsiElement newLine = PsiParserFacade.SERVICE.getInstance(psiMethod.getProject())
					                                            .createWhiteSpaceFromText("\n");
					annotation.add(newLine);
				}
				list.getParent().addBefore(annotation, list);
			}
		}
		PsiMethod setter = psiElementFactory
				.createMethodFromText("public void " + setterName + "("
				                      + objectType + " " + variable + "){"
				                      + "this." + variable + " = " + variable
				                      + ";"
				                      + "}", null);
		if (isBoReturned) {
			getter.add(todoComment);
			setter.add(todoComment);
		}
		destinationClass.add(getter);
		destinationClass.add(setter);
	}
	
	public boolean isBoReturned(PsiMethod getterMethod) {
		return InheritanceUtil.isInheritor(getterMethod.getReturnType(),
		                                   "amos.server.databaseAccess.bo.AbstractAmosBusinessObject");
	}
	
	public void addEntityTag(Project project, PsiClass destinationClass) {
		String objectType = "amos.share.databaseAccess.occ.EntityTag";
		PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
		PsiField field = psiElementFactory
				.createFieldFromText("private amos.share.databaseAccess.occ.EntityTag entityTag;", null);
		destinationClass.add(field);
		
		PsiMethod getter = psiElementFactory
				.createMethodFromText("public " + objectType + " getEntityTag() {"
				                      + "return this.entityTag;"
				                      + "}", null);
		PsiMethod setter = psiElementFactory
				.createMethodFromText("public void setEntityTag("
				                      + "amos.share.databaseAccess.occ.EntityTag entityTag){"
				                      + "this.entityTag = entityTag;"
				                      + "}", null);
		destinationClass.add(getter);
		destinationClass.add(setter);
	}
	
	@NotNull
	public PsiMethod generateToDtoMethod(@NotNull Project project,@NotNull  List<PsiMethod> gettersToInclude,
	                                      @Nullable String pkName,
	                                      @NotNull String boName,@NotNull  String dtoName,
	                                      boolean useEntityTag) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		StringBuilder toDtoContent = new StringBuilder("static ").append(dtoName)
		                                                         .append(" toDto(@NotNull ")
		                                                         .append(boName)
		                                                         .append(" bo) {\n")
		                                                         .append("\t")
		                                                         .append(dtoName)
		                                                         .append(" dto = new ")
		                                                         .append(dtoName)
		                                                         .append("();\n");
		if(useEntityTag) {
			StringUtils.getInstance().addSetOfGetter(toDtoContent, "getEntityTag", pkName, false);
		}
		for (PsiMethod getter : gettersToInclude) {
			StringUtils.getInstance().addSetOfGetter(toDtoContent, getter.getName(), pkName, false);
		}
		toDtoContent.append("\treturn dto;\n").append("}\n");
		return elementFactory.createMethodFromText(toDtoContent.toString(), null);
	}
	
	@NotNull
	private PsiElement generateListToDto(@NotNull Project project, @NotNull String boName, @NotNull String dtoName) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content = "static List<" + dtoName +"> toDtos(@NotNull List<" + boName + "> bos) {\n"
		+ "\treturn ListUtils.map(bos, " + dtoName + "Mapper::toDto);\n"
		+ "}\n";
		return elementFactory.createMethodFromText(content, null);
	}
	
	@NotNull
	private PsiElement generateListToBo(@NotNull Project project, @NotNull String boName,
	                                    @NotNull String boFinderClassName,
	                                    @NotNull String dtoName, @Nullable String pkGetter) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content;
		if(pkGetter == null) {
			content = "static List<" + boName + "> toBos(@NotNull List<" + dtoName + "> dtos){\n"
			          + "\t//The BO has no @AmosBeanInfo(primaryKey=true), can't auto generate this method !\n"
			          + "\treturn Collections.emptyList();\n"
			          + "}\n";
		} else {
			String methodEnding = StringUtils.getInstance().removeGetterPrefix(pkGetter, false);
			content = "static List<" + boName + "> toBos(@NotNull List<" + dtoName + "> dtos) {\n"
			          + "\tif(dtos.isEmpty()) {\n"
			          + "\t\treturn Collections.emptyList();\n"
			          + "\t}\n"
			          + "\tList<Integer> existingDtoIds = dtos.stream().map(" + dtoName + "::"
			          + pkGetter + ")\n"
			          + "\t                            .filter(pk -> pk != null && pk > 0).collect(\n"
			          + "\t\t\t\t\tCollectors.toList());\n"
			          + "\tMap<Integer, " + boName + "> existingBoMap =\n"
			          + "\t\t\texistingDtoIds.isEmpty() ? Collections.emptyMap() :\n"
			          + "\t\t\t" + boFinderClassName + ".findBy" + methodEnding
			          + "s(existingDtoIds)\n"
			          + "\t\t\t                  .stream().collect(Collectors.toMap(\n"
			          + "\t\t\t                  \t\t" + boName + "::" + pkGetter
			          + ", Function.identity()));\n"
			          + "\t\n"
			          + "\tList<" + boName + "> result = new ArrayList<>();\n"
			          + "\tfor (" + dtoName + " dto : dtos) {\n"
			          + "\t\t" + boName + " bo = existingBoMap\n"
			          + "\t\t\t\t.getOrDefault(dto." + pkGetter + "(), new " + boName + "());\n"
			          + "\t\tcopyToBo(dto, bo);\n"
			          + "\tresult.add(bo);\n"
			          + "\t}\n"
			          + "\treturn result;\n"
			          + "}";
		}
		return elementFactory.createMethodFromText(content, null);
	}
	
	@NotNull 
	private PsiElement generateDeleteDtos(@NotNull Project project, @NotNull String boName, @NotNull String dtoName) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content = "static void delete(@NotNull List<" + dtoName +"> dtosToDelete){\n"
		                 + "\tList<" + boName + "> bos = toBos(dtosToDelete);\n"
		                 + "\tAmosTransaction transaction = new AmosTransaction();\n"
		                 + "\tfor ("+ boName + " bo : bos) {\n"
		                 + "\t\tbo.deleteToTransaction(transaction);\n"
		                 + "\t}\n"
		                 + "\ttransaction.execute();\n"
		                 + "}\n";
		return elementFactory.createMethodFromText(content, null);
	}
	
	@NotNull
	private PsiElement generateSaveDtos(@NotNull Project project,@NotNull String boFinderClassName,  @NotNull String boName, @NotNull String dtoName, @Nullable String pkGetter) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content;
		if(pkGetter == null) {
			content = "static List<" + boName + "> saveDtos(@NotNull List<" + dtoName + "> dtosToSave){\n"
					+ "\t//The BO has no @AmosBeanInfo(primaryKey=true), can't auto generate this method !\n"
			        + "\treturn Collections.emptyList();\n"  
					+ "}\n";
		}else {
			String methodEnding = StringUtils.getInstance().removeGetterPrefix(pkGetter) + "s";
			String methodName = "findBy" + methodEnding;
			content = "static List<" + boName + "> saveDtos(@NotNull List<" + dtoName + "> dtosToSave){\n"
			                 + "\tList<" + boName + "> bos = toBos(dtosToSave);\n"
			                 + "\tAmosTransaction transaction = new AmosTransaction();\n"
			                 + "\tfor ("+ boName + " bo : bos) {\n"
			                 + "\t\tbo.saveToTransaction(transaction);\n"
			                 + "\t}\n"
			                 + "\ttransaction.execute();\n"
			                 + "\t//need to load them again to have the entityTag refreshed.\n"
			                 + "\treturn "+ boFinderClassName + "." + methodName + "(bos.stream()\n"
			                 + "\t                                                .map(" + boName + "::" + pkGetter + ")\n"
			                 + "\t                                                .collect(Collectors.toList()));\n"
			                 + "}\n";
		}
		return elementFactory.createMethodFromText(content, null);
	}
	
	public void generateFindByIdsIfNeeded(@NotNull Project project, @NotNull Pair<PsiClass, PsiMethod> finder, @NotNull String boName, @NotNull String pkGetter, @NotNull String tableWithColumn) {
		String methodEnding = StringUtils.getInstance().removeGetterPrefix(pkGetter, false) + "s";
		String methodName = "findBy" + methodEnding;
		String table = tableWithColumn.split("\\.")[0];
		if (Stream.of(finder.getFirst().getMethods()).noneMatch(method -> methodName.equals(method.getName()) &&
		                                                                  InheritanceUtil.isInheritor(
				method.getParameterList().getParameters()[0].getType(), "java.util.Collection"))) {
			PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
			String content = "/** Auto Generated by the Dto generator */\n"
			                 + "public static List<"+ boName +"> " + methodName + "(Collection<Integer> ids) {\n"
			                 + "\treturn " + table + ".get().findBy(new "+ table + ".Column[]{" + tableWithColumn + "}, ids.toArray())" 
			                 + ".stream().map(" + boName + "::new).collect(Collectors.toList());\n"
			                 + "}";
			PsiMethod findByIds = elementFactory.createMethodFromText(content, null);
			finder.getFirst().addAfter(findByIds, finder.getSecond());
		}
	}
	
	@NotNull
	public PsiMethod generateCopyToBoMethod(@NotNull Project project,@NotNull  List<PsiMethod> gettersToInclude,
	                                         @NotNull String boName,@NotNull  String dtoName,
	                                         boolean useEntityTag) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		StringBuilder toBoContent = new StringBuilder("static void copyToBo(").append(dtoName)
		                                                                      .append(" dto, ").append(boName).append(" bo) {\n");
		if(useEntityTag) {
			StringUtils.getInstance().addSetOfGetter(toBoContent, "getEntityTag", null, true);
		}
		for (PsiMethod getter : gettersToInclude) {
			StringUtils.getInstance().addSetOfGetter(toBoContent, getter.getName(), null, true);
		}
		toBoContent.append("}\n");
		return elementFactory.createMethodFromText(toBoContent.toString(), null);
	}
	
	@NotNull
	public PsiJavaFile applyCodeStyleAndReformatImports(@NotNull PsiJavaFile fileToCleanup) {
		Project project = fileToCleanup.getProject();
		JavaCodeStyleManager instance = JavaCodeStyleManager
				.getInstance(project);
		PsiJavaFile cleaned = (PsiJavaFile)instance.shortenClassReferences(fileToCleanup);
		instance.optimizeImports(cleaned);
		return (PsiJavaFile) CodeStyleManager.getInstance(project).reformat(cleaned);
	}
	
	public void addFileInDirectory(@NotNull PsiDirectory directory,@NotNull PsiJavaFile fileInMemory) {
		PsiFile existingFile = directory.findFile(fileInMemory.getName());
		if(existingFile == null) {
			existingFile = directory.createFile(fileInMemory.getName());
		}
		existingFile
					.deleteChildRange(existingFile.getFirstChild(), existingFile.getLastChild());
		Stream.of(fileInMemory.getChildren()).forEachOrdered(existingFile::add);
	}
	
	public void addMapplingClass(@NotNull PsiJavaFile mapperFile,
	                             @NotNull List<PsiMethod> gettersToInclude,
	                             @Nullable String pkGetterName,
	                             @NotNull Pair<PsiClass, PsiMethod> finder,
	                             @NotNull String boName, @NotNull String dtoName,
	                             boolean hasEntityTag) {
		Project project = mapperFile.getProject();
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String javaDocTxt = "/**\n"
		                    + " * Auto generated DTO/BO Mapper\n"
		                    + " *\n"
		                    + " * @author " + SwissAsStorage.getInstance().getFourLetterCode() + "\n"
		                    + " */\n";
		
		PsiClass mapperClass = elementFactory.createClass(dtoName + "Mapper");
		mapperClass.getModifierList().setModifierProperty(PsiModifier.PACKAGE_LOCAL, true);
		mapperClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
		PsiDocComment javadoc = elementFactory.createDocCommentFromText(javaDocTxt);
		mapperClass.addBefore(javadoc, mapperClass.getFirstChild());
		
		mapperClass.add(generateToDtoMethod(project, gettersToInclude,
		                                    pkGetterName, boName, dtoName, hasEntityTag));
		mapperClass.add(generateCopyToBoMethod(project, gettersToInclude,
		                                       boName, dtoName, hasEntityTag));
		mapperClass.add(generateListToDto(project, boName, dtoName));
		mapperClass.add(generateDeleteDtos(project, boName, dtoName));
		mapperClass.add(generateSaveDtos(project, finder.getFirst().getName(), boName, dtoName, pkGetterName));
		mapperClass.add(generateListToBo(project, boName, finder.getFirst().getName(), dtoName, pkGetterName));
		mapperFile.add(mapperClass);
		
	}
	
	
	public List<PsiMethod> getGettersForPsiClass(@NotNull PsiClass psiClass) {
		List<PsiMethod> psiMethods = Stream.of(psiClass.getChildren()).filter(PsiMethod.class::isInstance)
		      .map(PsiMethod.class::cast).collect(Collectors.toList());
		      
		return psiMethods.stream().filter(this::isGetter).sorted(
				Comparator.comparing(PsiMethod::getName)).collect(Collectors.toList());
	}
	
	public String getDDPkForPsiClass(@NotNull PsiMethod pkGetter) {
		PsiMethodCallExpression expression = PsiTreeUtil
				.collectElementsOfType(pkGetter.getBody(), PsiMethodCallExpression.class).stream()
				.findFirst().orElse(null);
		if(expression == null) {
			LOGGER.error("could not find a method call in the pk getter");
		}
		PsiCodeBlock ddPkBody = ((PsiMethod) expression.getMethodExpression().resolve()).getBody();
		return new ArrayList<>(PsiTreeUtil.collectElementsOfType(ddPkBody, PsiReferenceExpression.class)).get(1).getText();
		
	}
	
	@Nullable
	private PsiAnnotation getAmosBeanAnnotation(@NotNull PsiMethod method){
		PsiModifierList list = method.getModifierList();
		
		PsiAnnotation originalAnnotation = list
				.findAnnotation("amos.share.databaseAccess.bo.AmosBeanInfo");
		return originalAnnotation == null ? null : (PsiAnnotation) originalAnnotation.copy();
	}
	
	public boolean isPrimaryGetter(@NotNull PsiMethod getter){
		PsiModifierList list = getter.getModifierList();
		PsiAnnotation annotation = list
				.findAnnotation("amos.share.databaseAccess.bo.AmosBeanInfo");
		return annotation != null && annotation.hasAttribute("primaryKey");
	}
	
	private boolean isGetter(@NotNull PsiMethod psiMethod) {
		boolean result = false;
		if(psiMethod.hasModifier(JvmModifier.PUBLIC) &&
		   !psiMethod.hasModifier(JvmModifier.STATIC) &&
		   !PsiType.VOID.equals(psiMethod.getReturnType()) &&
		   psiMethod.getParameterList().isEmpty()) {
			result = StringUtils.getInstance().isGetter(psiMethod.getName());
		}
		return result;
	}
	
	/**
	 * As some BO has a home class this method will check both Bo and BoHome and take the one that has
	 * finder methods. It will also return the last finder
	 */
	@Nullable
	public Pair<PsiClass, PsiMethod> getFinderClassAndLastFinder(@NotNull Project project, @NotNull PsiClass boClass) {
		PsiClass result = boClass;
		List<PsiMethod> staticMethods = Stream.of(boClass.getMethods())
		                                      .filter(method -> method.hasModifier(JvmModifier.STATIC))
		                                      .filter(method -> method.getName().toLowerCase().startsWith("findby"))
		                                      .collect(Collectors.toList());
		if(staticMethods.isEmpty()) {
			JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
			result = javaPsiFacade.findClass(boClass.getQualifiedName() + "Home",
			                                             GlobalSearchScope.allScope(project));
			staticMethods = Stream.of(result.getMethods())
			                      .filter(method -> method.hasModifier(JvmModifier.STATIC))
			                      .filter(method -> method.getName().toLowerCase().startsWith("findby"))
			                      .collect(Collectors.toList());
		}
		if(staticMethods.isEmpty()) {
			return new Pair<>(null, null);
		}
		return new Pair<>(result, staticMethods.get(staticMethods.size() - 1));
	}
	
	@NotNull 
	public PsiMethod generateEquals(@NotNull Project project, @NotNull String dtoName, @NotNull String getterName) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content = "@Override\npublic boolean equals(Object o) {\n"
		                 + "\tif (this == o) {\n"
						 + "\t\treturn true;\n"
		                 + "\t}\n"
		                 + "\tif (o == null || getClass() != o.getClass()) {" 
		                 + "\t\treturn false;\n" 
		                 + "\t}\n"
		                 + "\t" + dtoName + " that = (" + dtoName + ") o;\n"
		                 + "\treturn Objects.equals(" + getterName + "(), that." + getterName
		                 + "());\n"
		                 + "\t}\n";
		return elementFactory.createMethodFromText(content, null);
	}
	
	@NotNull
	public PsiElement generateHashcode(@NotNull Project project, @NotNull String pkMethod) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		String content = "@Override\npublic int hashCode() {\n"
		                 + "\treturn Objects.hash(" + pkMethod + "());\n"
		                 + "}\n";
		return elementFactory.createMethodFromText(content, null);
	}
}
