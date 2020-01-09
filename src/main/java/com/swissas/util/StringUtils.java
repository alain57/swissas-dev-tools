package com.swissas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StringUtils {
	private static final Pattern LETTER_CODE_PATTERN = Pattern.compile("^[A-Z]{3,4}$");
	private static final Pattern LETTER_CODE_PATTERN_WITH_NAME = Pattern.compile("^[A-Z]{3,4} \\(.+\\)$");
	private static final Pattern GETTER_PREFIX_PATTERN = Pattern.compile("^(get|is|has)(.*)$");
	private static StringUtils instance;
	
	private StringUtils() {
		//do nothing
	}
	
	public static StringUtils getInstance() {
		if(instance == null) {
			instance = new StringUtils();
		}
		return instance;
	}
	
	public boolean isLetterCode(@NotNull String text) {
		return text.isEmpty() || LETTER_CODE_PATTERN.matcher(text).find();
	}
	
	public boolean isLetterCodeWithName(@NotNull String text) {
		return text.isEmpty() || LETTER_CODE_PATTERN_WITH_NAME.matcher(text).find();
	}
	
	public boolean isPositiveNumber(@NotNull String text){
		int i;
		try {
			i = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			i = -1;
		}
		
		return i >= 0;
	}
	
	public boolean isValidLetterCode(String potentialLetterCode) {
		return SwissAsStorage.getInstance().getUserMap().containsKey(potentialLetterCode);
	}
	
	public String removeJavaEnding(@NotNull String name) {
		return name.indexOf('.') == -1 ? name :name.substring(0,name.indexOf(".java"));
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
	
	
	public void addSetOfGetter(@NotNull StringBuilder sb,@NotNull String getterName, @Nullable String pkgGetter, boolean isDtoToBo) {
		String variableToSet = isDtoToBo ? "bo" : "dto";
		String variableToGet = isDtoToBo ? "dto" : "bo";
		
		String variable = StringUtils.getInstance().removeGetterPrefix(getterName);
		String setterName = "set" + variable.substring(0, 1).toUpperCase() + variable.substring(1);
		boolean addIf = getterName.equals(pkgGetter) && !isDtoToBo;
		if(addIf) {
			sb.append("\tif(dto.").append(getterName).append("() != null ) {");
		}
		sb.append("\t").append(variableToSet).append(".")
		  .append(setterName).append("(").append(variableToGet).append(".")
		  .append(getterName).append("()").append(");\n");
		if(addIf) {
			sb.append("}\n");
		}
	}
	
}
