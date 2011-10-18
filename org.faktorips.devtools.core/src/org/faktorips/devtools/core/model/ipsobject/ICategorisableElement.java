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

package org.faktorips.devtools.core.model.ipsobject;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Used to mark elements that can be assigned to categories.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface ICategorisableElement extends IIpsElement {

    public static final String PROPERTY_CATEGORY = "category"; //$NON-NLS-1$

    /**
     * Returns the category this element is assigned to.
     */
    public String getCategory();

    /**
     * Sets the name of the category this element is assigned to.
     * 
     * @param category The name of the category this element is assigned to
     */
    public void setCategory(String category);

}
