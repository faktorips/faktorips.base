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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * This interface can be implemented by any {@link IpsElementWorkbenchAdapter} that needs to provide
 * labels in plural form.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.1
 * 
 * @see IpsElementWorkbenchAdapter
 */
public interface IPluralLabelWorkbenchAdapter {

    /**
     * Returns the plural label for the given IPS element.
     * 
     * @param element The IPS element to retrieve the plural label for.
     */
    public String getPluralLabel(IIpsElement element);

}
