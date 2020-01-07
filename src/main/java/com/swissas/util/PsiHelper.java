package com.swissas.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
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
	private static       PsiHelper INSTANCE = null;
	
	private PsiHelper() {
		
	}
	
	public static PsiHelper getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new PsiHelper();
		}
		return INSTANCE;
	}
	
	public PsiDirectory findOrCreateDirectoryInShared(@NotNull Project project,
	                                                  @NotNull String packageName) {
		PsiDirectory result = null;
		//project is an xml file. first parent is .idea folder, second parent is the real top folder of project
		String dirPath = Stream.of(project.getProjectFile().getParent().getParent().getChildren()) 
			      .filter(e -> e.isDirectory() && e.getName().contains("share"))
			      .map(e -> e.getPath()).findFirst().orElseThrow();
	
		
		dirPath += "/" + "src" + "/" +
		                 packageName.replaceAll("\\.", "/");
		try {
			VirtualFile vfsDir = VfsUtil.createDirectoryIfMissing(dirPath);
			result = PsiManager.getInstance(project).findDirectory(vfsDir);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return result;
	}
	
	public List<PsiClass> getRpcImplementationForProjectUp(@NotNull Project project) {
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		PsiClass rpcRegistry = javaPsiFacade
				.findClass("amos.server.system.transport.AmosApnRpcRegistry", GlobalSearchScope.allScope(project));
		PsiCodeBlock constructorContent = rpcRegistry.getConstructors()[0].getBody();
		PsiClassObjectAccessExpression[] expressions = PsiTreeUtil
				.collectElementsOfType(constructorContent, PsiClassObjectAccessExpression.class)
				.toArray(PsiClassObjectAccessExpression[]::new);
		return IntStream.range(0, expressions.length).filter(i -> i % 2 != 0)
		                                                        .mapToObj(i -> expressions[i])
		                .map(e -> PsiUtil.resolveClassInType(e.getOperand().getType()))
		                                                        .collect(Collectors.toList());
		
	}
	
	public List<PsiClass> getBoClassesForProjectUp(@NotNull Project project) {
		JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
		PsiClass abstractBoClass = javaPsiFacade.findClass("amos.share.databaseAccess.bo.AbstractAmosBusinessObject",  GlobalSearchScope.allScope(project));
		Query<PsiClass> search = ClassInheritorsSearch
				.search(abstractBoClass, GlobalSearchScope.projectScope(project), true);
		return search.findAll().stream().filter(e -> !e.isInterface() && !e.hasModifier(
				JvmModifier.ABSTRACT) && e.getQualifiedName() != null).collect(Collectors.toList());
	}
	
	public Map<String, PsiClass> getRpcImplementationMapForProjectUp(@NotNull Project project) {
		List<PsiClass> list = getRpcImplementationForProjectUp(project);
		return list.stream().collect(Collectors.toMap(PsiNamedElement::getName, Function
				.identity()));
	}
	
	public Map<String, PsiClass> getBoMapForProjectUp(@NotNull Project project) {
		List<PsiClass> list = getBoClassesForProjectUp(project);
		return list.stream().collect(Collectors.toMap(PsiNamedElement::getName, Function
				.identity(), (a, b) -> b)); //don't care about duplicate, people should be smart and stop giving the same name for different objects ! 
		
	}
	
	public void addFieldGetterAndSetter(Project project, PsiClass destinationClass, 
	                                     String objectType, String getterName, boolean addTodo) {
		String variable = StringUtils.getInstance().removeGetterPrefix(getterName);
		PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
		PsiComment todoComment = psiElementFactory
				.createCommentFromText("//TODO: remove the BO here !!!", null);
		PsiField field = psiElementFactory
				.createFieldFromText("private " + objectType + " " + variable + ";", null);
		if (addTodo) {
			field.add(todoComment);
		}
		destinationClass.add(field);
		
		String setterName = "set" + variable.substring(0, 1).toUpperCase() + variable.substring(1);
		PsiMethod getter = psiElementFactory
				.createMethodFromText("public " + objectType + " " + getterName + "() {"
				                      + "return " + variable + ";"
				                      + "}", null);
		PsiMethod setter = psiElementFactory
				.createMethodFromText("public void " + setterName + "("
				                      + objectType + " " + variable + "){"
				                      + "this." + variable + " = " + variable
				                      + ";"
				                      + "}", null);
		if (addTodo) {
			getter.add(todoComment);
			setter.add(todoComment);
		}
		destinationClass.add(getter);
		destinationClass.add(setter);
	}
	
	@NotNull
	public PsiMethod generateToDtoMethod(@NotNull Project project,@NotNull  List<PsiMethod> gettersToInclude,
	                                      @NotNull String boName,@NotNull  String dtoName,
	                                      boolean useEntityTag) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		StringBuilder toDtoContent = new StringBuilder("public ").append(dtoName)
		                                                         .append(" toDto(").append(boName).append(" bo) {\n");
		toDtoContent.append("\t").append(dtoName).append(" dto = new ").append(dtoName).append("();\n");
		if(useEntityTag) {
			StringUtils.getInstance().addSetOfGetter(toDtoContent, "getEntityTag", "dto", "bo");
		}
		for (PsiMethod getter : gettersToInclude) {
			StringUtils.getInstance().addSetOfGetter(toDtoContent, getter.getName(), "dto", "bo");
		}
		toDtoContent.append("\treturn dto;\n").append("}\n");
		return elementFactory.createMethodFromText(toDtoContent.toString(), null);
	}
	
	@NotNull
	private PsiElement generateSaveToBo(@NotNull Project project, @Nullable PsiMethod findBy, 
	                                    @NotNull String pkGetter, String boName, String dtoName) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		
		String findByString = findBy != null ?  findBy.getContainingClass().getQualifiedName() + "." + findBy.getName()
		                                        + "(dto." + pkGetter + "());\n"
											: "null; //Could not generate the findBy. The BO or BOHome should have a findById or findBy" 
											  + StringUtils.getInstance().removeGetterPrefix(pkGetter, false) + " method.\n";
		
		String content = "public " + boName + " saveToBo(" + dtoName + " dto){\n"
		                 + "if (dto == null ) {\n"
		                 + "return null;\n"
		                 + "}\n"
		                 + boName + " bo = dto." + pkGetter + "() == null ? new "
		                 + boName + "() : "
		                 + findByString
		                 + "copyToBo(dto, bo);\n"
		                 + " return bo;\n"
		                 + "}";
		return elementFactory.createMethodFromText(content, null);
	}
	
	@NotNull
	public PsiMethod generateCopyToBoMethod(@NotNull Project project,@NotNull  List<PsiMethod> gettersToInclude,
	                                         @NotNull String boName,@NotNull  String dtoName,
	                                         boolean useEntityTag) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
		StringBuilder toBoContent = new StringBuilder("public void copyToBo(").append(dtoName)
		                                                                      .append(" dto, ").append(boName).append(" bo) {\n");
		if(useEntityTag) {
			StringUtils.getInstance().addSetOfGetter(toBoContent, "getEntityTag", "bo", "dto");
		}
		for (PsiMethod getter : gettersToInclude) {
			StringUtils.getInstance().addSetOfGetter(toBoContent, getter.getName(), "bo", "dto");
		}
		toBoContent.append("}\n");
		return elementFactory.createMethodFromText(toBoContent.toString(), null);
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
	
	public void addStaticInnerMappingClass(@NotNull PsiClass upperClass,
	                                       @NotNull List<PsiMethod> gettersToInclude,
	                                       @Nullable PsiMethod findByMethod,
	                                       @NotNull String pkGetterName,
	                                       @NotNull String boName, @NotNull String dtoName,
	                                       @NotNull String letterCode, boolean hasEntityTag) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(upperClass.getProject());
		String javaDocTxt = "/**\n"
		                    + " * Auto generated DTO/BO Mapper\n"
		                    + " *\n"
		                    + " * @author " + letterCode + "\n"
		                    + " */\n";
		
		PsiClass mapperClass = elementFactory.createClass(boName + dtoName + "Mapper");
		mapperClass.getModifierList().setModifierProperty(PsiModifier.PACKAGE_LOCAL, true);
		mapperClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
		PsiDocComment javadoc = elementFactory.createDocCommentFromText(javaDocTxt);
		mapperClass.addBefore(javadoc, mapperClass.getFirstChild());
		
		mapperClass.add(PsiHelper.getInstance()
		                         .generateToDtoMethod(upperClass.getProject(), gettersToInclude, 
		                                              boName, dtoName, hasEntityTag));
		mapperClass.add(PsiHelper.getInstance()
		                .generateSaveToBo(upperClass.getProject(), findByMethod, pkGetterName, boName, dtoName));
		               
		mapperClass.add(PsiHelper.getInstance()
		                         .generateCopyToBoMethod(upperClass.getProject(), gettersToInclude, 
		                                                 boName, dtoName, hasEntityTag));
		upperClass.addBefore(mapperClass, upperClass.getLastChild());
	}
	
	
	public List<PsiMethod> getGettersForPsiClass(PsiClass psiClass) {
		if(psiClass == null) {
			return Collections.emptyList();
		}
		List<PsiMethod> psiMethods = new ArrayList<>(PsiTreeUtil.collectElementsOfType(psiClass, PsiMethod.class));
		return psiMethods.stream().filter(this::isGetter).sorted(
				Comparator.comparing(PsiMethod::getName)).collect(Collectors.toList());
		
	}
	
	public boolean isPrimaryGetter(PsiMethod getter){
		PsiModifierList list = getter.getModifierList();
		PsiAnnotation annotation = list
				.findAnnotation("amos.share.databaseAccess.bo.AmosBeanInfo");
		return annotation != null && annotation.hasAttribute("primaryKey");
	}
	
	private boolean isGetter(PsiMethod psiMethod) {
		boolean result = false;
		if(psiMethod != null && psiMethod.hasModifier(JvmModifier.PUBLIC) &&
		   !psiMethod.hasModifier(JvmModifier.STATIC) &&
		   !PsiType.VOID.equals(psiMethod.getReturnType()) &&
		   psiMethod.getParameterList().isEmpty()) {
			String name = psiMethod.getName();
			result = name.matches("^(get|is|has).*$");
		}
		return result;
	}
	
	@Nullable
	public PsiMethod getFindByIdMethod(@NotNull Project project, @NotNull PsiClass boClass, @NotNull String pkName) {
		List<PsiMethod> psiMethods = new ArrayList<>(
				PsiTreeUtil.collectElementsOfType(boClass, PsiMethod.class));
		String findByPk = "findBy" + StringUtils.getInstance().removeGetterPrefix(pkName, false);
		boolean hasFindMethods = psiMethods.stream().map(PsiMethod::getName).anyMatch(
				name -> name.toLowerCase().startsWith("findby"));
		PsiMethod result = null;
		if (hasFindMethods) {
			result = psiMethods.stream()
			                   .filter(method -> method.getName().toLowerCase().equals(findByPk))
			                   .findFirst().orElse(null);
			if (result == null) {
				result = psiMethods.stream().filter(method -> method.getName().toLowerCase()
				                                                    .startsWith("findbyid"))
				                   .findFirst().orElse(null);
			}
		} else {
			JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
			PsiClass homeClass = javaPsiFacade.findClass(boClass.getQualifiedName() + "Home",
			                                             GlobalSearchScope.allScope(project));
			psiMethods = new ArrayList<>(
					PsiTreeUtil.collectElementsOfType(homeClass, PsiMethod.class));
			hasFindMethods = psiMethods.stream().map(PsiMethod::getName)
			                           .anyMatch(name -> name.toLowerCase().startsWith("findby"));
			if (hasFindMethods) {
				result = psiMethods.stream().filter(method -> method.getName()
				                                                    .equalsIgnoreCase(findByPk)).findFirst()
				                   .orElse(null);
				if (result == null) {
					result = psiMethods.stream().filter(method -> method.getName().toLowerCase()
					                                                    .startsWith("findbyid"))
					                   .findFirst().orElse(null);
				}
			}
		}
		return result;
	}
}
