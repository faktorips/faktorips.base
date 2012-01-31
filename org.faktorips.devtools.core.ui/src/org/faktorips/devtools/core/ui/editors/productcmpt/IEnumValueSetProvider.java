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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;

/**
 * Provider used when enum valuesets are configured. E.g. when an enum value set in the model
 * (policy component type attribute) is configured with a subset by a product component (config
 * element). In above case the model valueset will be returned by {@link #getSourceEnumValueSet()}.
 * {@link #getTargetConfigElement()} will return the config element of the product component, that
 * defines the subset of the source value set.
 * <p/>
 * However there are other cases of configuring valuesets against each other. E.g. product variants
 * allow to restrict valuesets defined in a product component even further. Those cases can be
 * represented by different implentations of this interface.
 * <p/>
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
    public IEnumValueSet getSourceEnumValueSet();

    /**
     * Returns the label that is to be displayed above the source values list.
     */
    public String getSourceLabel();

    /**
     * Returns the {@link IConfigElement} that defines and contains a subset of the source-valueset.
     */
    public IConfigElement getTargetConfigElement();

    /**
     * Returns the label that is to be displayed above the target values list.
     */
    public String getTargetLabel();
}
