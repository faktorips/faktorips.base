/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;

public class ProductCmptGenerationToTypeDelta extends PropertyValueContainerToTypeDelta {

    public ProductCmptGenerationToTypeDelta(IProductCmptGeneration generation, IIpsProject ipsProject)
            throws CoreRuntimeException {
        super(generation, generation, ipsProject);
    }

    @Override
    public IProductCmptGeneration getPropertyValueContainer() {
        return (IProductCmptGeneration)super.getPropertyValueContainer();
    }

    @Override
    protected void createAdditionalEntriesAndChildren() throws CoreRuntimeException {
        // nothing to do
    }

}
