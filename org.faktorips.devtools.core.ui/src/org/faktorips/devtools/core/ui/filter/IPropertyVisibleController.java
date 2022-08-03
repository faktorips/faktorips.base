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

import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * Manages the visibility of UI elements that are based upon {@link IProductCmptProperty product
 * component properties}, which can be filtered using {@link IProductCmptPropertyFilter product
 * component property filters}.
 * <p>
 * Multiple {@link IProductCmptPropertyFilter product component property filters} can be added to
 * this controller. Each filter implements it's own logic to trigger the controller's
 * {@link #updateUI(boolean)} method, which adjusts the visibility of the mapped controls.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IPropertyVisibleController {

    /**
     * Adjusts the visibility of the controls that have been registered to this controller by means
     * of the {@link #addPropertyControlMapping(Control, IProductCmptProperty, Control...)}.
     * 
     * @param refresh if <code>true</code> a full refresh needs to be performed that means the
     *            refresh callback installed by {@link #setRefreshCallback(Runnable)} is executed
     */
    void updateUI(boolean refresh);

    /**
     * Registers the indicated {@link IProductCmptProperty product component property} to this
     * controller and associated the given controls with the {@link IProductCmptProperty product
     * component property}.
     * <p>
     * Returns {@code true} if no such mapping already existed, or if the mapping has changed trough
     * the invocation. Returns {@code false} otherwise.
     * <p>
     * Adding a property control mapping automatically updates the visible state of all controls. So
     * you have to make sure that all filters have been added before you add property control
     * mappings.
     * <p>
     * <strong>Important:</strong> Clients must not forget to remove the mapping as soon as all
     * controls associated to the {@link IProductCmptProperty} are disposed by means of the
     * {@link #removePropertyControlMapping(Control)} method.
     * 
     * @param containerControl the control for which to create the mapping. This should normally be
     *            a common parent of the mapped controls. The information is required to be able to
     *            handle the fact that multiple editors can be opened at the same time
     * @param property the {@link IProductCmptProperty} to register to this controller
     * @param controls the controls to associated with the {@link IProductCmptProperty}
     */
    boolean addPropertyControlMapping(Control containerControl,
            IProductCmptProperty property,
            Control... controls);

    /**
     * Removes all mappings for the indicated control from this controller.
     * <p>
     * Returns {@code true} if at least one mapping existed for the indicated control, {@code false}
     * otherwise.
     * 
     * @param containerControl the control for which to remove all mappings
     */
    boolean removePropertyControlMapping(Control containerControl);

    /**
     * Adds the indicated {@link IProductCmptPropertyFilter} to this controller.
     * <p>
     * Returns {@code true} if the indicated {@link IProductCmptPropertyFilter} has not been added
     * to this controller before, {@code false} otherwise.
     * <p>
     * The filters should be added to the controller before the property control mappings are added
     * 
     * @param filter the {@link IProductCmptPropertyFilter} to add to this controller
     */
    boolean addFilter(IProductCmptPropertyFilter filter);

    /**
     * Removes the indicated {@link IProductCmptPropertyFilter} from this controller.
     * <p>
     * Returns {@code true} if the indicated {@link IProductCmptPropertyFilter} was really removed
     * from this controller. Returns {@code false} if no such {@link IProductCmptPropertyFilter} was
     * added to this controller.
     * 
     * @param filter the {@link IProductCmptPropertyFilter} to remove from this controller
     */
    boolean removeFilter(IProductCmptPropertyFilter filter);

    /**
     * Checks, whether controls belonging to the given {@link IProductCmptProperty} must be filtered
     * or not.
     * <p>
     * Return {@code true}, if there is an {@link IProductCmptPropertyFilter}, which filters the
     * given property. Return {@code false} otherwise.
     * 
     */
    boolean isFiltered(IProductCmptProperty property);

    /**
     * Adds the list of filters to this controller. Useful when adding multiple filters at once.
     */
    void addFilters(List<IProductCmptPropertyFilter> filters);

    /**
     * Installs a callback function that is executed after the filter has changed to inform the
     * caller that a refresh needs to be performed.
     * 
     * @param callback The callback that is executed when refresh is required.
     */
    void setRefreshCallback(Runnable callback);
}
