/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

/**
 * Matcher for Strings, which uses ? and * as wildcard for one are many characters
 * 
 * @author dicker
 */
public class WildcardMatcher implements IMatcher<String> {

    private static final String WILDCARD_ZERO_OR_MANY = "*"; //$NON-NLS-1$
    private static final String WILDCARD_ONCE = "?"; //$NON-NLS-1$

    private static final String REGEXP_ZERO_OR_MANY = ".*"; //$NON-NLS-1$
    private static final String REGEXP_ONCE = "."; //$NON-NLS-1$

    private final Pattern pattern;

    public WildcardMatcher(String searchTerm) {

        boolean isRegExp = searchTerm.contains(WILDCARD_ZERO_OR_MANY) || searchTerm.contains(WILDCARD_ONCE);

        String regexpSearchTerm;
        if (isRegExp) {
            regexpSearchTerm = searchTerm.replace(WILDCARD_ZERO_OR_MANY, REGEXP_ZERO_OR_MANY).replace(WILDCARD_ONCE,
                    REGEXP_ONCE);
        } else {
            regexpSearchTerm = searchTerm + REGEXP_ZERO_OR_MANY;
        }

        pattern = createPattern(regexpSearchTerm);
    }

    private Pattern createPattern(String regexpSearchTerm) {
        try {
            return Pattern.compile(regexpSearchTerm, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    @Override
    public boolean isMatching(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        if (pattern == null) {
            return false;
        }

        return pattern.matcher(text).matches();
    }
}
