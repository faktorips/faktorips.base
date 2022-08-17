/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.value;

import java.util.Locale;
import java.util.Objects;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;

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

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !getClass().equals(obj.getClass())) {
            return false;
        }
        AbstractValue<?> value = (AbstractValue<?>)obj;
        return Objects.equals(getContentAsString(), value.getContentAsString());
    }

    @Override
    public int hashCode() {
        return getContentAsString().hashCode();
    }

}
