/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * Allows to batch "Move" refactor multiple {@link IIpsElement}s.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsCompositeMoveRefactoring extends IIpsCompositeRefactoring {

    /**
     * Sets the target {@link IIpsPackageFragment}.
     * 
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment}
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Sets whether the runtime ID of {@link IProductCmpt} should be adapted.
     * 
     * @param adaptRuntimeId Flag indicating whether to adapt runtime IDs
     */
    public void setAdaptRuntimeId(boolean adaptRuntimeId);

    /**
     * Returns the target {@link IIpsPackageFragment}.
     */
    public IIpsPackageFragment getTargetIpsPackageFragment();

    /**
     * Returns whether the runtime ID of {@link IProductCmpt} should be adapted.
     */
    public boolean isAdaptRuntimeId();

    /**
     * Returns whether the option 'adapt runtime id' is relevant which is the case if at least one
     * {@link IProductCmpt} shall be refactored.
     */
    public boolean isAdaptRuntimeIdRelevant();

}
