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

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Manages the visibility of UI elements that are based upon {@link IProductCmptProperty product
 * component properties}, which can be filtered using {@link IProductCmptPropertyFilter product
 * component property filters}.
 * <p>
 * Multiple {@link IProductCmptPropertyFilter product component property filters} can be added to
 * this controller. Each filter implements it's own logic to trigger the controller's
 * {@link #updateUI()} method, which adjusts the visibility of the mapped controls.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IPropertyVisibleController {

    /**
     * Adjusts the visibility of the controls that have been registered to this controller by means
     * of the {@link #addPropertyControlMapping(Control, IProductCmptProperty, Control...)}.
     */
    public void updateUI();

    /**
     * Registers the indicated {@link IProductCmptProperty product component property} to this
     * controller and associated the given controls with the {@link IProductCmptProperty product
     * component property}.
     * <p>
     * Returns {@code true} if no such mapping already existed, or if the mapping has changed trough
     * the invocation. Returns {@code false} otherwise.
     * <p>
     * <strong>Important:</strong> Clients must not forget to remove the mapping as soon as all
     * controls associated to the {@link IProductCmptProperty} are disposed by means of the
     * {@link #removePropertyControlMapping(Control)} method.
     * 
     * @param outerControl the control for which to create the mapping. This should normally be a
     *            common parent of the mapped controls. The information is required to be able to
     *            handle the fact that multiple editors can be opened at the same time
     * @param property the {@link IProductCmptProperty} to register to this controller
     * @param controls the controls to associated with the {@link IProductCmptProperty}
     */
    public boolean addPropertyControlMapping(Control outerControl, IProductCmptProperty property, Control... controls);

    /**
     * Removes all mappings for the indicated control from this controller.
     * <p>
     * Returns {@code true} if at least one mapping existed for the indicated control, {@code false}
     * otherwise.
     * 
     * @param outerControl the control for which to remove all mappings
     */
    public boolean removePropertyControlMapping(Control outerControl);

    /**
     * Adds the indicated {@link IProductCmptPropertyFilter} to this controller.
     * <p>
     * Returns {@code true} if the indicated {@link IProductCmptPropertyFilter} has not been added
     * to this controller before, {@code false} otherwise.
     * 
     * @param filter the {@link IProductCmptPropertyFilter} to add to this controller
     */
    public boolean addFilter(IProductCmptPropertyFilter filter);

    /**
     * Removes the indicated {@link IProductCmptPropertyFilter} from this controller.
     * <p>
     * Returns {@code true} if the indicated {@link IProductCmptPropertyFilter} was really removed
     * from this controller. Returns {@code false} if no such {@link IProductCmptPropertyFilter} was
     * added to this controller.
     * 
     * @param filter the {@link IProductCmptPropertyFilter} to remove from this controller
     */
    public boolean removeFilter(IProductCmptPropertyFilter filter);

}
