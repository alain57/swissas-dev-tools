package com.swissas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StringUtils {
	private static final Pattern LETTER_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}$");
	private static final Pattern LETTER_CODE_PATTERN_WITH_NAME = Pattern.compile("^[A-Z]{2,4} \\(.+\\)$");
	private static final Pattern GETTER_PREFIX_PATTERN = Pattern.compile("^(get|is|has|are)([A-Z].*)$");
	private static final String JAVA_EXTENSION = ".java";
	private static final StringUtils INSTANCE = new StringUtils();
	
	private StringUtils() {
		//do nothing
	}
	
	public static StringUtils getInstance() {
		return INSTANCE;
	}
	
	/**
	 * The @NotNull annotations are not instrumented anymore by the build, so the contract is enforced explicitly.
	 */
	private static void requireText(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
	}
	
	public boolean isLetterCode(@NotNull String text) {
		requireText(text);
		return text.isEmpty() || LETTER_CODE_PATTERN.matcher(text).find();
	}
	
	public boolean isLetterCodeWithName(@NotNull String text) {
		requireText(text);
		return text.isEmpty() || LETTER_CODE_PATTERN_WITH_NAME.matcher(text).find();
	}
	
	public boolean isPositiveNumber(@NotNull String text){
		requireText(text);
		int i;
		try {
			i = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			i = -1;
		}
		
		return i >= 0;
	}
	
	public boolean isValidLetterCode(@Nullable String potentialLetterCode) {
		return SwissAsStorage.getInstance().getUserMap().containsKey(potentialLetterCode);
	}
	
	public String removeJavaEnding(@NotNull String name) {
		return name.endsWith(JAVA_EXTENSION) ? name.substring(0, name.length() - JAVA_EXTENSION.length()) : name;
	}
	
	public String removeGetterPrefix(@NotNull String name) {
		return removeGetterPrefix(name, true);
	}
	
	public String removeGetterPrefix(@NotNull String name, boolean firstChatLowerCase) {
		String result = null;
		Matcher matcher = GETTER_PREFIX_PATTERN.matcher(name);
		if(matcher.find()) {
			String match = matcher.group(2);
			result = match.substring(0, 1);
			if(firstChatLowerCase) {
				result = result.toLowerCase();
			}
			result+= match.substring(1); 
		}
		return result;
	}
	
	
	public boolean isGetter(@NotNull String valueToCheck) {
		return GETTER_PREFIX_PATTERN.matcher(valueToCheck).find();
	}
	
	public void addSetOfGetter(@NotNull StringBuilder sb,@NotNull String getterName, @Nullable String pkgGetter, boolean isDtoToBo) {
		String variableToSet = isDtoToBo ? "bo" : "dto";
		String variableToGet = isDtoToBo ? "dto" : "bo";
		
		String variable = removeGetterPrefix(getterName);
		if (variable == null) {
			throw new IllegalArgumentException("not a getter : " + getterName);
		}
		String setterName = "set" + variable.substring(0, 1).toUpperCase() + variable.substring(1);
		boolean addIf = getterName.equals(pkgGetter) && !isDtoToBo;
		if(addIf) {
			sb.append("\tif(bo.").append(getterName).append("() != null ) {\n");
		}
		sb.append("\t").append(variableToSet).append(".")
		  .append(setterName).append("(").append(variableToGet).append(".")
		  .append(getterName).append("()").append(");\n");
		if(addIf) {
			sb.append("\t}\n");
		}
	}
	
}
