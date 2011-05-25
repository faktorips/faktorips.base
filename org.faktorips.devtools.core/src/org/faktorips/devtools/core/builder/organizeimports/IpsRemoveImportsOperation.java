/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder.organizeimports;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a util class to remove unused import statements after code generation.
 * 
 * 
 * @author dirmeier
 */
public class IpsRemoveImportsOperation {

    private final static String EMPTY = ""; //$NON-NLS-1$

    private static Pattern oneLineCommentsPattern = Pattern.compile("//.*"); //$NON-NLS-1$

    // multiple line comment pattern ignoring java doc comments
    private static Pattern multiLineCommentsPattern = Pattern.compile("/\\*[^\\*][\\s\\S]*?\\*/"); //$NON-NLS-1$

    // http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.7
    // (?<![\p{Alpha}0-9_$\.])\p{Alpha}[\p{Alpha}0-9_$]*
    // The first part is \p{Alpha} to find all words beginning with a letter
    // The second part is any matches of letters, numbers and the characters _ and $
    private static String wordPatternString = "(\\p{Alpha}[\\p{L}0-9_$]*)"; //$NON-NLS-1$

    // The first part is a negative look ahead to avoid words beginning with . (e.g. method names)
    private static Pattern relevantWordPattern = Pattern.compile("(?<![\\p{L}0-9_$\\.])" + wordPatternString); //$NON-NLS-1$

    private static Pattern importPattern = Pattern
            .compile(" *import[\\.\\s\\p{L}0-9_$]*\\.[\\s]*" + wordPatternString + "[\\s]*; *[\\r\\n]?"); //$NON-NLS-1$ //$NON-NLS-2$

    public String removeUnusedImports(String input) {
        String withoutComments = removeComments(input);
        String withoutImports = removeImports(withoutComments);
        Set<String> words = getRelevantWords(withoutImports);
        String inputWithRemovedImports = removeImports(input, words);

        return inputWithRemovedImports;
    }

    private String removeImports(String input, Set<String> words) {
        StringBuilder inputWithRemovedImports = new StringBuilder();
        Matcher importMatcher = importPattern.matcher(input);
        int lastEnd = 0;
        while (importMatcher.find()) {
            inputWithRemovedImports.append(input.substring(lastEnd, importMatcher.start()));
            String importWord = importMatcher.group(1);
            if (words.contains(importWord)) {
                inputWithRemovedImports.append(input.substring(importMatcher.start(), importMatcher.end()));
            }
            lastEnd = importMatcher.end();
        }
        inputWithRemovedImports.append(input.substring(lastEnd));
        return inputWithRemovedImports.toString();
    }

    private Set<String> getRelevantWords(String input) {
        Matcher matcher = relevantWordPattern.matcher(input);
        Set<String> words = new HashSet<String>();
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }

    private String removeComments(String input) {
        String removedMultiLineComments = multiLineCommentsPattern.matcher(input).replaceAll(EMPTY);
        return oneLineCommentsPattern.matcher(removedMultiLineComments).replaceAll(EMPTY);
    }

    private String removeImports(String input) {
        Matcher matcher = importPattern.matcher(input);
        return matcher.replaceAll(EMPTY);
    }

}
