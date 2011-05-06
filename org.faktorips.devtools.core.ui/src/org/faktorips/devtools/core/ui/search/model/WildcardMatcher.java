/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

/**
 * Matcher mit oder ohne Wildcards
 * 
 * TODO subclassing
 * 
 * @author dicker
 */
public class WildcardMatcher {

    private static final String WILDCARD_ZERO_OR_MANY = "*"; //$NON-NLS-1$
    private static final String WILDCARD_ONCE = "?"; //$NON-NLS-1$

    private static final String REGEXP_ZERO_OR_MANY = ".*"; //$NON-NLS-1$
    private static final String REGEXP_ONCE = "."; //$NON-NLS-1$

    private Pattern pattern;
    private boolean isRegExp;
    private boolean isValidPattern;

    public WildcardMatcher(String searchTerm) {

        isRegExp = searchTerm.contains(WILDCARD_ZERO_OR_MANY) || searchTerm.contains(WILDCARD_ONCE);

        String regexpSearchTerm;
        if (isRegExp) {
            regexpSearchTerm = searchTerm.replace(WILDCARD_ZERO_OR_MANY, REGEXP_ZERO_OR_MANY).replace(WILDCARD_ONCE,
                    REGEXP_ONCE);
        } else {
            regexpSearchTerm = searchTerm + REGEXP_ZERO_OR_MANY;
        }

        try {
            pattern = Pattern.compile(regexpSearchTerm, Pattern.CASE_INSENSITIVE);
            isValidPattern = true;
        } catch (PatternSyntaxException e) {
            isValidPattern = false;
        }

    }

    public boolean isMatching(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        if (!isValidPattern) {
            return false;
        }

        return pattern.matcher(text).matches();
    }
}
