/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
     * Returns whether the given {@link IProductCmptProperty product component property} is
     * filtered.
     * 
     * @param property the {@link IProductCmptProperty product component property} to filter or not
     */
    public boolean isFiltered(IProductCmptProperty property);

    /**
     * Sets the {@link IPropertyVisibleController} that will be responsible for managing this
     * {@link IProductCmptPropertyFilter}.
     * 
     * @deprecated as of 3.17. Filters might be global while controllers exist per editor, thus such
     *             an association is no longer possible.
     */
    @Deprecated
    public void setPropertyVisibleController(IPropertyVisibleController controller);

    /**
     * Notifies the {@link IPropertyVisibleController} that manages this
     * {@link IProductCmptPropertyFilter} that the filtering conditions changed.
     * 
     * @deprecated as of 3.17. Filters can no longer notify controllers. Update Editors manually.
     */
    @Deprecated
    public void notifyController();

}
