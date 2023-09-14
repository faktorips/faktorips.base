/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.organizeimports;

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

    private static final String EMPTY = ""; //$NON-NLS-1$

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
            .compile("(?<!\\S) *import\\s[\\.\\s\\p{L}0-9_$]*\\.[\\s]*" + wordPatternString + "[\\s]*; *\\r?\\n?"); //$NON-NLS-1$ //$NON-NLS-2$

    public String removeUnusedAndDuplicateImports(String input) {
        String withoutComments = removeComments(input);
        String withoutImports = removeImports(withoutComments);
        Set<String> words = getRelevantWords(withoutImports);
        return removeImports(input, words);
    }

    private String removeImports(String input, Set<String> words) {
        Set<String> imports = new HashSet<>();
        StringBuilder inputWithRemovedImports = new StringBuilder();
        Matcher importMatcher = importPattern.matcher(input);
        int lastEnd = 0;
        while (importMatcher.find()) {
            inputWithRemovedImports.append(input.substring(lastEnd, importMatcher.start()));
            if (imports.add(importMatcher.group(0))) {
                String importWord = importMatcher.group(1);
                if (words.contains(importWord)) {
                    inputWithRemovedImports.append(input.substring(importMatcher.start(), importMatcher.end()));
                }
            }
            lastEnd = importMatcher.end();
        }
        inputWithRemovedImports.append(input.substring(lastEnd));
        return inputWithRemovedImports.toString();
    }

    private Set<String> getRelevantWords(String input) {
        Matcher matcher = relevantWordPattern.matcher(input);
        Set<String> words = new HashSet<>();
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
