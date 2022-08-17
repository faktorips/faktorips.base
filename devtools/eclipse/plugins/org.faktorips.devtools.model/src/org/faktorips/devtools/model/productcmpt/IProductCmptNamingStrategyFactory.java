/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A factory to create naming strategies for product components.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptNamingStrategyFactory {

    /**
     * Returns the ID of the type of naming strategy. This method never returns <code>null</code>.
     */
    String getExtensionId();

    /**
     * Creates a new naming strategy. This method never returns <code>null</code>.
     * 
     * @param ipsProject The project this strategy is for.
     * 
     * @throws NullPointerException if the IPS project is <code>null</code>.
     */
    IProductCmptNamingStrategy newProductCmptNamingStrategy(IIpsProject ipsProject);

}
