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