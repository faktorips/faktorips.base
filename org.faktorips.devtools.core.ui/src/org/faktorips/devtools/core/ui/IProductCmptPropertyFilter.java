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
 * Allows to filter {@link IProductCmptProperty product component properties} from UI elements.
 * <p>
 * A filter states whether a given {@link IProductCmptProperty} is filtered at a given time by means
 * of the {@link #isFiltered(IProductCmptProperty)} method.
 * <p>
 * Furthermore, the filter is responsible for notifying the {@link IPropertyVisibleController} when
 * the filtering conditions change. For example, it could be that the filtering depends on the
 * current perspective. In this case, the filter should notify the controller upon perspective
 * change.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptPropertyFilter {

    /**
     * Sets the {@link IPropertyVisibleController} that will be responsible for managing this
     * {@link IProductCmptPropertyFilter}.
     */
    public void setPropertyVisibleController(IPropertyVisibleController controller);

    /**
     * Returns whether the given {@link IProductCmptProperty product component property} is
     * filtered.
     * 
     * @param property the {@link IProductCmptProperty product component property} to filter or not
     */
    public boolean isFiltered(IProductCmptProperty property);

    /**
     * Notifies the {@link IPropertyVisibleController} that manages this
     * {@link IProductCmptPropertyFilter} that the filtering conditions changed.
     */
    public void notifyController();

}
