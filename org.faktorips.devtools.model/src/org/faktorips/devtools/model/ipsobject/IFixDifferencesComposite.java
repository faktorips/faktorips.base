/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.IIpsMetaObject;

/**
 * This interface is used for a composite of fix difference elements. The fix difference framework
 * is used to indicate and fix the differences between {@link IIpsMetaObject} and the corresponding
 * {@link IIpsMetaClass}. Differences may happen accrue if the model changes while there are
 * existing product definition objects. In such cases we want to give as much help to the user as
 * possible to get the product definition object in synch with the model.
 * 
 * @author dirmeier
 */
public interface IFixDifferencesComposite extends IAdaptable {

    /**
     * Returns true if this element as well as every child element is empty. That means there is no
     * work to do, the method {@link #fixAllDifferencesToModel()} would do nothing.
     * 
     * @return true if this element and every child element is empty
     */

    boolean isEmpty();

    /**
     * Fixes the difference between the type and the product component. This method should fix the
     * differences in this element and call the {@link #fixAllDifferencesToModel()} method in every
     * child element.
     * <p>
     * For example if the type contains a new attribute but the product component generation. has
     * not matching attribute value, this method creates the attribute value.
     */
    void fixAllDifferencesToModel();

    /**
     * An {@link IFixDifferencesComposite} would have children that also contains fixes. This method
     * getting the containing fix differences elements
     * 
     * @return a list of {@link IFixDifferencesComposite IFixDifferencesComposites} that are
     *             children of this element
     */
    List<IFixDifferencesComposite> getChildren();

    /**
     * Getting the {@link IIpsElement} that corresponds to this {@link IFixDifferencesComposite}.
     * This object is used as base element for displaying labels or images for this element.
     * 
     * @return The {@link IIpsElement} that corresponds to this {@link IFixDifferencesComposite}
     */
    IIpsElement getCorrespondingIpsElement();

}
