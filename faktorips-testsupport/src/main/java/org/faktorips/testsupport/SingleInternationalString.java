/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testsupport;

import java.util.Locale;

import org.faktorips.values.InternationalString;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Implementation of {@link InternationalString} which uses the same text for all locales.
 */
public class SingleInternationalString implements InternationalString {

    private static final long serialVersionUID = 1L;

    private final String text;

    /**
     * Constructor.
     * 
     * @param text the value string used for all locales.
     */
    public SingleInternationalString(String text) {
        this.text = text;
    }

    /**
     * Getting the value string which is independent from the specified locale.
     * 
     * @param locale this parameter is not used
     */
    @Override
    public String get(@CheckForNull Locale locale) {
        return text;
    }
}
