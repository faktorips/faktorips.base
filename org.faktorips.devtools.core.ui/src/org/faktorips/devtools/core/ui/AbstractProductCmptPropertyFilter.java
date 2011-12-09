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
 * Base implementation of {@link IProductCmptPropertyFilter} that simplifies the implementation of
 * filters.
 * <p>
 * <strong>Subclassing:</strong><br>
 * Subclasses must implement the {@link #isFiltered(IProductCmptProperty)} method to indicate
 * whether a given {@link IProductCmptProperty} is filtered at a given time. Furthermore, subclasses
 * are responsible to make sure to call {@link #notifyController()} as soon as the filtering
 * conditions change.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptPropertyFilter
 */
public abstract class AbstractProductCmptPropertyFilter implements IProductCmptPropertyFilter {

    private IPropertyVisibleController controller;

    @Override
    public void setPropertyVisibleController(IPropertyVisibleController controller) {
        this.controller = controller;
    }

    @Override
    public final void notifyController() {
        controller.updateUI();
    }

}
