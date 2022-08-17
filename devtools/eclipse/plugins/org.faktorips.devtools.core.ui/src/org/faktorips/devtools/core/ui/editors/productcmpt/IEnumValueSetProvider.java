/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IEnumValueSet;

/**
 * Provider used when enum valuesets are configured. E.g. when an enum value set in the model
 * (policy component type attribute) is configured with a subset by a product component (config
 * element). In above case the model valueset will be returned by {@link #getSourceEnumValueSet()}.
 * {@link #getTargetConfiguredValueSet()} will return the config element of the product component,
 * that defines the subset of the source value set.
 * <p>
 * However there are other cases of configuring valuesets against each other. E.g. product variants
 * allow to restrict valuesets defined in a product component even further. Those cases can be
 * represented by different implentations of this interface.
 * <p>
 * The {@link AnyValueSetControl} can be configured with a custom {@link IEnumValueSetProvider} to
 * allow arbitrary pairs of (enum-)valueset and config element defining a subset.
 * 
 * @see AnyValueSetControl
 * 
 * @author Stefan Widmaier
 */
public interface IEnumValueSetProvider {

    /**
     * The base-valueset, e.g. the valueset defined in the model (by a product component type
     * attribute).
     * <p>
     * This method may return <code>null</code> if there is no soure valueset. That is the case if
     * the model does not define a concrete valueset.
     * 
     * @return returns the valueset a subset is created of, or <code>null</code> if there is none.
     */
    IEnumValueSet getSourceEnumValueSet();

    /**
     * Returns the label that is to be displayed above the source values list.
     */
    String getSourceLabel();

    /**
     * Returns the {@link IConfiguredValueSet} that defines and contains a subset of the
     * source-valueset.
     */
    IConfiguredValueSet getTargetConfiguredValueSet();

    /**
     * Returns the label that is to be displayed above the target values list.
     */
    String getTargetLabel();
}
