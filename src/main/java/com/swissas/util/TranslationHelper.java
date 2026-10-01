package com.swissas.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The text manipulations needed to translate a string (property key generation, replacement of the shared words, ...).
 * They don't need any IntelliJ object, therefore they can be unit tested.
 *
 * @author Tavan Alain
 */
public final class TranslationHelper {
	private static final int MAX_KEY_LENGTH = 36;
	
	private TranslationHelper() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Converts a property value to the upper case key it is stored with.
	 * ex : "Hello world %s" is converted to "HELLO_WORLD"
	 */
	@NotNull
	public static String convertPropertyStringToKey(@NotNull String propertyString) {
		String withoutPercent = propertyString.replaceAll("(%s)+", "_");
		String capitalizeFully = StringUtil.unescapeStringCharacters(withoutPercent)
				.toUpperCase().replaceAll("[^A-Z0-9 ]", "")
				.replace(" ", "_");
		if (capitalizeFully.startsWith("_")) {
			capitalizeFully = capitalizeFully.substring(1);
		}
		if (capitalizeFully.endsWith("_")) {
			capitalizeFully = capitalizeFully.substring(0, capitalizeFully.length() - 1);
		}
		if (capitalizeFully.length() > MAX_KEY_LENGTH) {
			capitalizeFully = capitalizeFully.substring(0, MAX_KEY_LENGTH);
		}
		return capitalizeFully;
	}
	
	/**
	 * @return a key that does not exist yet, a number is added in case of duplicate (ex : KEY_TXT, KEY_1_TXT).
	 */
	@NotNull
	public static String generateFullKey(@NotNull Set<String> existingKeys, @NotNull String propertyValue,
	                                     @NotNull String ending) {
		String translatedKey = convertPropertyStringToKey(propertyValue);
		String fullKey = translatedKey + ending;
		int numberInCaseOfDuplicateKey = 0;
		while (existingKeys.contains(fullKey)) {
			numberInCaseOfDuplicateKey++;
			fullKey = translatedKey + "_" + numberInCaseOfDuplicateKey + ending;
		}
		return fullKey;
	}
	
	private static final List<Map.Entry<Pattern, String>> COMMON_MISTAKES = List.of(
			mistake("\\b[wW]ork[ -]?[oO]rder\\b", "@WORKORDER@"),
			mistake("\\bWO\\b", "@WO@"),
			mistake("\\baircraft\\b", "@AIRCRAFT@"),
			mistake("\\bAC\\b", "@AC@"),
			mistake("\\b[pP]art[ -]?[nN]umber\\b", "@PART_NUMBER@"),
			mistake("\\bPN\\b", "P/N"),
			mistake("\\b[sS]erial[ -]?[nN]umber\\b", "@SERIAL_NUMBER@"),
			mistake("\\bSN\\b", "@SN@"),
			mistake("\\bAmos\\b", "@AMOS@"),
			mistake("[wW]ork[ -]?[pP]ackage\\b", "@WORKPACKAGE@"),
			mistake("\\bWP\\b", "@WP@"),
			mistake("\\b([aA])nalyze\\b", "$1nalyse"),
			mistake("\\bCenter\\b", "Centre"));
	
	private static Map.Entry<Pattern, String> mistake(String regex, String replacement) {
		return Map.entry(Pattern.compile(regex), replacement);
	}
	
	@Nullable
	public static String autoCorrectCommonMistakes(@Nullable String sentence) {
		if (sentence == null) {
			return null;
		}
		String result = sentence;
		for (Map.Entry<Pattern, String> mistake : COMMON_MISTAKES) {
			result = mistake.getKey().matcher(result).replaceAll(mistake.getValue());
		}
		return result;
	}
		/**
	 * Replaces the words that already exist in the shared translation file by their reference (@KEY@).
	 * The shared values are plain texts, not regular expressions.
	 */
	@NotNull
	public static String replaceWithKnownKeys(@NotNull String sentence, @NotNull Map<?, ?> sharedProperties) {
		String result = sentence;
		for (Map.Entry<?, ?> entry : sharedProperties.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null || entry.getValue().toString().isEmpty()) {
				continue; //an empty value would match between every character
			}
			String key = "@" + entry.getKey() + "@";
			Pattern value = Pattern.compile("\\b" + Pattern.quote(entry.getValue().toString()) + "\\b");
			result = value.matcher(result).replaceAll(Matcher.quoteReplacement(key));
		}
		return result;
	}
}
