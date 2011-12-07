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

package org.faktorips.devtools.core.ui;

import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Used to mark UI elements that are able to adjust themselves dynamically to the registered
 * {@link IProductCmptPropertyFilter product component property filters}.
 * <p>
 * For example, the contents shown by an editor might be filtered by the registered
 * {@link IProductCmptPropertyFilter product component property filters}. Usually, the filtering is
 * performed upon instantiating the editor controls. However, filters might yield different results
 * as soon as another perspective is activated. An editor implementing this interface states that it
 * is capable to react accordingly to this change via the {@link #adjustToFilter()} method.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptPropertyFilter
 * @see IProductCmptProperty
 */
public interface IProductCmptPropertyFilterUpdateSupport {

    /**
     * Adjusts this UI element to the registered {@link IProductCmptPropertyFilter product component
     * property filters}.
     * 
     * @see IProductCmptPropertyFilter
     */
    public void adjustToFilter();

}
