/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.filter;

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
 * @deprecated as of 3.17. Filters no longer have a reference to a controller. Thus this base class
 *             is no longer required.
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptPropertyFilter
 */
@Deprecated
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
