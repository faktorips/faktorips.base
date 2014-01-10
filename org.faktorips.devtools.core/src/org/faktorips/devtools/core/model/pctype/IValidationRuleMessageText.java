/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

import org.faktorips.devtools.core.model.IInternationalString;

public interface IValidationRuleMessageText extends IInternationalString {

    // matching a text that follows '{' and is followed by '}' or ','
    public static final Pattern REPLACEMENT_PARAMETER_REGEXT = Pattern.compile("(?<=(\\{))[\\p{L}0-9_$]+(?=([,\\}]))"); //$NON-NLS-1$

    /**
     * Extracting the replacement parameters from messageText. The replacement parameters are
     * defined curly braces. In contrast to the replacement parameters used in {@link MessageFormat}
     * , these parameters could have names and not only indices. However you could use additional
     * format information separated by comma as used by {@link MessageFormat}.
     * 
     */
    public abstract LinkedHashSet<String> getReplacementParameters();

}