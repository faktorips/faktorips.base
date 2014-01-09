/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A factory to create naming strategies for product components.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptNamingStrategyFactory {

    /**
     * Returns the ID of the type of naming strategy. This method never returns <code>null</code>.
     */
    public String getExtensionId();

    /**
     * Creates a new naming strategy. This method never returns <code>null</code>.
     * 
     * @param ipsProject The project this strategy is for.
     * 
     * @throws NullPointerException if the ips project is <code>null</code>.
     */
    IProductCmptNamingStrategy newProductCmptNamingStrategy(IIpsProject ipsProject);

}
