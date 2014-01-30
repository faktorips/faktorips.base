/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.value;

import java.util.Locale;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;

/**
 * Abstract class for {@link IValue}.
 * 
 * @author frank
 * @since 3.9
 */
public abstract class AbstractValue<T> implements IValue<T> {

    @Override
    public String getLocalizedContent(Locale locale) {
        return getContentAsString();
    }

    @Override
    public String getDefaultLocalizedContent(IIpsProject ipsProject) {
        return getLocalizedContent(getDefaultLanguage(ipsProject));
    }

    @Override
    public String getLocalizedContent() {
        return getLocalizedContent(null);
    }

    /**
     * Returns the projects default language.
     * 
     * @param ipsProject the actual IpsProject
     */
    private Locale getDefaultLanguage(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }
}
