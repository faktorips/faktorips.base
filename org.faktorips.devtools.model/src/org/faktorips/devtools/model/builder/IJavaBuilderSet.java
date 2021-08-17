/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;

/**
 * A {@link IIpsArtefactBuilderSet} that generates Java code.
 * 
 * @author dschwering
 */
public interface IJavaBuilderSet extends IJavaPackageStructure, IIpsArtefactBuilderSet {

    /**
     * Returns a list containing all <code>IJavaElement</code>s this builder set generates for the
     * given <code>IIpsObjectPartContainer</code>.
     * <p>
     * Returns an empty list if no <code>IJavaElement</code>s are generated for the provided
     * <code>IIpsObjectPartContainer</code>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsObjectPartContainer The <code>IIpsObjectPartContainer</code> to obtain the
     *            generated <code>IJavaElement</code>s for.
     * 
     * @throws NullPointerException If the parameter is null
     */
    List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer);

}