/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

    /**
     * Returns the projects default language.
     * 
     * @param ipsProject the actual IpsProject
     */
    private Locale getDefaultLanguage(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }
}
